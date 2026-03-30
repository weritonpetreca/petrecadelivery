#!/bin/bash

# --- The Witcher's Coordinates ---
CONTAINER_NAME="petrecadelivery-keycloak"
REALM_NAME="petreca-realm" # Ensure this perfectly matches your realm!
USERNAME="geralt"
PASSWORD="witcher123"
ROLE="COURIER"

echo "🐺 Step 1: Authenticating to the Keycloak Master Realm..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password admin

echo "⚔️ Step 2: Creating user '$USERNAME' with full profile data..."
# By providing all data, Keycloak will NOT trigger the "Update Profile" Required Action
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh create users \
  -r $REALM_NAME \
  -s username=$USERNAME \
  -s enabled=true \
  -s firstName="Geralt" \
  -s lastName="of Rivia" \
  -s email="geralt@kaermorhen.com" \
  -s emailVerified=true

echo "🛡️ Step 3: Forging the permanent password..."
# kcadm.sh set-password creates a PERMANENT password by default.
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh set-password \
  -r $REALM_NAME \
  --username $USERNAME \
  --new-password $PASSWORD

echo "🪄 Step 4: Assigning the '$ROLE' role to the user..."
docker exec $CONTAINER_NAME /opt/keycloak/bin/kcadm.sh add-roles \
  -r $REALM_NAME \
  --uusername $USERNAME \
  --rolename $ROLE

echo "✅ Contract complete! The Witcher is fully equipped."