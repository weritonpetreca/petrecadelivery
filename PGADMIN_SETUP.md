# pgAdmin Auto-Configuration

This project includes automatic pgAdmin server configuration.

## Files Created:
- `pgadmin-servers.json` - Server connection configuration
- `pgpass` - PostgreSQL password file (permissions: 600)

## What Happens Automatically:
When you run `docker-compose up -d`, pgAdmin will:
1. Start with the server "PetrecaDelivery" already registered
2. Automatically connect to PostgreSQL using the credentials in `pgpass`
3. Show both `courierdb` and `deliverydb` databases ready to explore

## Manual Setup (Not Needed):
If for some reason the automatic configuration doesn't work, you can manually register the server:
1. Open pgAdmin at http://localhost:5050
2. Right-click "Servers" → "Register" → "Server"
3. General tab: Name = "PetrecaDelivery"
4. Connection tab:
   - Host: postgres
   - Port: 5432
   - Username: postgres
   - Password: postgres
   - Save password: ✓

## Security Note:
The `pgpass` file contains the PostgreSQL password in plain text. This is acceptable for local development but should NEVER be used in production.
