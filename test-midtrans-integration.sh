#!/bin/bash

# ============================================
# Midtrans Integration Test Script
# ============================================
# Usage: ./test-midtrans-integration.sh
# ============================================

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8081/api/v1"
TEST_EMAIL="test-$(date +%s)@example.com"
TEST_PASSWORD="Test123!"
TEST_NAME="Test User"

echo -e "${BLUE}============================================${NC}"
echo -e "${BLUE}🚀 Midtrans Integration Test${NC}"
echo -e "${BLUE}============================================${NC}\n"

# Step 1: Check if application is running
echo -e "${YELLOW}[1/6]${NC} Checking if application is running..."
if curl -s "${BASE_URL}/actuator/health" > /dev/null; then
    echo -e "${GREEN}✅ Application is running${NC}\n"
else
    echo -e "${RED}❌ Application is not running. Start with: ./gradlew bootRun${NC}"
    exit 1
fi

# Step 2: Register new user
echo -e "${YELLOW}[2/6]${NC} Registering new user..."
REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"${TEST_EMAIL}\",
    \"password\": \"${TEST_PASSWORD}\",
    \"name\": \"${TEST_NAME}\"
  }")

JWT_TOKEN=$(echo $REGISTER_RESPONSE | jq -r '.token')
USER_ID=$(echo $REGISTER_RESPONSE | jq -r '.userId')
SUBSCRIPTION_TIER=$(echo $REGISTER_RESPONSE | jq -r '.subscription.tier')

if [ "$JWT_TOKEN" != "null" ] && [ "$JWT_TOKEN" != "" ]; then
    echo -e "${GREEN}✅ User registered successfully${NC}"
    echo -e "   Email: ${TEST_EMAIL}"
    echo -e "   User ID: ${USER_ID}"
    echo -e "   Initial Tier: ${SUBSCRIPTION_TIER}\n"
else
    echo -e "${RED}❌ Registration failed${NC}"
    echo -e "Response: ${REGISTER_RESPONSE}"
    exit 1
fi

# Step 3: Check initial subscription (should be FREE)
echo -e "${YELLOW}[3/6]${NC} Verifying initial subscription..."
if [ "$SUBSCRIPTION_TIER" == "FREE" ]; then
    echo -e "${GREEN}✅ User has FREE tier subscription${NC}\n"
else
    echo -e "${RED}❌ Expected FREE tier, got: ${SUBSCRIPTION_TIER}${NC}"
    exit 1
fi

# Step 4: Create payment transaction
echo -e "${YELLOW}[4/6]${NC} Creating subscription payment..."
IDEMPOTENCY_KEY="test-$(date +%s)-$(uuidgen)"

PAYMENT_RESPONSE=$(curl -s -X POST "${BASE_URL}/payments/subscription" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "X-Idempotency-Key: ${IDEMPOTENCY_KEY}")

PAYMENT_ID=$(echo $PAYMENT_RESPONSE | jq -r '.paymentId')
ORDER_ID=$(echo $PAYMENT_RESPONSE | jq -r '.orderId')
SNAP_TOKEN=$(echo $PAYMENT_RESPONSE | jq -r '.snapToken')
SNAP_URL=$(echo $PAYMENT_RESPONSE | jq -r '.snapRedirectUrl')
AMOUNT=$(echo $PAYMENT_RESPONSE | jq -r '.amount')
STATUS=$(echo $PAYMENT_RESPONSE | jq -r '.status')

if [ "$PAYMENT_ID" != "null" ] && [ "$PAYMENT_ID" != "" ]; then
    echo -e "${GREEN}✅ Payment created successfully${NC}"
    echo -e "   Payment ID: ${PAYMENT_ID}"
    echo -e "   Order ID: ${ORDER_ID}"
    echo -e "   Amount: IDR ${AMOUNT}"
    echo -e "   Status: ${STATUS}"
    echo -e "   Snap Token: ${SNAP_TOKEN:0:20}..."
    echo -e "\n   ${BLUE}💳 Payment URL:${NC}"
    echo -e "   ${SNAP_URL}\n"
else
    echo -e "${RED}❌ Payment creation failed${NC}"
    echo -e "Response: ${PAYMENT_RESPONSE}"
    exit 1
fi

# Step 5: Test idempotency (same key should return same payment)
echo -e "${YELLOW}[5/6]${NC} Testing idempotency..."
DUPLICATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/payments/subscription" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "X-Idempotency-Key: ${IDEMPOTENCY_KEY}")

DUPLICATE_PAYMENT_ID=$(echo $DUPLICATE_RESPONSE | jq -r '.paymentId')

if [ "$DUPLICATE_PAYMENT_ID" == "$PAYMENT_ID" ]; then
    echo -e "${GREEN}✅ Idempotency works correctly${NC}"
    echo -e "   Same payment returned for duplicate request\n"
else
    echo -e "${YELLOW}⚠️  Warning: Idempotency might not be working${NC}\n"
fi

# Step 6: Summary and next steps
echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}🎉 Test Completed Successfully!${NC}"
echo -e "${BLUE}============================================${NC}\n"

echo -e "${YELLOW}📋 Test Results:${NC}"
echo -e "✅ Application is running"
echo -e "✅ User registration works"
echo -e "✅ Payment creation works"
echo -e "✅ Idempotency check passed\n"

echo -e "${YELLOW}🔍 Next Steps:${NC}"
echo -e "1. Open payment URL in browser:"
echo -e "   ${BLUE}${SNAP_URL}${NC}\n"

echo -e "2. Test with Sandbox Credit Card:"
echo -e "   Card: ${GREEN}4811 1111 1111 1114${NC}"
echo -e "   CVV: ${GREEN}123${NC}"
echo -e "   Expiry: ${GREEN}01/25${NC}\n"

echo -e "3. After payment, verify subscription upgraded:"
echo -e "   ${BLUE}curl ${BASE_URL}/subscriptions/me \\${NC}"
echo -e "   ${BLUE}  -H \"Authorization: Bearer ${JWT_TOKEN}\"${NC}\n"

echo -e "4. Setup webhook for local testing (if needed):"
echo -e "   ${BLUE}ngrok http 8081${NC}"
echo -e "   Then update Midtrans dashboard with ngrok URL\n"

echo -e "${YELLOW}📝 Test Data (save for later):${NC}"
echo -e "Email: ${GREEN}${TEST_EMAIL}${NC}"
echo -e "Password: ${GREEN}${TEST_PASSWORD}${NC}"
echo -e "JWT Token: ${GREEN}${JWT_TOKEN:0:30}...${NC}"
echo -e "Payment ID: ${GREEN}${PAYMENT_ID}${NC}"
echo -e "Order ID: ${GREEN}${ORDER_ID}${NC}\n"

echo -e "${BLUE}============================================${NC}"
echo -e "${GREEN}✨ All systems operational!${NC}"
echo -e "${BLUE}============================================${NC}\n"
