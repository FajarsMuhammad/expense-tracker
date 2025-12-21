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

// Test configuration - 100 existing users with 7000 transactions each
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

// Main test scenario for existing users
export default function () {
  // Use existing users created from previous test
  const userId = `loadtest-user-${__VU}`;
  const userEmail = `${userId}@example.com`;
  const userPassword = 'LoadTest123!';

  let token = '';
  const walletIds = [];
  const allCategories = [];
  const expenseCategories = [];
  const incomeCategories = [];

  // ============================================
  // Scenario 1: Login with Existing User
  // ============================================

  console.log(`[VU ${__VU}] Logging in with existing user ${userEmail}`);

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
      const body = JSON.parse(r.body);
      return body.token !== undefined;
    },
  });

  if (!loginSuccess) {
    console.log(`[VU ${__VU}] Login failed: ${loginRes.status} ${loginRes.body}`);
    errorRate.add(1);
    authSuccessRate.add(0);
    return;
  }

  authSuccessRate.add(1);
  const loginBody = JSON.parse(loginRes.body);
  token = loginBody.token;

  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  };

  console.log(`[VU ${__VU}] Login successful`);
  sleep(0.5);

  // ============================================
  // Scenario 2: Get Existing Wallets
  // ============================================

  console.log(`[VU ${__VU}] Fetching existing wallets...`);

  const getWalletsRes = http.get(`${BASE_URL}/api/v1/wallets`, {
    headers: headers,
    tags: { name: 'GetWallets' },
  });

  if (getWalletsRes.status === 200) {
    const walletsBody = JSON.parse(getWalletsRes.body);
    if (walletsBody.content && walletsBody.content.length > 0) {
      walletsBody.content.forEach((wallet) => {
        walletIds.push(wallet.id);
      });
      console.log(`[VU ${__VU}] Found ${walletIds.length} existing wallets`);
    } else {
      console.log(`[VU ${__VU}] No existing wallets found, creating 10 new wallets...`);
      // Create 10 wallets if none exist
      for (let i = 0; i < 10; i++) {
        const createWalletPayload = JSON.stringify({
          name: `Wallet-${__VU}-${i + 1}`,
          currency: randomCurrency(),
          initialBalance: randomInt(100000, 10000000),
          description: `Load test wallet ${i + 1} for VU ${__VU}`,
        });

        const createWalletRes = http.post(`${BASE_URL}/api/v1/wallets`, createWalletPayload, {
          headers: headers,
          tags: { name: 'CreateWallet' },
        });

        if (createWalletRes.status === 201) {
          const walletBody = JSON.parse(createWalletRes.body);
          walletIds.push(walletBody.id);
          walletOperations.add(1);
        }
      }
      console.log(`[VU ${__VU}] Created ${walletIds.length} wallets`);
    }
  }

  sleep(0.5);

  // ============================================
  // Scenario 3: Get Existing Categories
  // ============================================

  console.log(`[VU ${__VU}] Fetching existing categories...`);

  const getCategoriesRes = http.get(`${BASE_URL}/api/v1/categories`, {
    headers: headers,
    tags: { name: 'GetCategories' },
  });

  if (getCategoriesRes.status === 200) {
    const categoriesBody = JSON.parse(getCategoriesRes.body);
    if (categoriesBody.content && categoriesBody.content.length > 0) {
      categoriesBody.content.forEach((category) => {
        allCategories.push(category.id);
        if (category.type === 'EXPENSE') {
          expenseCategories.push(category.id);
        } else {
          incomeCategories.push(category.id);
        }
      });
      console.log(`[VU ${__VU}] Found ${allCategories.length} existing categories (${expenseCategories.length} expense, ${incomeCategories.length} income)`);
    } else {
      console.log(`[VU ${__VU}] No existing categories found, creating categories...`);
      // Create expense categories
      const expenseCategoryNames = ['Groceries', 'Transport', 'Entertainment', 'Bills', 'Shopping'];
      for (const name of expenseCategoryNames) {
        const createCategoryPayload = JSON.stringify({
          name: name,
          type: 'EXPENSE',
          description: `${name} category for VU ${__VU}`,
        });

        const createCategoryRes = http.post(`${BASE_URL}/api/v1/categories`, createCategoryPayload, {
          headers: headers,
          tags: { name: 'CreateCategory' },
        });

        if (createCategoryRes.status === 201) {
          const categoryBody = JSON.parse(createCategoryRes.body);
          allCategories.push(categoryBody.id);
          expenseCategories.push(categoryBody.id);
          categoryOperations.add(1);
        }
      }

      // Create income categories
      const incomeCategoryNames = ['Salary', 'Bonus', 'Investment'];
      for (const name of incomeCategoryNames) {
        const createCategoryPayload = JSON.stringify({
          name: name,
          type: 'INCOME',
          description: `${name} category for VU ${__VU}`,
        });

        const createCategoryRes = http.post(`${BASE_URL}/api/v1/categories`, createCategoryPayload, {
          headers: headers,
          tags: { name: 'CreateCategory' },
        });

        if (createCategoryRes.status === 201) {
          const categoryBody = JSON.parse(createCategoryRes.body);
          allCategories.push(categoryBody.id);
          incomeCategories.push(categoryBody.id);
          categoryOperations.add(1);
        }
      }
      console.log(`[VU ${__VU}] Created ${allCategories.length} categories`);
    }
  }

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
  // Scenario 5: Create 1,000 Debts (Optional)
  // ============================================

  console.log(`[VU ${__VU}] Creating 1,000 debts...`);

  let debtSuccessCount = 0;
  for (let i = 0; i < 1000; i++) {
    const isLent = i % 2 === 0; // 50% lent, 50% borrowed

    const createDebtPayload = JSON.stringify({
      personName: `Person-${__VU}-${i}`,
      amount: randomInt(100000, 10000000),
      type: isLent ? 'LENT' : 'BORROWED',
      description: `Debt ${i + 1} for VU ${__VU}`,
      dueDate: new Date(Date.now() + randomInt(1, 365) * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      status: 'UNPAID',
    });

    const createDebtRes = http.post(`${BASE_URL}/api/v1/debts`, createDebtPayload, {
      headers: headers,
      tags: { name: 'CreateDebt' },
    });

    if (createDebtRes.status === 201) {
      debtSuccessCount++;
      debtOperations.add(1);
    }

    // Progress logging every 200 debts
    if ((i + 1) % 200 === 0) {
      console.log(`[VU ${__VU}] Created ${i + 1}/1000 debts (Success: ${debtSuccessCount})`);
    }
  }

  console.log(`[VU ${__VU}] Completed creating debts - Success: ${debtSuccessCount}`);
  sleep(1);

  // ============================================
  // Scenario 6: Export Operations
  // ============================================

  console.log(`[VU ${__VU}] Performing export operations...`);

  // Export to CSV
  const exportCsvRes = http.get(`${BASE_URL}/api/v1/transactions/export/csv`, {
    headers: headers,
    tags: { name: 'ExportCSV' },
  });

  if (exportCsvRes.status === 200) {
    exportOperations.add(1);
    console.log(`[VU ${__VU}] CSV export successful`);
  }

  sleep(1);

  // Export to Excel
  const exportExcelRes = http.get(`${BASE_URL}/api/v1/transactions/export/excel`, {
    headers: headers,
    tags: { name: 'ExportExcel' },
  });

  if (exportExcelRes.status === 200) {
    exportOperations.add(1);
    console.log(`[VU ${__VU}] Excel export successful`);
  }

  console.log(`[VU ${__VU}] Test completed successfully!`);
}

// Summary handler
export function handleSummary(data) {
  console.log('\n======================================');
  console.log('LOAD TEST SUMMARY - 100 EXISTING USERS');
  console.log('======================================\n');

  console.log('Test Configuration:');
  console.log(`  Virtual Users: 100 (existing users)`);
  console.log(`  Per User Tasks:`);
  console.log(`    - 1 Login (no registration)`);
  console.log(`    - Use existing wallets/categories (or create if missing)`);
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
  console.log(`  Logins: 100`);
  console.log(`  Transactions: 700,000 (100 users × 7,000)`);
  console.log(`  Debts: 100,000 (100 users × 1,000)`);
  console.log(`  Exports: 200 (100 users × 2)\n`);

  console.log('Iterations:');
  console.log(`  Total: ${data.metrics.iterations.values.count}`);
  console.log(`  Expected: 100 (1 iteration per user)\n`);

  console.log('Test Duration:');
  console.log(`  Total Time: ${(data.state.testRunDurationMs / 1000 / 60).toFixed(2)} minutes\n`);

  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
  };
}

function textSummary(data, options) {
  return '';
}
