#!/bin/bash

# =========================================================
# 🪄 THE GRANDMASTER'S RUNES: Reusable Validation Functions
# =========================================================

verify_not_null() {
  local VALUE=$1
  local ERROR_MESSAGE=$2
  if [ "$VALUE" = "null" ] || [ -z "$VALUE" ]; then
    echo "❌ CRITICAL FAILURE: $ERROR_MESSAGE"
    exit 1
  fi
}

verify_execution() {
  local EXIT_CODE=$1
  local ERROR_MESSAGE=$2
  if [ $EXIT_CODE -ne 0 ]; then
    echo "❌ CRITICAL FAILURE: $ERROR_MESSAGE"
    exit 1
  fi
}

verify_delivery_status() {
  local EXPECTED_STATUS=$1
  local CURRENT_STATUS=$(curl -s "http://localhost:9999/api/v1/deliveries/$DELIVERY_ID" \
    -H "Authorization: Bearer $TOKEN" | jq -r '.status')

  if [ "$CURRENT_STATUS" != "$EXPECTED_STATUS" ]; then
    echo "❌ CRITICAL FAILURE: Expected $EXPECTED_STATUS, but status is $CURRENT_STATUS"
    exit 1
  fi
  echo "   Current Status: $CURRENT_STATUS"
}

echo "================================================"
echo "🐺 PETRECA DELIVERY - END-TO-END DEVSECOPS TEST"
echo "================================================"

# ---------------------------------------------------------
# PHASE 1: THE AUTHENTICATION
# ---------------------------------------------------------
echo "🔑 Requesting JWT from Keycloak (User: geralt)..."

TOKEN=$(curl -s -X POST "http://localhost:8082/realms/petreca-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=petreca-api-client" \
  -d "grant_type=password" \
  -d "username=geralt" \
  -d "password=witcher123" | jq -r '.access_token')

verify_not_null "$TOKEN" "Could not retrieve token from Keycloak."
echo "✅ Token successfully acquired!"
echo "------------------------------------------------"

# ---------------------------------------------------------
# PHASE 2: THE LIFECYCLE
# ---------------------------------------------------------

# Step 1: Create a Courier
echo "📍 Step 1: Recruiting a witcher..."
# -D - dumps headers, -o /dev/null hides body. grep finds Location, awk gets the last segment (the ID), tr removes carriage returns.
COURIER_LOCATION=$(curl -s -D - -X POST "http://localhost:9999/api/v1/couriers" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Geralt of Rivia", "phone": "11987654321"}' -o /dev/null | grep -i ^Location)

verify_not_null "$COURIER_LOCATION" "Could not get Location header for Courier."

COURIER_ID=$(echo "$COURIER_LOCATION" | awk -F'/' '{print $NF}' | tr -d '\r')
verify_not_null "$COURIER_ID" "Could not extract Courier ID. Gateway cache might be cold."

echo "✅ Courier created: $COURIER_ID"
echo "   Name: Geralt of Rivia"
echo ""

# Step 2: Create a Delivery (Draft)
echo "📦 Step 2: Drafting a delivery contract..."
DELIVERY_LOCATION=$(curl -s -D - -X POST "http://localhost:9999/api/v1/deliveries" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": {"zipCode": "12345-000", "street": "Rua A", "number": "10", "name": "Emp A", "phone": "119"},
    "recipient": {"zipCode": "54321-000", "street": "Av B", "number": "20", "name": "Cli B", "phone": "118"},
    "items": [{ "name": "Silver Sword", "quantity": 1 }]
  }' -o /dev/null | grep -i ^Location)

verify_not_null "$DELIVERY_LOCATION" "Could not get Location header for Delivery."

DELIVERY_ID=$(echo "$DELIVERY_LOCATION" | awk -F'/' '{print $NF}' | tr -d '\r')
verify_not_null "$DELIVERY_ID" "Could not extract Delivery ID. Wait a moment for routes to sync and try again."

echo "✅ Delivery drafted: $DELIVERY_ID"
verify_delivery_status "DRAFT"
echo ""

# Step 3: Place the Delivery
echo "📋 Step 3: Posting contract on the notice board..."
curl -s -X POST "http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/placement" \
  -H "Authorization: Bearer $TOKEN" > /dev/null

verify_execution $? "Could not place delivery on the notice board."

verify_delivery_status "WAITING_FOR_COURIER"
echo "   Event: DeliveryPlacedEvent → Kafka"
echo ""

# Step 4: Assign Courier (Pick Up)
echo "🛵 Step 4: Geralt accepts the contract..."
curl -s -X POST "http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/pickups" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"courierId\": \"$COURIER_ID\"}" > /dev/null

verify_execution $? "Could not assign courier to the delivery."

verify_delivery_status "IN_TRANSIT"
echo "   Event: DeliveryPickedUpEvent → Kafka"
echo ""

# Step 5: Complete the Delivery
echo "🏆 Step 5: Contract fulfilled..."
curl -s -X POST "http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/completion" \
  -H "Authorization: Bearer $TOKEN" > /dev/null

verify_execution $? "Could not complete the delivery."

verify_delivery_status "DELIVERED"
echo "   Event: DeliveryFulfilledEvent → Kafka"
echo ""

echo "========================================"
echo "⚔️ Contract complete. Toss a coin to your witcher."
echo ""
echo "📊 View Kafka events at: http://localhost:8090"
echo "🗄️ View database at: http://localhost:5050"
echo "📋 View service registry at: http://localhost:8761"
echo "📉 View metrics dashboard at: http://localhost:3000"
echo "🔐 View Keycloak admin at: http://localhost:8082"
echo "🕵️‍♂️ View distributed traces at: http://localhost:16686"