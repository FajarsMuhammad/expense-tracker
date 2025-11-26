import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const authSuccessRate = new Rate('auth_success');
const walletOperations = new Counter('wallet_operations');
const categoryOperations = new Counter('category_operations');
const transactionOperations = new Counter('transaction_operations');
const dashboardRequests = new Counter('dashboard_requests');

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

// Test configuration
export const options = {
  stages: [
    { duration: '10s', target: 10 },  // Ramp-up to 10 users
    { duration: '5m', target: 10 },   // Stay at 10 users for 5 minutes
    { duration: '10s', target: 0 },   // Ramp-down to 0 users
  ],
  thresholds: {
    'http_req_duration': ['p(95)<500', 'p(99)<1000'],
    'http_req_failed': ['rate<0.01'],
    'errors': ['rate<0.01'],
    'auth_success': ['rate>0.99'],
  },
};

// Helper function to generate random data
function randomString(length) {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomCurrency() {
  const currencies = ['IDR', 'USD', 'EUR'];
  return currencies[Math.floor(Math.random() * currencies.length)];
}

function randomCategoryName(type) {
  const expenseCategories = ['Groceries', 'Transport', 'Entertainment', 'Bills', 'Shopping'];
  const incomeCategories = ['Salary', 'Bonus', 'Investment'];
  const categories = type === 'EXPENSE' ? expenseCategories : incomeCategories;
  return categories[Math.floor(Math.random() * categories.length)];
}

// Main test scenario
export default function () {
  const userId = `loadtest-user-${__VU}`;
  const userEmail = `${userId}@example.com`;
  const userPassword = 'LoadTest123!';
  const userName = `Load Test User ${__VU}`;

  let token = '';
  let walletId = '';
  let categoryId = '';
  let transactionId = '';

  // ============================================
  // Scenario 1: User Registration & Authentication
  // ============================================

  // Register user (only once per VU)
  if (__ITER === 0) {
    const registerPayload = JSON.stringify({
      email: userEmail,
      password: userPassword,
      name: userName,
    });

    const registerRes = http.post(`${BASE_URL}/auth/register`, registerPayload, {
      headers: { 'Content-Type': 'application/json' },
      tags: { name: 'Register' },
    });

    const registerSuccess = check(registerRes, {
      'registration status is 200 or 409 (already exists)': (r) => r.status === 200 || r.status === 409,
    });

    if (!registerSuccess) {
      console.log(`Registration failed for ${userEmail}: ${registerRes.status} ${registerRes.body}`);
    }

    authSuccessRate.add(registerSuccess);
    errorRate.add(!registerSuccess);

    sleep(1);
  }

  // Login to get JWT token
  const loginPayload = JSON.stringify({
    email: userEmail,
    password: userPassword,
  });

  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'Login' },
  });

  const loginSuccess = check(loginRes, {
    'login status is 200': (r) => r.status === 200,
    'login returns token': (r) => {
      if (r.status === 200) {
        const body = JSON.parse(r.body);
        return body.token && body.token.length > 0;
      }
      return false;
    },
  });

  if (loginSuccess && loginRes.status === 200) {
    const loginData = JSON.parse(loginRes.body);
    token = loginData.token;
  } else {
    console.log(`Login failed for ${userEmail}: ${loginRes.status}`);
    errorRate.add(1);
    return; // Exit if login fails
  }

  authSuccessRate.add(loginSuccess);
  errorRate.add(!loginSuccess);
  sleep(1);

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // ============================================
  // Scenario 2: Wallet CRUD Operations (30 requests)
  // ============================================

  // Create Wallet
  for (let i = 0; i < 3; i++) {
    const createWalletPayload = JSON.stringify({
      name: `Wallet ${randomString(5)}`,
      currency: randomCurrency(),
      initialBalance: randomInt(100000, 10000000),
    });

    const createWalletRes = http.post(`${BASE_URL}/wallets`, createWalletPayload, {
      headers: headers,
      tags: { name: 'CreateWallet' },
    });

    const createSuccess = check(createWalletRes, {
      'create wallet status is 201': (r) => r.status === 201,
      'wallet has id': (r) => {
        if (r.status === 201) {
          const body = JSON.parse(r.body);
          if (i === 0 && body.id) {
            walletId = body.id; // Store first wallet ID for later use
          }
          return body.id && body.id.length > 0;
        }
        return false;
      },
    });

    walletOperations.add(1);
    errorRate.add(!createSuccess);
    sleep(randomInt(1, 2));
  }

  // List Wallets
  for (let i = 0; i < 5; i++) {
    const listWalletsRes = http.get(`${BASE_URL}/wallets`, {
      headers: headers,
      tags: { name: 'ListWallets' },
    });

    const listSuccess = check(listWalletsRes, {
      'list wallets status is 200': (r) => r.status === 200,
      'list returns array': (r) => {
        if (r.status === 200) {
          const body = JSON.parse(r.body);
          return Array.isArray(body);
        }
        return false;
      },
    });

    walletOperations.add(1);
    errorRate.add(!listSuccess);
    sleep(1);
  }

  // Get Specific Wallet
  if (walletId) {
    for (let i = 0; i < 3; i++) {
      const getWalletRes = http.get(`${BASE_URL}/wallets/${walletId}`, {
        headers: headers,
        tags: { name: 'GetWallet' },
      });

      const getSuccess = check(getWalletRes, {
        'get wallet status is 200': (r) => r.status === 200,
      });

      walletOperations.add(1);
      errorRate.add(!getSuccess);
      sleep(1);
    }

    // Update Wallet
    for (let i = 0; i < 2; i++) {
      const updateWalletPayload = JSON.stringify({
        name: `Updated Wallet ${randomString(5)}`,
        currency: 'USD',
        initialBalance: randomInt(200000, 5000000),
      });

      const updateWalletRes = http.put(`${BASE_URL}/wallets/${walletId}`, updateWalletPayload, {
        headers: headers,
        tags: { name: 'UpdateWallet' },
      });

      const updateSuccess = check(updateWalletRes, {
        'update wallet status is 200': (r) => r.status === 200,
      });

      walletOperations.add(1);
      errorRate.add(!updateSuccess);
      sleep(1);
    }
  }

  // ============================================
  // Scenario 3: Category CRUD Operations (25 requests)
  // ============================================

  // Create Categories
  const categoryTypes = ['EXPENSE', 'EXPENSE', 'INCOME'];
  for (let i = 0; i < categoryTypes.length; i++) {
    const type = categoryTypes[i];
    const createCategoryPayload = JSON.stringify({
      name: `${randomCategoryName(type)} ${randomString(3)}`,
      type: type,
    });

    const createCategoryRes = http.post(`${BASE_URL}/api/v1/categories`, createCategoryPayload, {
      headers: headers,
      tags: { name: 'CreateCategory' },
    });

    const createSuccess = check(createCategoryRes, {
      'create category status is 201': (r) => r.status === 201,
      'category has id': (r) => {
        if (r.status === 201) {
          const body = JSON.parse(r.body);
          if (i === 0 && body.id) {
            categoryId = body.id; // Store first category ID
          }
          return body.id && body.id.length > 0;
        }
        return false;
      },
    });

    categoryOperations.add(1);
    errorRate.add(!createSuccess);
    sleep(randomInt(1, 2));
  }

  // List Categories
  for (let i = 0; i < 8; i++) {
    const listCategoriesRes = http.get(`${BASE_URL}/api/v1/categories`, {
      headers: headers,
      tags: { name: 'ListCategories' },
    });

    const listSuccess = check(listCategoriesRes, {
      'list categories status is 200': (r) => r.status === 200,
    });

    categoryOperations.add(1);
    errorRate.add(!listSuccess);
    sleep(1);
  }

  // Get Specific Category
  if (categoryId) {
    for (let i = 0; i < 5; i++) {
      const getCategoryRes = http.get(`${BASE_URL}/api/v1/categories/${categoryId}`, {
        headers: headers,
        tags: { name: 'GetCategory' },
      });

      const getSuccess = check(getCategoryRes, {
        'get category status is 200': (r) => r.status === 200,
      });

      categoryOperations.add(1);
      errorRate.add(!getSuccess);
      sleep(1);
    }

    // Update Category
    for (let i = 0; i < 2; i++) {
      const updateCategoryPayload = JSON.stringify({
        name: `Updated Category ${randomString(4)}`,
      });

      const updateCategoryRes = http.put(`${BASE_URL}/api/v1/categories/${categoryId}`, updateCategoryPayload, {
        headers: headers,
        tags: { name: 'UpdateCategory' },
      });

      const updateSuccess = check(updateCategoryRes, {
        'update category status is 200': (r) => r.status === 200,
      });

      categoryOperations.add(1);
      errorRate.add(!updateSuccess);
      sleep(1);
    }
  }

  // ============================================
  // Scenario 4: Transaction Operations (30 requests)
  // ============================================

  if (walletId && categoryId) {
    // Create Transactions
    for (let i = 0; i < 10; i++) {
      const transactionDate = new Date();
      transactionDate.setDate(transactionDate.getDate() - randomInt(0, 30));

      const createTransactionPayload = JSON.stringify({
        walletId: walletId,
        categoryId: categoryId,
        type: i % 3 === 0 ? 'INCOME' : 'EXPENSE',
        amount: randomInt(10000, 1000000),
        note: `Transaction ${randomString(6)}`,
        date: transactionDate.toISOString(),
      });

      const createTransactionRes = http.post(`${BASE_URL}/transactions`, createTransactionPayload, {
        headers: headers,
        tags: { name: 'CreateTransaction' },
      });

      const createSuccess = check(createTransactionRes, {
        'create transaction status is 201': (r) => r.status === 201,
        'transaction has id': (r) => {
          if (r.status === 201) {
            const body = JSON.parse(r.body);
            if (i === 0 && body.id) {
              transactionId = body.id;
            }
            return body.id && body.id.length > 0;
          }
          return false;
        },
      });

      transactionOperations.add(1);
      errorRate.add(!createSuccess);
      sleep(randomInt(1, 3));
    }

    // List Transactions
    for (let i = 0; i < 10; i++) {
      const listTransactionsRes = http.get(`${BASE_URL}/transactions`, {
        headers: headers,
        tags: { name: 'ListTransactions' },
      });

      const listSuccess = check(listTransactionsRes, {
        'list transactions status is 200': (r) => r.status === 200,
      });

      transactionOperations.add(1);
      errorRate.add(!listSuccess);
      sleep(1);
    }

    // Update Transaction
    if (transactionId) {
      for (let i = 0; i < 3; i++) {
        const updateTransactionPayload = JSON.stringify({
          walletId: walletId,
          categoryId: categoryId,
          type: 'EXPENSE',
          amount: randomInt(50000, 500000),
          note: `Updated Transaction ${randomString(5)}`,
          date: new Date().toISOString(),
        });

        const updateTransactionRes = http.put(`${BASE_URL}/transactions/${transactionId}`, updateTransactionPayload, {
          headers: headers,
          tags: { name: 'UpdateTransaction' },
        });

        const updateSuccess = check(updateTransactionRes, {
          'update transaction status is 200': (r) => r.status === 200,
        });

        transactionOperations.add(1);
        errorRate.add(!updateSuccess);
        sleep(1);
      }
    }
  }

  // ============================================
  // Scenario 5: Dashboard & Analytics (15 requests)
  // ============================================

  // Get Dashboard Summary
  for (let i = 0; i < 10; i++) {
    const dashboardRes = http.get(`${BASE_URL}/dashboard/summary`, {
      headers: headers,
      tags: { name: 'Dashboard' },
    });

    const dashboardSuccess = check(dashboardRes, {
      'dashboard status is 200': (r) => r.status === 200,
    });

    dashboardRequests.add(1);
    errorRate.add(!dashboardSuccess);
    sleep(randomInt(2, 4));
  }

  // Get Current User
  for (let i = 0; i < 5; i++) {
    const meRes = http.get(`${BASE_URL}/me`, {
      headers: headers,
      tags: { name: 'GetCurrentUser' },
    });

    const meSuccess = check(meRes, {
      'get current user status is 200': (r) => r.status === 200,
      'user has email': (r) => {
        if (r.status === 200) {
          const body = JSON.parse(r.body);
          return body.email === userEmail;
        }
        return false;
      },
    });

    errorRate.add(!meSuccess);
    sleep(1);
  }

  sleep(randomInt(1, 3));
}

