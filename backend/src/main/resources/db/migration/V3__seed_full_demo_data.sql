-- Seed persistente de datos de prueba para cubrir todos los flujos de la app.
-- Se ejecuta una sola vez con Flyway y deja los datos guardados en PostgreSQL.

-- ─────────────────────────────────────────────────────────────────────────────
-- 1) USUARIOS DE PRUEBA
-- Passwords conocidas:
--   Admin12345!  -> hash admin
--   Owner12345!  -> hash owner
--   Client12345! -> hash client
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO users (
    id, email, phone, email_verified, phone_verified, status,
    oauth_provider, oauth_subject, name, avatar_url, role, password_hash,
    created_at, updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000001'::uuid,
    'admin@resena.local',
    '+573000000001',
    TRUE, TRUE, 'ACTIVE',
    'LOCAL', NULL, 'Administrador Demo', NULL, 'ADMIN',
    '$2a$10$y6TgGGpjwTeTC008uD7LceZSoH6wCr3vwQ7R.rgowD2IS6V/AhTEC',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@resena.local');

INSERT INTO users (
    id, email, phone, email_verified, phone_verified, status,
    oauth_provider, oauth_subject, name, avatar_url, role, password_hash,
    created_at, updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000011'::uuid,
    'dueno1@resena.local',
    '+573000000011',
    TRUE, TRUE, 'ACTIVE',
    'LOCAL', NULL, 'Carlos Propietario', NULL, 'USER',
    '$2a$10$5eOhSr4W3KRrzwgSr5ZaLe8EvGCdZ5yTW34nUf4ci54/iamP5H8.2',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'dueno1@resena.local');

INSERT INTO users (
    id, email, phone, email_verified, phone_verified, status,
    oauth_provider, oauth_subject, name, avatar_url, role, password_hash,
    created_at, updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000012'::uuid,
    'dueno2@resena.local',
    '+573000000012',
    TRUE, TRUE, 'ACTIVE',
    'LOCAL', NULL, 'Maria Empresaria', NULL, 'USER',
    '$2a$10$5eOhSr4W3KRrzwgSr5ZaLe8EvGCdZ5yTW34nUf4ci54/iamP5H8.2',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'dueno2@resena.local');

INSERT INTO users (
    id, email, phone, email_verified, phone_verified, status,
    oauth_provider, oauth_subject, name, avatar_url, role, password_hash,
    created_at, updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000021'::uuid,
    'cliente1@resena.local',
    '+573000000021',
    TRUE, TRUE, 'ACTIVE',
    'LOCAL', NULL, 'Ana Garcia', NULL, 'USER',
    '$2a$10$cLZLiqpd1fzN/uLgBgYxM.MoxhsRGqAWldO7TufewU5lb5fZoKe82',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'cliente1@resena.local');

INSERT INTO users (
    id, email, phone, email_verified, phone_verified, status,
    oauth_provider, oauth_subject, name, avatar_url, role, password_hash,
    created_at, updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000022'::uuid,
    'cliente2@resena.local',
    '+573000000022',
    TRUE, TRUE, 'ACTIVE',
    'LOCAL', NULL, 'Pedro Lopez', NULL, 'USER',
    '$2a$10$cLZLiqpd1fzN/uLgBgYxM.MoxhsRGqAWldO7TufewU5lb5fZoKe82',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'cliente2@resena.local');

INSERT INTO users (
    id, email, phone, email_verified, phone_verified, status,
    oauth_provider, oauth_subject, name, avatar_url, role, password_hash,
    created_at, updated_at
)
SELECT
    '00000000-0000-0000-0000-000000000023'::uuid,
    'cliente3@resena.local',
    '+573000000023',
    TRUE, TRUE, 'ACTIVE',
    'LOCAL', NULL, 'Sofia Martinez', NULL, 'USER',
    '$2a$10$cLZLiqpd1fzN/uLgBgYxM.MoxhsRGqAWldO7TufewU5lb5fZoKe82',
    NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'cliente3@resena.local');

-- ─────────────────────────────────────────────────────────────────────────────
-- 2) NEGOCIOS
-- 5 APPROVED + 2 PENDING + 1 REJECTED
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO businesses (
    id, owner_id, name, description, category, address, city, phone, email, website,
    status, rejection_reason, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000001'::uuid,
    (SELECT id FROM users WHERE email = 'dueno1@resena.local'),
    'La Terraza Gourmet',
    'Restaurante de cocina fusion colombiana con menu premium y cocteleria artesanal.',
    'Restaurante',
    'Carrera 7 #45-12', 'Bogota', '+573151001001', 'terraza@resena.local', 'https://laterraza.example.com',
    'APPROVED', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM businesses WHERE name = 'La Terraza Gourmet');

