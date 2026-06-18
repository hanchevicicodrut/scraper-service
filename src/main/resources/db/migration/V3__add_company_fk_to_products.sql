-- V3__add_company_fk_to_products.sql

-- 0. Resync the sequence so future inserts never collide with existing ids
SELECT setval(
               pg_get_serial_sequence('company', 'id'),
               COALESCE((SELECT MAX(id) FROM company), 1)
       );

-- 1. Insert BikeXpert (idempotent on name, not on id)
INSERT INTO company (name, modifiedby, createdat, updatedat,
                     company_id_from_chatbot_api_key, hashed_chatbot_api_key)
SELECT 'BikeXpert', 'system', NOW(), NOW(), 'bikexpert', 'bikexpert'
    WHERE NOT EXISTS (SELECT 1 FROM company WHERE name = 'BikeXpert');

-- 2. Add column as nullable first
ALTER TABLE products
    ADD COLUMN company_id BIGINT;

-- 3. Backfill existing products with BikeXpert
UPDATE products
SET company_id = (SELECT id FROM company WHERE name = 'BikeXpert')
WHERE company_id IS NULL;

-- 4. Enforce NOT NULL now that every row has a value
ALTER TABLE products
    ALTER COLUMN company_id SET NOT NULL;

-- 5. Add the foreign key, RESTRICT on delete
ALTER TABLE products
    ADD CONSTRAINT fk_products_company
        FOREIGN KEY (company_id)
            REFERENCES company(id)
            ON DELETE RESTRICT;
