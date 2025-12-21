import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const authSuccessRate = new Rate('auth_success');
const walletOperations = new Counter('wallet_operations');
const categoryOperations = new Counter('category_operations');
const transactionOperations = new Counter('transaction_operations');
const debtOperations = new Counter('debt_operations');
const exportOperations = new Counter('export_operations');

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

// Test configuration - 100 users with 7000 transactions each
export const options = {
  scenarios: {
    load_test: {
      executor: 'per-vu-iterations',
      vus: 100,
      iterations: 1,
      maxDuration: '3h',
    },
  },
  thresholds: {
    'http_req_duration': ['p(95)<2000', 'p(99)<5000'],
    'http_req_failed': ['rate<0.05'],
    'errors': ['rate<0.05'],
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

  console.log(`[VU ${__VU}] Starting test - Registering user ${userEmail}`);

  // Register user
  const registerPayload = JSON.stringify({
    email: userEmail,
    password: userPassword,
    name: userName,
  });

  const registerRes = http.post(`${BASE_URL}/api/v1/auth/register`, registerPayload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'Register' },
  });

  const registerSuccess = check(registerRes, {
    'registration status is 200 or 409 (already exists)': (r) => r.status === 200 || r.status === 409,
  });

  if (!registerSuccess) {
    console.log(`[VU ${__VU}] Registration failed: ${registerRes.status} ${registerRes.body}`);
    errorRate.add(1);
    return;
  }

  authSuccessRate.add(registerSuccess);
  console.log(`[VU ${__VU}] Registration successful`);
  sleep(0.5);

  // Login to get JWT token
  const loginPayload = JSON.stringify({
    email: userEmail,
    password: userPassword,
  });

  const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, {
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
  console.log(`[VU ${__VU}] Login successful`);
  sleep(0.5);

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // ============================================
  // Scenario 2: Create 10 Wallets
  // ============================================

  console.log(`[VU ${__VU}] Creating 10 wallets...`);
  const walletIds = [];

  for (let i = 0; i < 10; i++) {
    const createWalletPayload = JSON.stringify({
      name: `Wallet-${__VU}-${i}`,
      currency: randomCurrency(),
      initialBalance: randomInt(100000, 10000000),
    });

    const createWalletRes = http.post(`${BASE_URL}/api/v1/wallets`, createWalletPayload, {
      headers: headers,
      tags: { name: 'CreateWallet' },
    });

    const createSuccess = check(createWalletRes, {
      'create wallet status is 201': (r) => r.status === 201,
    });

    if (createSuccess && createWalletRes.status === 201) {
      const body = JSON.parse(createWalletRes.body);
      walletIds.push(body.id);
      if (i === 0) {
        walletId = body.id; // Store first wallet ID
      }
    }

    walletOperations.add(1);
    errorRate.add(!createSuccess);

    if ((i + 1) % 5 === 0) {
      console.log(`[VU ${__VU}] Created ${i + 1}/10 wallets`);
    }
  }

  console.log(`[VU ${__VU}] Completed creating ${walletIds.length} wallets`);
  sleep(0.5);

  // ============================================
  // Scenario 3: Create Categories
  // ============================================

  console.log(`[VU ${__VU}] Creating categories...`);

  // Create 5 expense categories
  const expenseCategories = [];
  for (let i = 0; i < 5; i++) {
    const createCategoryPayload = JSON.stringify({
      name: `Expense-${__VU}-${randomCategoryName('EXPENSE')}-${i}`,
      type: 'EXPENSE',
    });

    const createCategoryRes = http.post(`${BASE_URL}/api/v1/categories`, createCategoryPayload, {
      headers: headers,
      tags: { name: 'CreateCategory' },
    });

    const createSuccess = check(createCategoryRes, {
      'create category status is 201': (r) => r.status === 201,
    });

    if (createSuccess && createCategoryRes.status === 201) {
      const body = JSON.parse(createCategoryRes.body);
      expenseCategories.push(body.id);
      if (!categoryId) categoryId = body.id;
    }

    categoryOperations.add(1);
    errorRate.add(!createSuccess);
  }

  // Create 2 income categories
  const incomeCategories = [];
  for (let i = 0; i < 2; i++) {
    const createCategoryPayload = JSON.stringify({
      name: `Income-${__VU}-${randomCategoryName('INCOME')}-${i}`,
      type: 'INCOME',
    });

    const createCategoryRes = http.post(`${BASE_URL}/api/v1/categories`, createCategoryPayload, {
      headers: headers,
      tags: { name: 'CreateCategory' },
    });

    const createSuccess = check(createCategoryRes, {
      'create category status is 201': (r) => r.status === 201,
    });

    if (createSuccess && createCategoryRes.status === 201) {
      const body = JSON.parse(createCategoryRes.body);
      incomeCategories.push(body.id);
    }

    categoryOperations.add(1);
    errorRate.add(!createSuccess);
  }

  const allCategories = [...expenseCategories, ...incomeCategories];
  console.log(`[VU ${__VU}] Created ${allCategories.length} categories`);
  sleep(0.5);

  // ============================================
  // Scenario 4: Create 7,000 Transactions
  // ============================================

  if (walletIds.length > 0 && allCategories.length > 0) {
    console.log(`[VU ${__VU}] Creating 7,000 transactions...`);

    let successCount = 0;
    let errorCount = 0;

    for (let i = 0; i < 7000; i++) {
      // Random wallet and category
      const randomWallet = walletIds[randomInt(0, walletIds.length - 1)];
      const isExpense = i % 3 !== 0; // 2/3 expense, 1/3 income
      const randomCategory = isExpense
        ? expenseCategories[randomInt(0, expenseCategories.length - 1)]
        : incomeCategories[randomInt(0, incomeCategories.length - 1)];

      const transactionDate = new Date();
      transactionDate.setDate(transactionDate.getDate() - randomInt(0, 365));

      const createTransactionPayload = JSON.stringify({
        walletId: randomWallet,
        categoryId: randomCategory,
        type: isExpense ? 'EXPENSE' : 'INCOME',
        amount: randomInt(10000, 1000000),
        note: `Transaction-${__VU}-${i}`,
        date: transactionDate.toISOString(),
      });

      const createTransactionRes = http.post(`${BASE_URL}/api/v1/transactions`, createTransactionPayload, {
        headers: headers,
        tags: { name: 'CreateTransaction' },
      });

      const createSuccess = check(createTransactionRes, {
        'create transaction status is 201': (r) => r.status === 201,
      });

      if (createSuccess) {
        successCount++;
      } else {
        errorCount++;
      }

      transactionOperations.add(1);
      errorRate.add(!createSuccess);

      // Progress logging every 1000 transactions
      if ((i + 1) % 1000 === 0) {
        console.log(`[VU ${__VU}] Created ${i + 1}/7000 transactions (Success: ${successCount}, Errors: ${errorCount})`);
      }
    }

    console.log(`[VU ${__VU}] Completed creating transactions - Success: ${successCount}, Errors: ${errorCount}`);
    sleep(1);
  } else {
    console.log(`[VU ${__VU}] Skipping transactions - no wallets or categories available`);
  }

  // ============================================
  // Scenario 5: Create 1,000 Debts
  // ============================================

  console.log(`[VU ${__VU}] Creating 1,000 debts...`);

  let debtSuccessCount = 0;
  let debtErrorCount = 0;

  for (let i = 0; i < 1000; i++) {
    const debtType = i % 2 === 0 ? 'PAYABLE' : 'RECEIVABLE';
    const dueDate = new Date();
    dueDate.setDate(dueDate.getDate() + randomInt(7, 365));

    const createDebtPayload = JSON.stringify({
      type: debtType,
      counterpartyName: `Person-${__VU}-${i}`,
      totalAmount: randomInt(100000, 10000000),
      dueDate: dueDate.toISOString(),
      note: `Debt-${__VU}-${i}`,
    });

    const createDebtRes = http.post(`${BASE_URL}/api/v1/debts`, createDebtPayload, {
      headers: headers,
      tags: { name: 'CreateDebt' },
    });

    const createSuccess = check(createDebtRes, {
      'create debt status is 201': (r) => r.status === 201,
    });

    if (createSuccess) {
      debtSuccessCount++;
    } else {
      debtErrorCount++;
    }

    debtOperations.add(1);
    errorRate.add(!createSuccess);

    // Progress logging every 200 debts
    if ((i + 1) % 200 === 0) {
      console.log(`[VU ${__VU}] Created ${i + 1}/1000 debts (Success: ${debtSuccessCount}, Errors: ${debtErrorCount})`);
    }
  }

  console.log(`[VU ${__VU}] Completed creating debts - Success: ${debtSuccessCount}, Errors: ${debtErrorCount}`);
  sleep(1);

  // ============================================
  // Scenario 6: Export to CSV and Excel
  // ============================================

  console.log(`[VU ${__VU}] Exporting transactions to CSV...`);

  // Export to CSV
  const exportCsvPayload = JSON.stringify({
    format: 'CSV',
    type: 'TRANSACTIONS',
    filter: null,
  });

  const exportCsvRes = http.post(`${BASE_URL}/api/v1/export/transactions`, exportCsvPayload, {
    headers: headers,
    tags: { name: 'ExportCSV' },
  });

  const csvSuccess = check(exportCsvRes, {
    'export CSV status is 200 or 403 (premium)': (r) => r.status === 200 || r.status === 403,
  });

  exportOperations.add(1);
  if (exportCsvRes.status === 403) {
    console.log(`[VU ${__VU}] Export CSV blocked - Premium feature`);
  } else if (csvSuccess) {
    console.log(`[VU ${__VU}] Export CSV successful`);
  } else {
    console.log(`[VU ${__VU}] Export CSV failed: ${exportCsvRes.status}`);
    errorRate.add(1);
  }

  sleep(1);

  // Export to Excel
  console.log(`[VU ${__VU}] Exporting transactions to Excel...`);

  const exportExcelPayload = JSON.stringify({
    format: 'EXCEL',
    type: 'TRANSACTIONS',
    filter: null,
  });

  const exportExcelRes = http.post(`${BASE_URL}/api/v1/export/transactions`, exportExcelPayload, {
    headers: headers,
    tags: { name: 'ExportExcel' },
  });

  const excelSuccess = check(exportExcelRes, {
    'export Excel status is 200 or 403 (premium)': (r) => r.status === 200 || r.status === 403,
  });

  exportOperations.add(1);
  if (exportExcelRes.status === 403) {
    console.log(`[VU ${__VU}] Export Excel blocked - Premium feature`);
  } else if (excelSuccess) {
    console.log(`[VU ${__VU}] Export Excel successful`);
  } else {
    console.log(`[VU ${__VU}] Export Excel failed: ${exportExcelRes.status}`);
    errorRate.add(1);
  }

  console.log(`[VU ${__VU}] ==> TEST COMPLETED SUCCESSFULLY`);
}

