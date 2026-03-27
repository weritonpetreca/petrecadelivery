#!/bin/bash

echo "⚔️ PetrecaDelivery — Full Contract Test"
echo "========================================"
echo ""

# Step 1: Create a Courier (Geralt of Rivia)
echo "📍 Step 1: Recruiting a witcher..."
COURIER_RESPONSE=$(curl -s -X POST http://localhost:9999/api/v1/couriers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Geralt of Rivia",
    "phone": "11987654321"
  }')

COURIER_ID=$(echo $COURIER_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "✅ Courier created: $COURIER_ID"
echo "   Name: Geralt of Rivia"
echo ""

# Step 2: Create a Delivery (Draft)
echo "📦 Step 2: Drafting a delivery contract..."
DELIVERY_RESPONSE=$(curl -s -X POST http://localhost:9999/api/v1/deliveries \
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

DELIVERY_ID=$(echo $DELIVERY_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "✅ Delivery drafted: $DELIVERY_ID"
echo "   Status: DRAFT"
echo ""

# Step 3: Place the Delivery
echo "📋 Step 3: Posting contract on the notice board..."
curl -s -X POST http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/placement > /dev/null
echo "✅ Delivery placed"
echo "   Status: WAITING_FOR_COURIER"
echo "   Event: DeliveryPlacedEvent → Kafka"
echo ""

# Step 4: Assign Courier (Pick Up)
echo "🛵 Step 4: Geralt accepts the contract..."
curl -s -X POST http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/pickups \
  -H "Content-Type: application/json" \
  -d "{\"courierId\": \"$COURIER_ID\"}" > /dev/null
echo "✅ Delivery picked up by Geralt"
echo "   Status: IN_TRANSIT"
echo "   Event: DeliveryPickedUpEvent → Kafka"
echo ""

# Step 5: Complete the Delivery
echo "🏆 Step 5: Contract fulfilled..."
curl -s -X POST http://localhost:9999/api/v1/deliveries/$DELIVERY_ID/completion > /dev/null
echo "✅ Delivery completed"
echo "   Status: DELIVERED"
echo "   Event: DeliveryFulfilledEvent → Kafka"
echo ""

# Step 6: Check Final State
echo "🔍 Step 6: Inspecting the completed contract..."
FINAL_DELIVERY=$(curl -s http://localhost:9999/api/v1/deliveries/$DELIVERY_ID)
STATUS=$(echo "$FINAL_DELIVERY" | grep -o '"status":"[^"]*' | cut -d'"' -f4)
echo "   Final Status: $STATUS"
echo ""

echo "========================================"
echo "⚔️ Contract complete. Toss a coin to your witcher."
echo ""
echo "📊 View Kafka events at: http://localhost:8090"
echo "🗄️  View database at: http://localhost:5050"
echo "📋 View service registry at: http://localhost:8761"
echo "📉 View metrics dashboard at: http://localhost:3000"
