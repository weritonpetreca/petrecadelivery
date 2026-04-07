#!/bin/bash

# Configuration
BASE_URL="http://localhost:9999"
KEYCLOAK_URL="http://localhost:8082"
REALM="petreca-realm"
CLIENT_ID="petreca-api-client"
USERNAME="geralt"
PASSWORD="witcher123"

echo "================================================"
echo "🐺 PETRECA DELIVERY - END-TO-END DEVSECOPS TEST"
echo "================================================"

# ========================================================
# 1. KEYCLOAK WARM-UP & TOKEN ACQUISITION
# ========================================================
echo "🔑 Requesting JWT from Keycloak (User: $USERNAME)..."

MAX_KC_RETRIES=15
KC_ATTEMPT=1
TOKEN=""

while [ $KC_ATTEMPT -le $MAX_KC_RETRIES ]; do
  TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=$CLIENT_ID" \
    -d "grant_type=password" \
    -d "username=$USERNAME" \
    -d "password=$PASSWORD")

  # Extract token using grep
  TOKEN=$(echo "$TOKEN_RESPONSE" | grep -oP '"access_token":"\K[^"]+')

  if [ -n "$TOKEN" ]; then
    echo "✅ Token successfully acquired!"
    break
  fi

  sleep 5
  KC_ATTEMPT=$((KC_ATTEMPT+1))
done

if [ -z "$TOKEN" ]; then
  echo "❌ CRITICAL FAILURE: Could not acquire token from Keycloak."
  exit 1
fi
echo "------------------------------------------------"

# ========================================================
# 2. THE JVM WARM-UP LOOP
# ========================================================
MAX_JVM_RETRIES=15
JVM_ATTEMPT=1
WARMED_UP=false

while [ $JVM_ATTEMPT -le $MAX_JVM_RETRIES ]; do
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/v1/deliveries" -H "Authorization: Bearer $TOKEN")

  if [ "$HTTP_STATUS" -eq 200 ] || [ "$HTTP_STATUS" -eq 201 ] || [ "$HTTP_STATUS" -eq 403 ]; then
    WARMED_UP=true
    break
  fi
  sleep 5
  JVM_ATTEMPT=$((JVM_ATTEMPT+1))
done

if [ "$WARMED_UP" = false ]; then
  echo "❌ CRITICAL FAILURE: The Continent did not stabilize in time."
  exit 1
fi

# ========================================================
# Helper Functions
# ========================================================
execute_with_retry() {
  local ATTEMPTS=5
  local SLEEP_TIME=5
  local CMD="$1"
  local ERROR_MSG="$2"

  for ((i=1;i<=ATTEMPTS;i++)); do
    eval "$CMD"
    if [ "$?" -eq 0 ]; then
      return 0 # Silent success
    fi
    sleep $SLEEP_TIME
  done

  echo "❌ CRITICAL FAILURE: $ERROR_MSG"
  exit 1
}

verify_status() {
  local EXPECTED_STATUS="$1"
  local ACTUAL_STATUS=""

  # We loop up to 3 times to account for microsecond database save delays
  for ((i=1;i<=3;i++)); do
    local RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/deliveries/$DELIVERY_ID" -H "Authorization: Bearer $TOKEN")
    ACTUAL_STATUS=$(echo "$RESPONSE" | grep -oP '"status":"\K[^"]+')

    if [ "$ACTUAL_STATUS" == "$EXPECTED_STATUS" ]; then
      echo "   Current Status: $ACTUAL_STATUS (Verified 🔍)"
      return 0
    fi
    sleep 1
  done

  echo "❌ STATE MISMATCH: Expected $EXPECTED_STATUS but found $ACTUAL_STATUS"
  exit 1
}

# ========================================================
# 3. THE CONTRACT
# ========================================================

echo "📍 Step 1: Recruiting a witcher..."
COURIER_PAYLOAD='{"name": "Geralt of Rivia", "phone": "11987654321"}'

