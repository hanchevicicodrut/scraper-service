-- ═══════════════════════════════════════════════════════════════
-- V1__init.sql
-- Initial schema — tables only, indexes added later after analysis
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE scrape_runs (
                             id               BIGSERIAL       PRIMARY KEY,
                             source_website   VARCHAR(255)    NOT NULL,
                             scrape_url       VARCHAR(500),
                             status           VARCHAR(50)     NOT NULL,
                             started_at       TIMESTAMPTZ,
                             finished_at      TIMESTAMPTZ,
                             total_found      INTEGER         DEFAULT 0,
                             total_inserted   INTEGER         DEFAULT 0,
                             total_updated    INTEGER         DEFAULT 0,
                             total_unchanged  INTEGER         DEFAULT 0,
                             total_failed     INTEGER         DEFAULT 0,
                             error_message    TEXT,
                             created_at       TIMESTAMPTZ     DEFAULT NOW()
);

CREATE TABLE products (
                          id               BIGSERIAL       PRIMARY KEY,
                          sku              VARCHAR(255)    NOT NULL UNIQUE,
                          source_website   VARCHAR(255)    NOT NULL,
                          product_url      TEXT,
                          name             VARCHAR(500)    NOT NULL,
                          brand            VARCHAR(255),
                          category         VARCHAR(255),
                          subcategory      VARCHAR(255),
                          description      TEXT,
                          current_price    NUMERIC(10, 2),
                          original_price   NUMERIC(10, 2),
                          currency         VARCHAR(10),
                          discount         VARCHAR(50),
                          in_stock         BOOLEAN         DEFAULT true,
                          active           BOOLEAN         DEFAULT true,
                          available_sizes  JSONB,
                          available_colors JSONB,
                          image_url        TEXT,
                          attributes       JSONB,
                          first_scraped_at TIMESTAMPTZ     DEFAULT NOW(),
                          last_scraped_at  TIMESTAMPTZ     DEFAULT NOW(),
                          deactivated_at   TIMESTAMPTZ
);

CREATE TABLE product_price_history (
                                       id                BIGSERIAL       PRIMARY KEY,
                                       product_id        BIGINT          NOT NULL REFERENCES products(id),
                                       scrape_run_id     BIGINT          NOT NULL REFERENCES scrape_runs(id),
                                       price             NUMERIC(10, 2)  NOT NULL,
                                       original_price    NUMERIC(10, 2),
                                       currency          VARCHAR(10),
                                       in_stock          BOOLEAN,
                                       change_type       VARCHAR(50),
                                       previous_price    NUMERIC(10, 2),
                                       change_percentage NUMERIC(5, 2),
                                       scraped_at        TIMESTAMPTZ     DEFAULT NOW()
);

CREATE TABLE product_embeddings (
                                    id              BIGSERIAL       PRIMARY KEY,
                                    product_id      BIGINT          NOT NULL UNIQUE REFERENCES products(id),
                                    vector_json     TEXT,
                                    model_name      VARCHAR(255),
                                    model_provider  VARCHAR(255),
                                    dimensions      INTEGER,
                                    embedded_text   TEXT,
                                    created_at      TIMESTAMPTZ     DEFAULT NOW(),
                                    updated_at      TIMESTAMPTZ     DEFAULT NOW()
);
