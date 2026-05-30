# 🚀 ReseñaApp - Multi-Platform Ready

Guía rápida para desplegar ReseñaApp en **Vercel (Frontend) + Dokploy (Backend)** con múltiples ambientes.

## ⚡ Quick Start (5 minutos)

### Development (Local)
```bash
# 1. Clonar repo
git clone https://github.com/tu-org/ResenaApp.git
cd ResenaApp

# 2. Copiar .env
cp .env.example .env

# 3. Levantar todo
docker compose up -d

# 4. Frontend en otra terminal
cd frontend && npm install && npm run dev

# 5. Abrir http://localhost:5173
```

### Staging (QA Testing)
```bash
# Ver: QUICKSTART_VERCEL.md
# 3 pasos en Vercel, automático después

git push origin release
# ↓ Vercel auto-despliega a staging.resena.vercel.app
```

### Production (Live)
```bash
# Ver: VERCEL.md + DOKPLOY.md

git push origin master
# ↓ Vercel auto-despliega a resena.com
# ↓ Dokploy auto-despliega backend
```

## 📊 Arquitectura

```
┌─────────────────────────────────────────┐
│  Frontend (Vue 3 + Vite)                │
│  Vercel: production/staging/preview     │
└──────────────┬──────────────────────────┘
               │
               ↓ HTTP/REST
               
┌─────────────────────────────────────────┐
│  Backend (Spring Boot 3.2)              │
│  Dokploy: production/staging/dev        │
└──────────────┬──────────────────────────┘
               │
               ↓ JDBC
               
┌─────────────────────────────────────────┐
│  Database (PostgreSQL 16)               │
│  Dokploy                                │
└─────────────────────────────────────────┘
```

## 🌐 Ambientes Disponibles

| Env | Frontend | Backend | Acceso | Rama |
|-----|----------|---------|--------|------|
| **Dev** | localhost:5173 | localhost:8080 | Local | any |
| **Staging** | staging.vercel.app | api-staging.* | QA | release |
| **Prod** | resena.com | api.resena.com | Public | master |

## 📁 Archivos Importantes

```
.github/workflows/          ← GitHub Actions (CI/CD)
  ├─ ci-build.yml           ← Build & Test
  ├─ docker-build.yml       ← Docker image
  └─ deploy-to-dokploy.yml  ← Deploy webhook

vercel.json                 ← Config Vercel
.vercelignore              ← Ignore backend

frontend/
  ├─ vite.config.ts        ← Multi-env support
  ├─ package.json           ← Scripts (dev, build:staging, build:prod)
  ├─ .env.development       ← Local API URL
  ├─ .env.staging           ← Staging API URL
  └─ .env.production        ← Prod API URL

backend/
  ├─ pom.xml                ← Maven + Actuator
  └─ src/main/resources/
     ├─ application.yml     ← Base
     ├─ application-docker.yml
     └─ application-prod.yml ← Production

docker-compose.yml          ← Local dev stack
docker-compose.prod.yml     ← Production deploy
```

## 📚 Documentación

| Documento | Para |
|-----------|------|
| [README.md](README.md) | Visión general + arquitectura |
| [VERCEL.md](VERCEL.md) | Guía completa Vercel |
| [DOKPLOY.md](DOKPLOY.md) | Guía completa Dokploy |
| [QUICKSTART_VERCEL.md](QUICKSTART_VERCEL.md) | **5-min setup Vercel** (comienza aquí) |
| [MULTI_ENVIRONMENT.md](MULTI_ENVIRONMENT.md) | Variables + troubleshooting |
| [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md) | Pre-deploy checklist |

## 🎯 Proximos Pasos

### 1️⃣ Frontend en Vercel (10 min)
```
Ver: QUICKSTART_VERCEL.md
├─ Crear cuenta Vercel
├─ Conectar GitHub repo
├─ Configurar variables de entorno
└─ Deploy ✅
```

### 2️⃣ Backend en Dokploy (10 min)
```
Ver: DOKPLOY.md sección 1-4
├─ Crear cuenta Dokploy
├─ Conectar GitHub repo
├─ Configurar variables de entorno
└─ Deploy ✅
```

### 3️⃣ Configurar Dominio Custom (5 min)
```
├─ Vercel: resena.com
├─ Dokploy: api.resena.com
└─ Esperar DNS propagación (24-48h)
```

## 🔐 Variables de Entorno Críticas

### Frontend (Vercel Dashboard)

```env
# Staging (rama: release)
VITE_API_URL=https://api-staging.resena.com
VITE_ENVIRONMENT=staging

# Production (rama: master)
VITE_API_URL=https://api.resena.com
VITE_ENVIRONMENT=production
```

### Backend (Dokploy Dashboard)

```env
# Ambos ambientes
JWT_SECRET=<openssl rand -base64 64>
DB_PASSWORD=<contraseña-segura>
GOOGLE_CLIENT_ID=<google-console>
FACEBOOK_CLIENT_ID=<facebook-dev>
FRONTEND_URL=https://resena.com  # Para CORS
```

## ✅ Deploy Checklist

- [ ] Backend en Dokploy (staging + prod)
- [ ] Frontend en Vercel (staging + prod)
- [ ] Variables de entorno configuradas
- [ ] Dominios custom apuntando
- [ ] SSL/TLS verificado
- [ ] API calls funcionan end-to-end
- [ ] Login + OAuth2 probados
- [ ] Base de datos conectada

## 🚨 Troubleshooting Rápido

```bash
# Frontend build falla
cd frontend && npm run build

# API calls retornan 404
# Verificar: VITE_API_URL en Vercel variables

# Dominio no funciona
# Esperar propagación DNS (24-48h)
# O verificar CNAME en registrador

# Backend rechaza requests
# Verificar CORS en FRONTEND_URL (Dokploy env vars)
```

## 📈 Monitoreo Post-Deploy

1. **Vercel Analytics** - Performance + visitas
2. **Dokploy Logs** - Backend errors
3. **GitHub Actions** - Build status
4. **Uptime Monitoring** (opcional) - UptimeRobot, Pingdom

## 💰 Costos Estimados

- **Vercel**: $0 (hobby plan) - $20/mes (pro)
- **Dokploy**: $25-35/mes
- **Dominio**: $12-15/año
- **Total**: ~$40-50/mes

## 🤝 Workflow de Desarrollo

```bash
# 1. Crear feature
git checkout -b feature/xyz master

# 2. Desarrollar
# (cambios en frontend/ y backend/)

# 3. Push → Auto preview en Vercel
git push origin feature/xyz

# 4. PR Review
# Pull Request → Vercel preview link para testing

# 5. Merge → Staging
# Merge a release → Auto deploy staging.vercel.app

# 6. QA Testing
# Team testa en staging

# 7. Merge → Production
# Merge release a master → Auto deploy resena.com 🚀
```

## 📞 Support & Docs

- **Vercel Docs**: https://vercel.com/docs
- **Dokploy Docs**: https://dokploy.com/docs
- **GitHub Issues**: tu-repo/issues
- **Status Page**: (opcional)

---

**¿Listo para desplegar?**

👉 Comienza con [QUICKSTART_VERCEL.md](QUICKSTART_VERCEL.md) (5 minutos) 

---

**Última actualización**: Abril 2026
**Aplicación**: ReseñaApp v0.0.1
**Estado**: ✅ Production-Ready Multi-Ambiente