// Summary handler
export function handleSummary(data) {
  console.log('\n======================================');
  console.log('PERFORMANCE TEST SUMMARY');
  console.log('======================================\n');

  console.log('Test Configuration:');
  console.log(`  Virtual Users: 10`);
  console.log(`  Duration: ~5 minutes`);
  console.log(`  Base URL: ${BASE_URL}\n`);

  console.log('HTTP Metrics:');
  console.log(`  Total Requests: ${data.metrics.http_reqs.values.count}`);
  console.log(`  Requests/sec: ${data.metrics.http_reqs.values.rate.toFixed(2)}`);
  console.log(`  Failed Requests: ${data.metrics.http_req_failed.values.passes}`);
  console.log(`  Error Rate: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%\n`);

  console.log('Response Times:');
  console.log(`  Average: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`  Median (p50): ${data.metrics.http_req_duration.values.med.toFixed(2)}ms`);
  console.log(`  p95: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`  p99: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms`);
  console.log(`  Max: ${data.metrics.http_req_duration.values.max.toFixed(2)}ms\n`);

  console.log('Custom Metrics:');
  console.log(`  Auth Success Rate: ${(data.metrics.auth_success.values.rate * 100).toFixed(2)}%`);
  console.log(`  Wallet Operations: ${data.metrics.wallet_operations.values.count}`);
  console.log(`  Category Operations: ${data.metrics.category_operations.values.count}`);
  console.log(`  Transaction Operations: ${data.metrics.transaction_operations.values.count}`);
  console.log(`  Dashboard Requests: ${data.metrics.dashboard_requests.values.count}\n`);

  console.log('Iterations:');
  console.log(`  Total: ${data.metrics.iterations.values.count}`);
  console.log(`  Per VU: ~${Math.round(data.metrics.iterations.values.count / 10)}\n`);

  console.log('======================================\n');

  return {
    'stdout': JSON.stringify(data, null, 2),
    'performance-tests/reports/summary.json': JSON.stringify(data, null, 2),
  };
}