create_courier() {
  COURIER_HEADER=$(curl -s -D - -o /dev/null -X POST "$BASE_URL/api/v1/couriers" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$COURIER_PAYLOAD")
  COURIER_LOCATION=$(echo "$COURIER_HEADER" | grep -i Location | awk '{print $2}' | tr -d '\r')
  COURIER_ID=$(basename "$COURIER_LOCATION")
  [ -n "$COURIER_ID" ] && [ "$COURIER_ID" != "/" ]
}

execute_with_retry "create_courier" "Could not get Location header for Courier."
echo "✅ Courier created: $COURIER_ID"
echo "   Name: Geralt of Rivia"

echo ""
echo "📦 Step 2: Drafting a delivery contract..."
DELIVERY_PAYLOAD='{
  "sender": {"zipCode": "12345-000", "street": "Rua A", "number": "10", "name": "Emp A", "phone": "119"},
  "recipient": {"zipCode": "54321-000", "street": "Av B", "number": "20", "name": "Cli B", "phone": "118"},
  "items": [{"name": "Silver Sword", "quantity": 1}]
}'

create_delivery() {
  DELIVERY_HEADER=$(curl -s -D - -o /dev/null -X POST "$BASE_URL/api/v1/deliveries" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$DELIVERY_PAYLOAD")
  DELIVERY_LOCATION=$(echo "$DELIVERY_HEADER" | grep -i Location | awk '{print $2}' | tr -d '\r')
  DELIVERY_ID=$(basename "$DELIVERY_LOCATION")
  [ -n "$DELIVERY_ID" ] && [ "$DELIVERY_ID" != "/" ]
}

execute_with_retry "create_delivery" "Could not get Location header for Delivery."
echo "✅ Delivery drafted: $DELIVERY_ID"
verify_status "DRAFT"

echo ""
echo "📋 Step 3: Posting contract on the notice board..."
place_delivery() {
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/deliveries/$DELIVERY_ID/placement" -H "Authorization: Bearer $TOKEN")
  [ "$HTTP_STATUS" -eq 204 ] || [ "$HTTP_STATUS" -eq 200 ]

  local ACTUAL_STATUS=$(curl -s -X GET "$BASE_URL/api/v1/deliveries/$DELIVERY_ID" -H "Authorization: Bearer $TOKEN" | grep -oP '"status":"\K[^"]+')
    if [ "$ACTUAL_STATUS" == "WAITING_FOR_COURIER" ]; then return 0; fi

    return 1
}

execute_with_retry "place_delivery" "Placement failed or rolled back."
verify_status "WAITING_FOR_COURIER"
echo "   Event: DeliveryPlacedEvent → Kafka"

echo ""
echo "🛵 Step 4: Geralt accepts the contract..."
PICKUP_PAYLOAD="{\"courierId\": \"$COURIER_ID\"}"
pickup_delivery() {
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/deliveries/$DELIVERY_ID/pickups" \
    -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$PICKUP_PAYLOAD")
  [ "$HTTP_STATUS" -eq 204 ] || [ "$HTTP_STATUS" -eq 200 ]

  local ACTUAL_STATUS=$(curl -s -X GET "$BASE_URL/api/v1/deliveries/$DELIVERY_ID" -H "Authorization: Bearer $TOKEN" | grep -oP '"status":"\K[^"]+')
    if [ "$ACTUAL_STATUS" == "IN_TRANSIT" ]; then return 0; fi

    return 1
}

execute_with_retry "pickup_delivery" "Pickup failed."
verify_status "IN_TRANSIT"
echo "   Event: DeliveryPickedUpEvent → Kafka"

echo ""
echo "🏆 Step 5: Contract fulfilled..."
fulfill_delivery() {
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/v1/deliveries/$DELIVERY_ID/completion" -H "Authorization: Bearer $TOKEN")
  [ "$HTTP_STATUS" -eq 204 ] || [ "$HTTP_STATUS" -eq 200 ]

  local ACTUAL_STATUS=$(curl -s -X GET "$BASE_URL/api/v1/deliveries/$DELIVERY_ID" -H "Authorization: Bearer $TOKEN" | grep -oP '"status":"\K[^"]+')
    if [ "$ACTUAL_STATUS" == "DELIVERED" ]; then return 0; fi

    return 1
}

execute_with_retry "fulfill_delivery" "Fulfillment failed."
verify_status "DELIVERED"
echo "   Event: DeliveryFulfilledEvent → Kafka"

echo ""
echo "========================================"
echo "⚔️ Contract complete. Toss a coin to your witcher."
echo ""
echo "📊 View Kafka events at: http://localhost:8090"
echo "🗄️ View database at: http://localhost:5050"
echo "📋 View service registry at: http://localhost:8761"
echo "📉 View metrics dashboard at: http://localhost:3000"
echo "🔐 View Keycloak admin at: $KEYCLOAK_URL"
echo "🕵️‍♂️ View distributed traces at: http://localhost:16686"