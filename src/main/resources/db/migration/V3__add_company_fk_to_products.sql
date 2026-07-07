-- ═══════════════════════════════════════════════════════════════
-- V3__add_company_fk_to_products.sql
-- Create company if not exists + add FK to products
-- Safe to run on both DEV (no company table) and PROD (company exists)
-- ═══════════════════════════════════════════════════════════════

-- Create company table only if it doesn't exist (DEV needs this, PROD skips it)
CREATE TABLE IF NOT EXISTS company (
                                       id                              BIGSERIAL    PRIMARY KEY,
                                       name                            VARCHAR(255) NOT NULL,
    modifiedby                      VARCHAR(255) NOT NULL,
    createdat                       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updatedat                       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    company_id_from_chatbot_api_key VARCHAR(255) NOT NULL,
    hashed_chatbot_api_key          VARCHAR(255) NOT NULL
    );

-- Resync sequence
SELECT setval(
               pg_get_serial_sequence('company', 'id'),
               COALESCE((SELECT MAX(id) FROM company), 1)
       );

-- Insert BikeXpert (idempotent)
INSERT INTO company (name, modifiedby, createdat, updatedat,
                     company_id_from_chatbot_api_key, hashed_chatbot_api_key)
SELECT 'BikeXpert', 'system', NOW(), NOW(), 'bikexpert', 'bikexpert'
    WHERE NOT EXISTS (SELECT 1 FROM company WHERE name = 'BikeXpert');

-- Add column as nullable first (IF NOT EXISTS = safe for PROD if already added)
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS company_id BIGINT;

-- Backfill existing products with BikeXpert
UPDATE products
SET company_id = (SELECT id FROM company WHERE name = 'BikeXpert')
WHERE company_id IS NULL;

-- Enforce NOT NULL
ALTER TABLE products
    ALTER COLUMN company_id SET NOT NULL;

-- Add FK constraint only if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_products_company'
        AND table_name = 'products'
    ) THEN
ALTER TABLE products
    ADD CONSTRAINT fk_products_company
        FOREIGN KEY (company_id)
            REFERENCES company(id)
            ON DELETE RESTRICT;
END IF;
END $$;
