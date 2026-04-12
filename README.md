# ReseñaApp 🌟

Plataforma de reseñas de negocios construida con **Vue 3 + Spring Boot** y desplegada con **Docker + Dokploy**.

## 📋 Tabla de Contenidos

- [Características](#características)
- [Tecnología](#tecnología)
- [Inicio Rápido](#inicio-rápido)
- [Estructura](#estructura)
- [Flujo Git](#flujo-git)
- [Desarrollo](#desarrollo)
- [Despliegue](#despliegue)
- [API Endpoints](#api-endpoints)

## ✨ Características

- ✅ Autenticación con email/password y OAuth2 (Google, Facebook)
- ✅ Reseñas y calificaciones de negocios
- ✅ Dashboard para propietarios de negocios
- ✅ Panel de administración para moderación
- ✅ API REST completamente documentada
- ✅ Multi-tenant con roles (ADMIN, OWNER, USER)
- ✅ Health checks y monitoring integrados
- ✅ CI/CD con GitHub Actions
- ✅ Despliegue en Docker + Dokploy

## 🛠 Tecnología

### Backend
- **Framework**: Spring Boot 3.2 (Java 21)
- **ORM**: JPA/Hibernate 6
- **BD**: PostgreSQL 16
- **Seguridad**: Spring Security + JWT + OAuth2
- **Build**: Maven 3.9
- **Migrations**: Flyway

### Frontend
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite
- **Lenguaje**: TypeScript
- **Estado**: Pinia
- **Routing**: Vue Router
- **HTTP**: Axios

### DevOps
- **Containerización**: Docker (multi-stage)
- **Orquestación**: Docker Compose
- **CI/CD**: GitHub Actions
- **Despliegue**: Dokploy

## 🚀 Inicio Rápido

### Prerequisitos
- Node.js 22+
- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- Git

### Desarrollo Local

```bash
# 1. Clonar repositorio
git clone https://github.com/tu-org/ResenaApp.git
cd ResenaApp

# 2. Copiar archivo de configuración
cp .env.example .env

# 3. Iniciar con Docker Compose (recomendado)
docker compose up -d

# 4. Acceder a la aplicación
# Frontend: http://localhost:8080
# API: http://localhost:8080/api
# Admin: http://localhost:8080/admin

# Credenciales de prueba
# Email: admin@resena.local
# Contraseña: Admin12345!
```

### Desarrollo sin Docker

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend (en otra terminal)
cd frontend
npm install
npm run dev
```

## 📁 Estructura

```
ReseñaApp/
├── .github/
│   ├── workflows/              # GitHub Actions CI/CD
│   │   ├── ci-build.yml
│   │   ├── docker-build.yml
│   │   ├── deploy-to-dokploy.yml
│   │   └── ...
│   └── copilot-instructions.md
├── backend/                    # Spring Boot API
│   ├── src/
│   │   ├── main/java/com/resenias/reviews/
│   │   │   ├── controller/     # REST endpoints
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # Data access
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── dto/            # Data transfer objects
│   │   │   ├── config/         # Configuration
│   │   │   ├── security/       # JWT, OAuth2
│   │   │   └── mapper/         # Entity mappers
│   │   └── resources/
│   │       ├── db/migration/   # Flyway migrations
│   │       └── application*.yml
│   └── pom.xml
├── frontend/                   # Vue 3 SPA
│   ├── src/
│   │   ├── components/         # Vue components
│   │   ├── views/              # Page components
│   │   ├── router/             # Vue Router config
│   │   ├── stores/             # Pinia stores
│   │   ├── api/                # Axios client
│   │   ├── types/              # TypeScript types
│   │   └── main.ts
│   ├── vite.config.ts
│   └── package.json
├── Dockerfile                  # Multi-stage build
├── docker-compose.yml          # Dev environment
├── docker-compose.prod.yml     # Prod environment
├── .env.example                # Configuration template
├── DOKPLOY.md                  # Despliegue guide
└── README.md                   # Este archivo
```

## 🔄 Flujo Git

Este proyecto sigue **GitFlow** con las siguientes ramas:

```
┌─ master (PRODUCCIÓN)
│  ↑
├─ release (STAGING)
│  ↑
├─ qa (TESTING)
│  ↑
└─ dev (DESARROLLO)
   ↑
   ├─ feature/auth-google
   ├─ feature/products-crud
   └─ feature/admin-dashboard
```

### Workflow Recomendado

```bash
# 1. Crear rama feature
git checkout -b feature/mi-feature dev

# 2. Desarrollar y commitear
git add .
git commit -m "feat: description"

# 3. Push y abrir PR (feature → dev)
git push origin feature/mi-feature

# 4. Después de aprobación: merge automático a dev
# 5. Promoviones: dev → release → qa → master
```

Ver [.github/copilot-instructions.md](.github/copilot-instructions.md) para detalles.

## 💻 Desarrollo

### Backend - Ejecutar Tests

```bash
cd backend
mvn test
mvn verify
```

### Backend - Compilar JAR

```bash
cd backend
mvn clean package
```

### Frontend - Desarrollo

```bash
cd frontend
npm install
npm run dev      # Servidor de desarrollo
npm run build    # Compilar para producción
npm run lint     # Linter
npm run preview  # Preview de build
```

### Database Migrations

Las migraciones Flyway se ejecutan automáticamente al iniciar la app:

```bash
# Ver migraciones disponibles
ls backend/src/main/resources/db/migration/

# Crear nueva migración
touch backend/src/main/resources/db/migration/V3__new_table.sql
```

## 🌐 Múltiples Ambientes

ReseñaApp está configurado para correr en múltiples ambientes:

| Ambiente | Frontend | Backend | Rama | Acceso |
|----------|----------|---------|------|--------|
| **Development** | localhost:5173 | localhost:8080 | local | Developers |
| **Staging** | staging.resena.vercel.app | [Dokploy] | `release` | QA & Partners |
| **Production** | resena.com | [Dokploy] | `master` | Public |

### Desarrollo Local (Todos los servicios)

```bash
# 1. Copiar .env
cp .env.example .env

# 2. Levantar ambos servicios con Docker
docker compose up -d

# 3. Frontend en otra terminal
cd frontend
npm install
npm run dev

# 4. Acceder a http://localhost:5173
```

### Staging (Vercel + Dokploy)

```bash
# Push a rama release (auto-deploy)
git push origin release
  ↓
# Vercel auto-despliega: staging.resena.vercel.app
# Dokploy auto-despliega backend
```

### Producción (Vercel + Dokploy)

```bash
# Push a rama master (auto-deploy)
git push origin master
  ↓
# Vercel auto-despliega: resena.com
# Dokploy auto-despliega backend
```

### Scripts de Configuración de Ambiente

```bash
# Bash (Linux/Mac)
source scripts/env-setup.sh staging
source scripts/env-setup.sh production

# PowerShell (Windows)
. scripts\env-setup.ps1 -Environment staging
. scripts\env-setup.ps1 -Environment production
```

Configure las variables VITE_* según ambiente.

### Docker Local

```bash
# Construir imagen
docker build -t resena-app:latest .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DB_USER=resena \
  -e DB_PASSWORD=changeme \
  -e SPRING_PROFILES_ACTIVE=docker \
  resena-app:latest
```

### Docker Compose (Recomendado)

```bash
# Desarrollo
docker compose up --build

# Producción
docker compose -f docker-compose.prod.yml up -d
```

### Dokploy

Ver [DOKPLOY.md](DOKPLOY.md) para guía completa de despliegue en Dokploy.

**Resumen:**
1. Conectar repositorio GitHub a Dokploy
2. Configurar variables de entorno
3. Configurar dominio y SSL
4. Auto-deploy en cada push a rama configurada

### Vercel (Frontend)

Ver [VERCEL.md](VERCEL.md) para guía completa de despliegue en Vercel.

**Resumen:**
1. Crear cuenta en vercel.com
2. Conectar repositorio GitHub (frontend se despliega automáticamente)
3. Configurar dominios custom
4. Variables de entorno por ambiente (dev, staging, prod)

**Arquitectura:**
- Frontend: Vercel (Vue 3 + Vite)
- Backend: Dokploy (Spring Boot Java)
- Database: PostgreSQL en Dokploy

## 📡 API Endpoints

### Autenticación

```bash
# Login con email/password
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

# Obtener perfil actual
GET /api/auth/me

# Logout (cliente: eliminar token del localStorage)
```

### Negocios (públicos)

```bash
# Listar negocios aprobados con filtros
GET /api/businesses?search=cafe&city=Madrid&category=Restaurante&page=0&size=10

# Obtener detalle de negocio
GET /api/businesses/{id}

# Obtener mi negocio (autenticado)
GET /api/businesses/me

# Crear nuevo negocio (autenticado)
POST /api/businesses
{
  "name": "Café Demo",
  "description": "Descripción",
  "category": "Restaurante",
  "address": "Calle Principal 123",
  "city": "Madrid",
  "phone": "+34 901 234 567",
  "email": "info@cafe.com",
  "website": "https://cafe.com"
}

# Actualizar mi negocio (autenticado)
PUT /api/businesses/{id}
```

### Reseñas

```bash
# Listar reseñas por negocio
GET /api/reviews?businessId={id}&page=0&size=10

# Obtener estadísticas de reseñas
GET /api/reviews/stats?businessId={id}
```

### Productos

```bash
# Listar productos por negocio
GET /api/products?businessId={id}

# Crear producto (autenticado)
POST /api/products
{
  "businessId": "{id}",
  "name": "Café Americano",
  "description": "Café puro",
  "priceRange": "$3-5"
}

# Actualizar producto (autenticado)
PUT /api/products/{id}
{
  "active": true
}
```

### Admin

```bash
# Listar negocios pendientes (admin only)
GET /api/admin/businesses/pending?page=0&size=10

# Aprobar negocio (admin only)
POST /api/admin/businesses/{id}/approve

# Rechazar negocio (admin only)
POST /api/admin/businesses/{id}/reject?reason=Información%20incompleta
```

## 👥 Roles y Permisos

| Rol | Permisos |
|-----|----------|
| **ANONYMOUS** | Leer negocios y reseñas |
| **USER** | + Crear reseñas, perfil |
| **OWNER** | + Crear/editar negocio, productos |
| **ADMIN** | + Aprobar/rechazar negocios, moderación |

## 🐛 Troubleshooting

### App no inicia

```bash
# Ver logs
docker compose logs -f app

# Verificar puerto 8080 no está en uso
lsof -i :8080
```

### Conexión a BD falla

```bash
# Verificar que PostgreSQL está corriendo
docker compose logs postgres

# Verificar credenciales en .env
cat .env | grep DB_
```

### Frontend en blanco

```bash
# Limpiar caché y rebuild
docker compose down -v
docker compose up --build
```

## 📚 Documentación Adicional

- [DOKPLOY.md](DOKPLOY.md) - Guía completa de despliegue
- [.github/copilot-instructions.md](.github/copilot-instructions.md) - Flujo de trabajo GitFlow
- [backend/README.md](backend/README.md) - Backend-specific docs (si existe)
- [frontend/README.md](frontend/README.md) - Frontend-specific docs (si existe)

## 🤝 Contribución

1. Crear rama desde `master`: `git checkout -b feature/my-feature master`
2. Hacer cambios y commit con messages descriptivos
3. Abrir Pull Request a `dev`
4. Esperar revisión y aprobación
5. Merge automático seguirá el flujo GitFlow

## 📄 Licencia

Este proyecto está bajo licencia MIT. Ver [LICENSE](LICENSE) para detalles.

## 📞 Contacto

- **Issues**: https://github.com/tu-org/ResenaApp/issues
- **Discussions**: https://github.com/tu-org/ResenaApp/discussions
- **Email**: soporte@resenaapp.com

---

**Última actualización**: Abril 2026  
**Versión**: 0.0.1-SNAPSHOT  
**Estado**: En desarrollo active
```

Jar generado:

- `backend/target/reviews-api-0.0.1-SNAPSHOT.jar`

## Opcional: omitir build de frontend desde Maven

```powershell
cd backend
mvn -DskipTests package -DskipFrontend=true
```
