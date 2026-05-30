# Integración Multi-Ambiente

Documentación de cómo ReseñaApp funciona en desarrollo, staging y producción.

## 📐 Arquitectura de Ambiente

```
┌──────────────────────────────────────────────────────────────────┐
│                        DESARROLLO LOCAL                          │
├──────────────────────────────────────────────────────────────────┤
│  Frontend (Vite):     http://localhost:5173                      │
│  Backend (Spring):    http://localhost:8080                      │
│  Database (PG):       localhost:5432                             │
│  API URL:             http://localhost:8080/api                  │
│                                                                  │
│  Docker Compose:      docker-compose.yml                         │
│  Branch:              any (local)                                │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                        STAGING (QA Testing)                      │
├──────────────────────────────────────────────────────────────────┤
│  Frontend (Vercel):   staging-resena.vercel.app                 │
│  Backend (Dokploy):   dokploy-staging.resena.com               │
│  API URL:             https://api-staging.resena.com/api         │
│                                                                  │
│  Frontend Build:      npm run build:staging                      │
│  Docker Compose:      docker-compose.prod.yml                    │
│  Branch:              release, develop                           │
│  Auto-deploy:         ✅ GitHub webhook triggers                │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                      PRODUCTION (Live)                           │
├──────────────────────────────────────────────────────────────────┤
│  Frontend (Vercel):   resena.com (custom domain)                │
│  Backend (Dokploy):   dokploy-prod.resena.com                  │
│  API URL:             https://api.resena.com/api                │
│                                                                  │
│  Frontend Build:      npm run build:production                   │
│  Docker Compose:      docker-compose.prod.yml                    │
│  Branch:              master                                    │
│  Auto-deploy:         ✅ GitHub webhook triggers                │
│  SSL/TLS:             ✅ Auto via Let's Encrypt                 │
└──────────────────────────────────────────────────────────────────┘
```

## 🔄 Variables de Entorno por Ambiente

### Frontend (Vite)

| Variable | Dev | Staging | Prod |
|----------|-----|---------|------|
| `VITE_API_URL` | http://localhost:8080 | https://api-staging.resena.com | https://api.resena.com |
| `VITE_ENVIRONMENT` | development | staging | production |
| `VITE_LOG_LEVEL` | debug | debug | info |
| `VITE_APP_NAME` | ReseñaApp | ReseñaApp Staging | ReseñaApp |

### Backend (Spring Boot)

| Variable | Dev | Staging | Prod |
|----------|-----|---------|------|
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://localhost:5432/resena_db | [Dokploy] | [Dokploy] |
| `SPRING_PROFILES_ACTIVE` | docker,dev | docker,prod | docker,prod |
| `JWT_SECRET` | changeme | [secret from Vercel] | [secret from Vercel] |
| `GOOGLE_CLIENT_ID` | [local] | [stg credentials] | [prod credentials] |
| `LOG_LEVEL` | DEBUG | INFO | INFO |

## 📋 Configuración de Archivos

```
ReseñaApp/
├── vercel.json                    # Config Vercel (Frontend routing)
├── .vercelignore                  # Ignore backend files in Vercel
├── docker-compose.yml             # Dev (local)
├── docker-compose.prod.yml        # Prod (Dokploy)
│
├── frontend/
│   ├── vite.config.ts             # Vite + environment support
│   ├── package.json               # Build scripts (dev, staging, prod)
│   ├── .env.development           # Dev API URL
│   ├── .env.staging               # Staging API URL
│   ├── .env.production            # Production API URL
│   └── src/
│       └── api/axios.ts           # Uses VITE_API_URL
│
└── backend/
    ├── src/main/resources/
    │   ├── application.yml        # Base config
    │   ├── application-docker.yml # Docker settings
    │   └── application-prod.yml   # Production settings
    └── pom.xml
```

## 🚀 Despliegue por Ambiente

### 1. Development (Local)

```bash
# Opción A: Docker Compose (recomendado)
docker compose up -d

# Opción B: Servicios individuales
# Terminal 1: Backend
cd backend
mvn spring-boot:run

# Terminal 2: Frontend
cd frontend
npm install
npm run dev
```

**Acceso:**
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Admin: http://localhost:8080/admin
- Login: admin@resena.local / Admin12345!

### 2. Staging (Para QA)

```bash
# 1. Push a rama release
git checkout release
git merge develop (or create PR)
git push origin release

# 2. Vercel auto-despliega frontend
# → staging-resena.vercel.app

# 3. Dokploy auto-despliega backend
# → dokploy-staging.resena.com

# 4. Verificar
curl https://staging-resena.vercel.app/
curl https://api-staging.resena.com/api/businesses
```

