-- V3__insert_magazinul_de_biciclete.sql
INSERT INTO company (name, modifiedby, createdat, updatedat,
                     company_id_from_chatbot_api_key, hashed_chatbot_api_key)
SELECT 'MagazinulDeBiciclete', 'system', NOW(), NOW(),
       'magazinuldebiciclete', 'magazinuldebiciclete'
    WHERE NOT EXISTS (SELECT 1 FROM company WHERE name = 'MagazinulDeBiciclete');
