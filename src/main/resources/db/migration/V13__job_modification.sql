SET @add_address_sql = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'jobs'
          AND column_name = 'address'
    ),
    'SELECT 1',
    'ALTER TABLE jobs ADD COLUMN address VARCHAR(255)'
);

PREPARE add_address_statement FROM @add_address_sql;
EXECUTE add_address_statement;
DEALLOCATE PREPARE add_address_statement;