INSERT INTO businesses (
    id, owner_id, name, description, category, address, city, phone, email, website,
    status, rejection_reason, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000002'::uuid,
    (SELECT id FROM users WHERE email = 'dueno1@resena.local'),
    'Tienda Digital XYZ',
    'Venta de tecnologia y accesorios con envios nacionales y soporte post-venta.',
    'Tienda',
    'Centro Comercial El Tesoro Local 204', 'Medellin', '+573151002002', 'tienda@resena.local', 'https://tiendaxyz.example.com',
    'APPROVED', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM businesses WHERE name = 'Tienda Digital XYZ');

INSERT INTO businesses (
    id, owner_id, name, description, category, address, city, phone, email, website,
    status, rejection_reason, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000003'::uuid,
    (SELECT id FROM users WHERE email = 'dueno2@resena.local'),
    'Centro Medico Vida Plena',
    'Clinica ambulatoria con medicina general, odontologia y servicios de laboratorio.',
    'Salud',
    'Avenida 6N #28-15', 'Cali', '+573151003003', 'medico@resena.local', NULL,
    'APPROVED', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM businesses WHERE name = 'Centro Medico Vida Plena');

INSERT INTO businesses (
    id, owner_id, name, description, category, address, city, phone, email, website,
    status, rejection_reason, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000004'::uuid,
    (SELECT id FROM users WHERE email = 'dueno2@resena.local'),
    'Salon Belleza Iris',
    'Servicio de colorimetria, corte y tratamientos capilares con productos premium.',
    'Belleza',
    'Calle 85 #11-64', 'Bogota', '+573151004004', 'iris@resena.local', NULL,
    'APPROVED', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM businesses WHERE name = 'Salon Belleza Iris');

INSERT INTO businesses (
    id, owner_id, name, description, category, address, city, phone, email, website,
    status, rejection_reason, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000005'::uuid,
    (SELECT id FROM users WHERE email = 'dueno1@resena.local'),
    'Academia Tech Skills',
    'Formacion en programacion, UX/UI y marketing digital con modalidad hibrida.',
    'Educacion',
    'Carrera 15 #93-47 Piso 3', 'Bogota', '+573151005005', 'academia@resena.local', 'https://techskills.example.com',
    'APPROVED', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM businesses WHERE name = 'Academia Tech Skills');

INSERT INTO businesses (
    id, owner_id, name, description, category, address, city, phone, email, website,
    status, rejection_reason, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000006'::uuid,
    (SELECT id FROM users WHERE email = 'dueno2@resena.local'),
    'Panaderia El Trigo Dorado',
    'Panaderia artesanal con recetas tradicionales y horno de lena.',
    'Restaurante',
    'Calle 10 #2-34', 'Manizales', '+573151006006', 'trigo@resena.local', NULL,
    'PENDING', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM businesses WHERE name = 'Panaderia El Trigo Dorado');

INSERT INTO businesses (
    id, owner_id, name, description, category, address, city, phone, email, website,
    status, rejection_reason, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000007'::uuid,
    (SELECT id FROM users WHERE email = 'dueno1@resena.local'),
    'Gym PowerFit Centro',
    'Gimnasio con maquinas de ultima generacion y clases grupales.',
    'Servicio',
    'Avenida El Dorado #65-23', 'Bogota', '+573151007007', 'powerfit@resena.local', NULL,
    'PENDING', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM businesses WHERE name = 'Gym PowerFit Centro');

INSERT INTO businesses (
    id, owner_id, name, description, category, address, city, phone, email, website,
    status, rejection_reason, created_at, updated_at
)
SELECT
    '10000000-0000-0000-0000-000000000008'::uuid,
    (SELECT id FROM users WHERE email = 'dueno2@resena.local'),
    'Empresa Fantasma SAS',
    'Descripcion incompleta.',
    'Otro',
    NULL, 'Bogota', NULL, NULL, NULL,
    'REJECTED', 'Informacion insuficiente y datos de contacto invalidos.', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM businesses WHERE name = 'Empresa Fantasma SAS');

