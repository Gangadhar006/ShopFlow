#!/bin/bash
set -e
BASE=${1:-http://localhost}

echo "Running smoke tests against $BASE"

# Register
REGISTER=$(curl -sf -X POST $BASE:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"smoke@test.com","password":"password123"}')
echo "Register: OK"

# Login
LOGIN=$(curl -sf -X POST $BASE:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"smoke@test.com","password":"password123"}')
TOKEN=$(echo $LOGIN | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo "Login: OK"

# Get profile
curl -sf $BASE:8081/api/users/me \
  -H "Authorization: Bearer $TOKEN" > /dev/null
echo "Get profile: OK"

# Get products
curl -sf $BASE:8082/api/products > /dev/null
echo "Get products: OK"

echo "All smoke tests passed"
