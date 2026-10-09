-- Idempotent schema, executed by Spring Boot SQL initialization on every startup.
-- Several replicas may start at the same time; the advisory lock serialises them because concurrent
-- "CREATE TABLE IF NOT EXISTS" statements can still collide in the PostgreSQL catalog.
SELECT pg_advisory_lock(727274);

CREATE TABLE IF NOT EXISTS franchise
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_franchise PRIMARY KEY (id),
    CONSTRAINT uk_franchise_name UNIQUE (name)
);

-- The unique index (franchise_id, name) also serves every lookup by franchise_id (leftmost column).
CREATE TABLE IF NOT EXISTS branch
(
    id           UUID         NOT NULL DEFAULT gen_random_uuid(),
    franchise_id UUID         NOT NULL,
    name         VARCHAR(100) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_branch PRIMARY KEY (id),
    CONSTRAINT fk_branch_franchise FOREIGN KEY (franchise_id) REFERENCES franchise (id) ON DELETE CASCADE,
    CONSTRAINT uk_branch_franchise_name UNIQUE (franchise_id, name)
);

CREATE TABLE IF NOT EXISTS product
(
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    branch_id  UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    stock      INTEGER      NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_product PRIMARY KEY (id),
    CONSTRAINT fk_product_branch FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE CASCADE,
    CONSTRAINT uk_product_branch_name UNIQUE (branch_id, name),
    CONSTRAINT ck_product_stock_non_negative CHECK (stock >= 0)
);

SELECT pg_advisory_unlock(727274);
