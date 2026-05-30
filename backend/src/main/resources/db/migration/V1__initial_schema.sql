CREATE TABLE users (
    id             UUID          NOT NULL,
    email          VARCHAR(255)  NOT NULL,
    phone          VARCHAR(50),
    email_verified BOOLEAN       NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN       NOT NULL DEFAULT FALSE,
    status         VARCHAR(50)   NOT NULL,
    oauth_provider VARCHAR(50),
    oauth_subject  VARCHAR(255),
    name           VARCHAR(255)  NOT NULL,
    avatar_url     VARCHAR(500),
    role           VARCHAR(50)   NOT NULL DEFAULT 'USER',
    created_at     TIMESTAMPTZ,
    updated_at     TIMESTAMPTZ,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone)
);

CREATE TABLE businesses (
    id               UUID          NOT NULL,
    owner_id         UUID,
    name             VARCHAR(255)  NOT NULL,
    description      TEXT,
    category         VARCHAR(100)  NOT NULL,
    address          VARCHAR(500),
    city             VARCHAR(100),
    phone            VARCHAR(50),
    email            VARCHAR(255),
    website          VARCHAR(500),
    status           VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    created_at       TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ,
    CONSTRAINT pk_businesses PRIMARY KEY (id),
    CONSTRAINT fk_businesses_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE products (
    id          UUID         NOT NULL,
    business_id UUID,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price_range VARCHAR(100),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_business FOREIGN KEY (business_id) REFERENCES businesses (id)
);

CREATE TABLE reviews (
    id          UUID        NOT NULL,
    user_id     UUID,
    business_id UUID,
    product_id  UUID,
    rating      SMALLINT,
    body        TEXT        NOT NULL,
    photos      TEXT[],
    status      VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ,
    CONSTRAINT pk_reviews PRIMARY KEY (id),
    CONSTRAINT fk_reviews_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_reviews_business FOREIGN KEY (business_id) REFERENCES businesses (id),
    CONSTRAINT fk_reviews_product  FOREIGN KEY (product_id)  REFERENCES products (id)
);

CREATE TABLE review_reports (
    id          UUID          NOT NULL,
    review_id   UUID,
    reporter_id UUID,
    reason      VARCHAR(1000) NOT NULL,
    status      VARCHAR(50)   DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ,
    CONSTRAINT pk_review_reports PRIMARY KEY (id),
    CONSTRAINT fk_review_reports_review   FOREIGN KEY (review_id)   REFERENCES reviews (id),
    CONSTRAINT fk_review_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id)
);
