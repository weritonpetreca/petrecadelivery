#!/bin/bash

echo "================================================"
echo "🐺 PETRECA DELIVERY - END-TO-END DEVSECOPS TEST"
echo "================================================"

# ---------------------------------------------------------
# PHASE 1: THE AUTHENTICATION (The New Code)
# ---------------------------------------------------------
echo "🔑 Requesting JWT from Keycloak (User: geralt)..."

TOKEN=$(curl -s -X POST "http://localhost:8082/realms/petreca-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=petreca-api-client" \
  -d "grant_type=password" \
  -d "username=geralt" \
  -d "password=witcher123" | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  echo "❌ CRITICAL FAILURE: Could not retrieve token."
  exit 1
fi
echo "✅ Token successfully acquired!"
echo "------------------------------------------------"

# ---------------------------------------------------------
# PHASE 2: YOUR ORIGINAL LIFECYCLE LOGIC (The Old Code)
# ---------------------------------------------------------

# Step 1: Create a Courier (Geralt of Rivia)
echo "📍 Step 1: Recruiting a witcher..."
COURIER_RESPONSE=$(curl -s -X POST "http://localhost:9999/api/v1/couriers" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Geralt of Rivia",
    "phone": "11987654321"
  }')

if [ "$COURIER_RESPONSE" = "null" ] || [ -z "$COURIER_RESPONSE" ]; then
  echo "❌ CRITICAL FAILURE: Could not create courier."
  exit 1
fi

COURIER_ID=$(echo "$COURIER_RESPONSE" | jq -r '.id')
echo "✅ Courier created: $COURIER_ID"
echo "   Name: Geralt of Rivia"
echo ""

# Step 2: Create a Delivery (Draft)
echo "📦 Step 2: Drafting a delivery contract..."
DELIVERY_RESPONSE=$(curl -s -X POST "http://localhost:9999/api/v1/deliveries" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sender": {
      "zipCode": "12345-000",
      "street": "Rua do Remetente",
      "number": "10",
      "name": "Empresa A",
      "phone": "11999999999"
    },
    "recipient": {
      "zipCode": "54321-000",
      "street": "Avenida do Destinatário",
      "number": "20",
      "name": "Cliente B",
      "phone": "11888888888"
    },
    "items": [{ "name": "Silver Sword", "quantity": 1 }]
  }')

if [ "$DELIVERY_RESPONSE" = "null" ] || [ -z "$DELIVERY_RESPONSE" ]; then
  echo "❌ CRITICAL FAILURE: Could not create delivery."
  exit 1
fi

DELIVERY_ID=$(echo "$DELIVERY_RESPONSE" | jq -r '.id')
echo "✅ Delivery drafted: $DELIVERY_ID"
echo "   Status: DRAFT"
echo ""

# Step 3: Place the Delivery
echo "📋 Step 3: Posting contract on the notice board..."
curl -s -X POST "http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/placement" \
  -H "Authorization: Bearer $TOKEN" > /dev/null

if [ $? -ne 0 ]; then
  echo "❌ CRITICAL FAILURE: Could not place delivery."
  exit 1
fi

echo "✅ Delivery placed"
echo "   Status: WAITING_FOR_COURIER"
echo "   Event: DeliveryPlacedEvent → Kafka"
echo ""

# Step 4: Assign Courier (Pick Up)
echo "🛵 Step 4: Geralt accepts the contract..."
curl -s -X POST "http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/pickups" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"courierId\": \"$COURIER_ID\"}" > /dev/null

if [ $? -ne 0 ]; then
  echo "❌ CRITICAL FAILURE: Could not assign courier."
  exit 1
fi

echo "✅ Delivery picked up by Geralt"
echo "   Status: IN_TRANSIT"
echo "   Event: DeliveryPickedUpEvent → Kafka"
echo ""

# Step 5: Complete the Delivery
echo "🏆 Step 5: Contract fulfilled..."
curl -s -X POST "http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/completion" \
  -H "Authorization: Bearer $TOKEN" > /dev/null

if [ $? -ne 0 ]; then
  echo "❌ CRITICAL FAILURE: Could not complete delivery."
  exit 1
fi

echo "✅ Delivery completed"
echo "   Status: DELIVERED"
echo "   Event: DeliveryFulfilledEvent → Kafka"
echo ""

# Step 6: Check Final State
echo "🔍 Step 6: Inspecting the completed contract..."
FINAL_DELIVERY=$(curl -s "http://localhost:9999/api/v1/deliveries/$DELIVERY_ID" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$FINAL_DELIVERY" | jq -r '.status')
echo "   Final Status: $STATUS"
echo ""

echo "========================================"
echo "⚔️ Contract complete. Toss a coin to your witcher."
echo ""
echo "📊 View Kafka events at: http://localhost:8090"
echo "🗄️  View database at: http://localhost:5050"
echo "📋 View service registry at: http://localhost:8761"
echo "📉 View metrics dashboard at: http://localhost:3000"
echo "🔐 View Keycloak admin at: http://localhost:8082"
echo "🕵️‍♂️ View distributed traces at: http://localhost:16686"
