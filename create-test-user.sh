#!/bin/bash

# --- The Witcher's Coordinates ---
CONTAINER_NAME="petrecadelivery-keycloak"
REALM_NAME="petreca-realm"
USERNAME="geralt"
PASSWORD="witcher123"
ROLE="COURIER"

# =========================================================
# 🪄 THE GRANDMASTER'S RUNES: Reusable Validation
# =========================================================
verify_execution() {
  local EXIT_CODE=$1
  local ERROR_MESSAGE=$2
  if [ $EXIT_CODE -ne 0 ]; then
    echo "❌ CRITICAL FAILURE: $ERROR_MESSAGE"
    exit 1
  fi
}

echo "================================================"
echo "🐺 KEYCLOAK FORGE - USER & ROLE PROVISIONING"
echo "================================================"

echo "⏳ Step 1: Waiting for the Emperor's Gates (Keycloak) to open..."
MAX_RETRIES=15
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  # We send the output to /dev/null to keep the terminal clean while waiting
  docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh config credentials \
    --server http://localhost:8080 \
    --realm master \
    --user admin \
    --password admin > /dev/null 2>&1

  if [ $? -eq 0 ]; then
    echo "✅ Keycloak is awake and authentication was successful!"
    break
  fi

  echo "   ... still waiting for Keycloak to boot (Attempt $((RETRY_COUNT+1))/$MAX_RETRIES)"
  sleep 3
  RETRY_COUNT=$((RETRY_COUNT+1))
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
  echo "❌ CRITICAL FAILURE: Keycloak did not wake up in time. Check Docker logs."
  exit 1
fi
echo "------------------------------------------------"

echo "⚔️ Step 2: Creating user '$USERNAME' with full profile data..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh create users \
  -r $REALM_NAME \
  -s username=$USERNAME \
  -s enabled=true \
  -s firstName="Geralt" \
  -s lastName="of Rivia" \
  -s email="geralt@kaermorhen.com" \
  -s emailVerified=true 2> /dev/null || echo "⚠️ User might already exist, proceeding..."

echo "🛡️ Step 3: Forging the permanent password..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh set-password \
  -r $REALM_NAME \
  --username $USERNAME \
  --new-password $PASSWORD 2> /dev/null
verify_execution $? "Failed to set the permanent password."

echo "📜 Step 4: Forging the '$ROLE' role in the realm..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh create roles \
  -r $REALM_NAME \
  -s name=$ROLE 2> /dev/null || echo "⚠️ Role might already exist, proceeding..."

echo "🪄 Step 5: Assigning the '$ROLE' role to the user..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh add-roles \
  -r $REALM_NAME \
  --uusername $USERNAME \
  --rolename $ROLE 2> /dev/null
verify_execution $? "Failed to assign the $ROLE role to $USERNAME."

echo "================================================"
echo "✅ Contract complete! The Witcher is fully equipped."