-- ─────────────────────────────────────────────────────────────────────────────
-- 3) PRODUCTOS
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000001'::uuid,
       (SELECT id FROM businesses WHERE name = 'La Terraza Gourmet'),
       'Lomo al Carbon',
       'Filete de res 300g con chimichurri y papa criolla.',
       '$65,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000001'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000002'::uuid,
       (SELECT id FROM businesses WHERE name = 'La Terraza Gourmet'),
       'Ceviche de Camaron',
       'Camaron fresco marinado en limon con aguacate.',
       '$48,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000002'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000003'::uuid,
       (SELECT id FROM businesses WHERE name = 'Tienda Digital XYZ'),
       'Audifonos Bluetooth Pro',
       'Cancelacion de ruido activa, 30h de bateria.',
       '$280,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000003'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000004'::uuid,
       (SELECT id FROM businesses WHERE name = 'Tienda Digital XYZ'),
       'Cable USB-C Reforzado',
       '2 metros, trenzado en nylon, carga rapida 65W.',
       '$35,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000004'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000005'::uuid,
       (SELECT id FROM businesses WHERE name = 'Centro Medico Vida Plena'),
       'Consulta Medicina General',
       'Atencion medica completa con historia clinica.',
       '$80,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000005'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000006'::uuid,
       (SELECT id FROM businesses WHERE name = 'Centro Medico Vida Plena'),
       'Limpieza Dental Profesional',
       'Profilaxis con ultrasonido y revision odontologica.',
       '$95,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000006'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000007'::uuid,
       (SELECT id FROM businesses WHERE name = 'Salon Belleza Iris'),
       'Coloracion Completa',
       'Tinte profesional con tratamiento hidratante.',
       '$180,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000007'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000008'::uuid,
       (SELECT id FROM businesses WHERE name = 'Salon Belleza Iris'),
       'Corte + Blow Dry',
       'Corte personalizado y secado profesional.',
       '$85,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000008'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000009'::uuid,
       (SELECT id FROM businesses WHERE name = 'Academia Tech Skills'),
       'Bootcamp Full Stack JS',
       '12 semanas intensivas: frontend, backend y despliegue.',
       '$3,500,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000009'::uuid);

INSERT INTO products (id, business_id, name, description, price_range, active, created_at)
SELECT '20000000-0000-0000-0000-000000000010'::uuid,
       (SELECT id FROM businesses WHERE name = 'Academia Tech Skills'),
       'Curso UX/UI Design',
       'Figma, investigacion de usuario y prototipado.',
       '$2,200,000', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM products WHERE id = '20000000-0000-0000-0000-000000000010'::uuid);