### 3. Production (Live)

```bash
# 1. Push a rama master
git checkout master
git merge release
git push origin master

# 2. Vercel auto-despliega frontend
# → resena.com

# 3. Dokploy auto-despliega backend
# → dokploy-prod.resena.com

# 4. Verificar
curl https://resena.com/
curl https://api.resena.com/api/businesses
```

## 🔐 Secretos por Ambiente

### Development

```bash
# .env (local, NO en git)
JWT_SECRET=demo-secret-not-secure
DB_PASSWORD=demo123
GOOGLE_CLIENT_ID=
FACEBOOK_CLIENT_ID=
```

### Staging

```bash
# En Vercel Dashboard → Settings → Environment Variables
# Con aplicación en rama: release

VITE_API_URL=https://api-staging.resena.com
```

```bash
# En Dokploy → Settings → Environment Variables
JWT_SECRET=<secure-secret-generation>
DB_PASSWORD=<secure-password>
GOOGLE_CLIENT_ID=<datos-reales>
FACEBOOK_CLIENT_ID=<datos-reales>
```

### Production

```bash
# En Vercel Dashboard → Settings → Environment Variables
# Con aplicación en rama: master

VITE_API_URL=https://api.resena.com
```

```bash
# En Dokploy → Settings → Environment Variables
JWT_SECRET=<secure-secret-generation>
DB_PASSWORD=<secure-password>
GOOGLE_CLIENT_ID=<datos-reales>
FACEBOOK_CLIENT_ID=<datos-reales>
```

## 🧪 Testing Multi-Ambiente

```bash
# Test desarrollo local
npm run dev        # Frontend
# En otra terminal:
mvn spring-boot:run # Backend

# Test staging (build)
npm run build:staging
vercel preview --prod

# Test producción (build)
npm run build:production
```

## 📊 Flujo de Promoción

```
┌─ feature branch
│    ↓
│  PR to develop
│    ↓
├─ develop (auto: Vercel preview)
│    ↓
│  Create release branch
│    ↓
├─ release (auto: Staging deploy)
│    ↓ (QA testing)
│
│  Approve release
│    ↓
├─ master (auto: Production deploy ✅)
```

## ✅ Checklist Pre-Deploy

### Antes de Promocionar a Staging

- [ ] Compilación local exitosa: `npm run build:staging`
- [ ] Tests pasan: `npm run type-check`
- [ ] Variables en .env correctas
- [ ] Backend en staging respondiendo

### Antes de Promocionar a Producción

- [ ] QA validation en staging completada
- [ ] Changelog actualizado
- [ ] Versión en package.json incremented
- [ ] Backend en producción verificado
- [ ] SSL/TLS en Vercel activo
- [ ] Dominios custom funcionando

## 🆘 Debugging Multi-Ambiente

### Frontend en Vercel

```bash
# Ver logs en tiempo real
vercel logs --project=resenaapp

# O en Dashboard → Deployments → Click → Logs

# Verificar variables de entorno
vercel env pull

# Forza redeploy
vercel redeploy --project=resenaapp
```

### Backend en Dokploy

```bash
# Ver logs
docker compose logs -f app

# O en Dokploy UI → Logs

# Verificar variables
docker compose exec app env | grep VITE_

# Restart
docker compose restart app
```

### Problema: API calls fallan en producción

```bash
# 1. Verificar URL en Vercel
VITE_API_URL=https://api.resena.com  # ✅ Con https://

# 2. Verificar CORS en backend
FRONTEND_URL=https://resena.com      # ✅ Con https://

# 3. Verificar ruta en vercel.json
"redirects": [
  { "source": "/api/:path*", "destination": "..." }
]
```

## 📚 Documentación Adicional

- [VERCEL.md](VERCEL.md) - Guía completa Vercel
- [DOKPLOY.md](DOKPLOY.md) - Guía completa Dokploy
- [QUICKSTART_VERCEL.md](QUICKSTART_VERCEL.md) - 5-min Vercel
- [README.md](README.md) - Arquitectura general

## 🎯 Resumen

| Aspecto | Local | Staging | Prod |
|---------|-------|---------|------|
| **Frontend Host** | localhost | Vercel | Vercel |
| **Backend Host** | localhost | Dokploy | Dokploy |
| **Build** | vite dev | build:staging | build:prod |
| **Deploy** | Manual | Auto-webhook | Auto-webhook |
| **SSL** | No | Let's Encrypt | Let's Encrypt |
| **Dominio** | No | vercel.app | Custom |

---

**Última actualización**: Abril 2026
**Version**: 0.0.1
**Status**: Multi-ambiente ✅ Production-Ready