// Summary handler
export function handleSummary(data) {
  console.log('\n======================================');
  console.log('LOAD TEST SUMMARY - 100 USERS');
  console.log('======================================\n');

  console.log('Test Configuration:');
  console.log(`  Virtual Users: 100`);
  console.log(`  Per User Tasks:`);
  console.log(`    - 1 Registration`);
  console.log(`    - 10 Wallets`);
  console.log(`    - 7,000 Transactions`);
  console.log(`    - 1,000 Debts`);
  console.log(`    - 2 Exports (CSV + Excel)`);
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
  console.log(`  Auth Success Rate: ${(data.metrics.auth_success?.values.rate * 100 || 0).toFixed(2)}%`);
  console.log(`  Wallet Operations: ${data.metrics.wallet_operations?.values.count || 0}`);
  console.log(`  Category Operations: ${data.metrics.category_operations?.values.count || 0}`);
  console.log(`  Transaction Operations: ${data.metrics.transaction_operations?.values.count || 0}`);
  console.log(`  Debt Operations: ${data.metrics.debt_operations?.values.count || 0}`);
  console.log(`  Export Operations: ${data.metrics.export_operations?.values.count || 0}\n`);

  console.log('Expected Totals (100 users):');
  console.log(`  Wallets: 1,000 (100 users × 10)`);
  console.log(`  Transactions: 700,000 (100 users × 7,000)`);
  console.log(`  Debts: 100,000 (100 users × 1,000)`);
  console.log(`  Exports: 200 (100 users × 2)\n`);

  console.log('Iterations:');
  console.log(`  Total: ${data.metrics.iterations.values.count}`);
  console.log(`  Expected: 100 (1 iteration per user)\n`);

  console.log('======================================\n');

  return {
    'stdout': JSON.stringify(data, null, 2),
    'performance-tests/reports/summary.json': JSON.stringify(data, null, 2),
  };
}