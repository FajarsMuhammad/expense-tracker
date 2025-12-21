#!/bin/bash

# K6 Performance Test Runner Script for Expense Tracker API
# This script checks prerequisites and runs the K6 load test

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "======================================"
echo "K6 Performance Test Runner"
echo "Expense Tracker API"
echo "======================================"
echo ""

# Check if k6 is installed
echo "Checking prerequisites..."
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}✗ K6 is not installed${NC}"
    echo ""
    echo "Please install K6:"
    echo "  macOS:   brew install k6"
    echo "  Windows: choco install k6"
    echo "  Linux:   https://k6.io/docs/getting-started/installation/"
    exit 1
fi

echo -e "${GREEN}✓ K6 is installed$(NC) ($(k6 version))"

# Check if application is running
echo "Checking if application is running on localhost:8081..."
if curl -s http://localhost:8081/api/v1/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Application is running${NC}"
else
    echo -e "${RED}✗ Application is not running on localhost:8081${NC}"
    echo ""
    echo "Please start the application first:"
    echo "  ./gradlew bootRun"
    echo "  OR"
    echo "  podman-compose -f docker-compose-monitoring.yml up -d"
    exit 1
fi

# Check if PostgreSQL is running
echo "Checking if PostgreSQL is running..."
if lsof -i :5432 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PostgreSQL is running${NC}"
else
    echo -e "${YELLOW}⚠ PostgreSQL may not be running on port 5432${NC}"
    echo "  The test may fail if the database is not accessible"
fi

echo ""
echo "======================================"
echo "Starting K6 Performance Test"
echo "======================================"
echo ""
echo "Configuration:"
echo "  Virtual Users: 10"
echo "  Duration: ~5 minutes"
echo "  Target: http://localhost:8081"
echo ""

# Create reports directory if it doesn't exist
mkdir -p reports

# Run the test
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="reports/results_${TIMESTAMP}.json"

echo "Running test... (this will take ~5 minutes)"
echo ""

k6 run --out json="${REPORT_FILE}" k6-test.js
#k6 run --out json="${REPORT_FILE}" k6-test-existing-users.js

EXIT_CODE=$?

echo ""
echo "======================================"
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ Test completed successfully${NC}"
    echo ""
    echo "Report saved to: ${REPORT_FILE}"
    echo ""
    echo "To view the report:"
    echo "  cat ${REPORT_FILE} | jq '.metrics'"
else
    echo -e "${RED}✗ Test failed with exit code ${EXIT_CODE}${NC}"
fi
echo "======================================"

exit $EXIT_CODE