-- ─────────────────────────────────────────────────────────────────────────────
-- 4) RESEÑAS
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000001'::uuid,
    (SELECT id FROM users WHERE email = 'cliente1@resena.local'),
    (SELECT id FROM businesses WHERE name = 'La Terraza Gourmet'),
    (SELECT id FROM products WHERE name = 'Lomo al Carbon' AND business_id = (SELECT id FROM businesses WHERE name = 'La Terraza Gourmet')),
    5,
    'Increible experiencia, carne al punto y servicio impecable. Volveria sin dudarlo.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000001'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000002'::uuid,
    (SELECT id FROM users WHERE email = 'cliente2@resena.local'),
    (SELECT id FROM businesses WHERE name = 'La Terraza Gourmet'),
    (SELECT id FROM products WHERE name = 'Ceviche de Camaron' AND business_id = (SELECT id FROM businesses WHERE name = 'La Terraza Gourmet')),
    4,
    'Buen lugar para cenar, la comida muy fresca aunque en hora pico demoran un poco.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000002'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000003'::uuid,
    (SELECT id FROM users WHERE email = 'cliente3@resena.local'),
    (SELECT id FROM businesses WHERE name = 'Tienda Digital XYZ'),
    (SELECT id FROM products WHERE name = 'Audifonos Bluetooth Pro' AND business_id = (SELECT id FROM businesses WHERE name = 'Tienda Digital XYZ')),
    5,
    'Los audifonos suenan excelente y la bateria dura bastante. Recomendados.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000003'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000004'::uuid,
    (SELECT id FROM users WHERE email = 'cliente1@resena.local'),
    (SELECT id FROM businesses WHERE name = 'Tienda Digital XYZ'),
    (SELECT id FROM products WHERE name = 'Cable USB-C Reforzado' AND business_id = (SELECT id FROM businesses WHERE name = 'Tienda Digital XYZ')),
    3,
    'El cable cumple, pero la atencion en caja fue muy lenta.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000004'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000005'::uuid,
    (SELECT id FROM users WHERE email = 'cliente2@resena.local'),
    (SELECT id FROM businesses WHERE name = 'Centro Medico Vida Plena'),
    (SELECT id FROM products WHERE name = 'Consulta Medicina General' AND business_id = (SELECT id FROM businesses WHERE name = 'Centro Medico Vida Plena')),
    5,
    'Atencion muy profesional, instalaciones limpias y resultado rapido de examenes.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000005'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000006'::uuid,
    (SELECT id FROM users WHERE email = 'cliente3@resena.local'),
    (SELECT id FROM businesses WHERE name = 'Centro Medico Vida Plena'),
    (SELECT id FROM products WHERE name = 'Limpieza Dental Profesional' AND business_id = (SELECT id FROM businesses WHERE name = 'Centro Medico Vida Plena')),
    4,
    'Muy buena limpieza dental, la especialista explico todo con detalle.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000006'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000007'::uuid,
    (SELECT id FROM users WHERE email = 'cliente2@resena.local'),
    (SELECT id FROM businesses WHERE name = 'Salon Belleza Iris'),
    (SELECT id FROM products WHERE name = 'Coloracion Completa' AND business_id = (SELECT id FROM businesses WHERE name = 'Salon Belleza Iris')),
    5,
    'El color quedo perfecto y el trato fue excelente de principio a fin.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000007'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000008'::uuid,
    (SELECT id FROM users WHERE email = 'cliente3@resena.local'),
    (SELECT id FROM businesses WHERE name = 'Salon Belleza Iris'),
    (SELECT id FROM products WHERE name = 'Corte + Blow Dry' AND business_id = (SELECT id FROM businesses WHERE name = 'Salon Belleza Iris')),
    3,
    'Buen resultado final pero me atendieron con retraso.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000008'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000009'::uuid,
    (SELECT id FROM users WHERE email = 'cliente1@resena.local'),
    (SELECT id FROM businesses WHERE name = 'Academia Tech Skills'),
    (SELECT id FROM products WHERE name = 'Bootcamp Full Stack JS' AND business_id = (SELECT id FROM businesses WHERE name = 'Academia Tech Skills')),
    4,
    'Buen contenido tecnico y mentores con experiencia real en industria.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000009'::uuid);

INSERT INTO reviews (id, user_id, business_id, product_id, rating, body, photos, status, created_at)
SELECT
    '30000000-0000-0000-0000-000000000010'::uuid,
    (SELECT id FROM users WHERE email = 'cliente3@resena.local'),
    (SELECT id FROM businesses WHERE name = 'Academia Tech Skills'),
    (SELECT id FROM products WHERE name = 'Curso UX/UI Design' AND business_id = (SELECT id FROM businesses WHERE name = 'Academia Tech Skills')),
    2,
    'Esperaba un nivel mas avanzado, faltaron ejercicios practicos.',
    ARRAY[]::text[],
    'ACTIVE',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE id = '30000000-0000-0000-0000-000000000010'::uuid);

-- ─────────────────────────────────────────────────────────────────────────────
-- 5) REPORTES DE RESEÑAS (flujo de moderacion)
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO review_reports (id, review_id, reporter_id, reason, status, created_at)
SELECT
    '40000000-0000-0000-0000-000000000001'::uuid,
    (SELECT id FROM reviews WHERE id = '30000000-0000-0000-0000-000000000010'::uuid),
    (SELECT id FROM users WHERE email = 'cliente2@resena.local'),
    'Contenido potencialmente ofensivo, por favor revisar por moderacion.',
    'PENDING',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM review_reports WHERE id = '40000000-0000-0000-0000-000000000001'::uuid);

INSERT INTO review_reports (id, review_id, reporter_id, reason, status, created_at)
SELECT
    '40000000-0000-0000-0000-000000000002'::uuid,
    (SELECT id FROM reviews WHERE id = '30000000-0000-0000-0000-000000000004'::uuid),
    (SELECT id FROM users WHERE email = 'cliente3@resena.local'),
    'Parece no corresponder con la experiencia real del producto.',
    'PENDING',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM review_reports WHERE id = '40000000-0000-0000-0000-000000000002'::uuid);
