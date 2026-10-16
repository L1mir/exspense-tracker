-- Read-only role for the PostgreSQL MCP server (AI agent): can read the schema and data, cannot change them.
-- Runs automatically when the docker-compose volume is created; for an existing volume run:
--   docker exec -i expense-tracker-postgres psql -U expense -d expense_tracker < docker/postgres/create-readonly-user.sql
-- Local development only. .mcp.json does not contain the password: it reads it from MCP_DB_PASSWORD.
CREATE ROLE expense_readonly WITH LOGIN PASSWORD 'readonly';
GRANT CONNECT ON DATABASE expense_tracker TO expense_readonly;
GRANT USAGE ON SCHEMA public TO expense_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO expense_readonly;
-- Tables created later by Flyway are readable too
ALTER DEFAULT PRIVILEGES FOR ROLE expense IN SCHEMA public GRANT SELECT ON TABLES TO expense_readonly;
