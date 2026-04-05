CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    email varchar UNIQUE NOT NULL,
    phone varchar UNIQUE,
    email_verified boolean DEFAULT false,
    phone_verified boolean DEFAULT false,
    status varchar NOT NULL CHECK (status IN ('PENDING_EMAIL', 'PENDING_PHONE', 'ACTIVE', 'SUSPENDED')),
    oauth_provider varchar,
    oauth_subject varchar,
    name varchar NOT NULL,
    avatar_url varchar,
    role varchar DEFAULT 'USER',
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz DEFAULT now()
);

CREATE TABLE businesses (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id uuid REFERENCES users(id) ON DELETE CASCADE,
    name varchar NOT NULL,
    description text,
    category varchar NOT NULL,
    address varchar,
    city varchar,
    phone varchar,
    email varchar,
    website varchar,
    status varchar DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    rejection_reason text,
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz DEFAULT now()
);

CREATE TABLE products (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id uuid REFERENCES businesses(id) ON DELETE CASCADE,
    name varchar NOT NULL,
    description text,
    price_range varchar,
    active boolean DEFAULT true,
    created_at timestamptz DEFAULT now()
);

CREATE TABLE reviews (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid REFERENCES users(id),
    business_id uuid REFERENCES businesses(id),
    product_id uuid REFERENCES products(id),
    rating smallint CHECK (rating BETWEEN 1 AND 5),
    body text NOT NULL,
    photos text[] DEFAULT '{}',
    status varchar DEFAULT 'ACTIVE',
    created_at timestamptz DEFAULT now()
);

CREATE TABLE review_reports (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id uuid REFERENCES reviews(id),
    reporter_id uuid REFERENCES users(id),
    reason varchar NOT NULL,
    status varchar DEFAULT 'PENDING',
    created_at timestamptz DEFAULT now()
);

CREATE INDEX idx_businesses_status ON businesses(status);
CREATE INDEX idx_reviews_business_id ON reviews(business_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_user_business ON reviews(user_id, business_id);
