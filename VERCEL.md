# Despliegue en Vercel

Esta guía explica cómo desplegar **ReseñaApp Frontend** en [Vercel](https://vercel.com/), la plataforma moderna de hosting para frontend.

> **Nota**: El **backend REST** se despliega en **Dokploy**. Vercel es solo para the frontend (Vue 3 + Vite).

## Arquitectura Multi-Ambiente

```
┌─────────────────────────────────────────────────────────────┐
│                     DESARROLLO LOCAL                         │
│  Frontend: http://localhost:5173 ← Proxy → Backend: :8080   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     STAGING (Pruebas)                        │
│  Frontend: staging-resena.vercel.app → Backend: (Dokploy)   │
│    rama: develop/release                                    │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     PRODUCCIÓN                               │
│  Frontend: resena.com (custom domain) → Backend: (Dokploy)   │
│    rama: master                                             │
└─────────────────────────────────────────────────────────────┘
```

## 1. Prerequisitos

- Cuenta en [vercel.com](https://vercel.com)
- GitHub, GitLab o Bitbucket conectado
- Node.js 18+ instalado localmente
- Git configurado

## 2. Configuración Inicial

### 2.1 Instalar CLI de Vercel (Opcional)

```bash
npm install -g vercel
```

### 2.2 Login en Vercel

```bash
vercel login
# Seguir las instrucciones de autenticación
```

### 2.3 Verificar Configuración

Vercel ya detecta:
- Framework: Vite
- Build Command: `npm run build`
- Output Directory: `dist`

Confirmado en:
- `vercel.json` - Configuración raíz
- `frontend/vite.config.ts` - Configuración Vite
- `frontend/package.json` - Scripts de build

## 3. Conectar Repositorio a Vercel

### 3.1 Desde Vercel Dashboard

1. Ir a https://vercel.com/dashboard
2. Click "Add New..." → "Project"
3. Seleccionar repositorio
4. Vercel auto-detecta configuración
5. Click "Deploy"

### 3.2 Alternativa: Desde CLI

```bash
cd ReseñaApp
vercel --prod
```

## 4. Configurar Múltiples Ambientes

### 4.1 Crear Projects en Vercel

**Producción:**
1. Crear nuevo project: resenasapp-prod
2. Conectar rama: `master`
3. Variables de entorno: Production

**Staging:**
1. Crear nuevo project: resenasapp-staging  
2. Conectar rama: `release` o `develop`
3. Variables de entorno: Staging

### 4.2 Configurar Variables de Entorno

En Vercel Dashboard → Settings → Environment Variables

**Para Staging:**
```
VITE_API_URL=https://api-staging.resenaapp.com
VITE_ENVIRONMENT=staging
VITE_LOG_LEVEL=debug
```

**Para Producción:**
```
VITE_API_URL=https://api.resenaapp.com
VITE_ENVIRONMENT=production
VITE_LOG_LEVEL=info
```

### 4.3 Aplicar a Ambientes

Cada variable puede estar limitada a:
- `Production` - Solo cuando rama es `master`
- `Preview` - PRs y preview deployments
- `Development` - Desarrollo local

```
Crear variable "VITE_API_URL"
├─ Production: https://api.resenaapp.com
├─ Preview: https://api-staging.resenaapp.com
└─ Development: http://localhost:8080
```

## 5. Despliegue por Rama

Vercel despliega automáticamente según rama:

| Rama | Entorno | URL | Acceso |
|------|---------|-----|--------|
| `master` | Production | resena.com | 🌐 Público |
| `release` | Staging | staging.resena.vercel.app | 🔒 Privado |
| `develop` | Preview | `<branch>.resena.vercel.app` | 🔒 Privado |
| PRs | Preview | `pr-<number>.resena.vercel.app` | 🔒 Privado |

## 6. Configurar Dominios Custom

### 6.1 Dominio Principal (Producción)

1. Vercel Dashboard → resenasapp-prod → Settings → Domains
2. Click "Add Domain"
3. Ingresar: `resena.com` (o tu dominio)
4. Vercel proporciona nameservers o CNAME
5. Actualizar DNS en tu registrador

**Ejemplo (Namecheap, GoDaddy, etc):**
```
Type: A Record
Name: @
Value: 76.76.19.165 (Vercel IP)

OR

Type: CNAME Record
Name: @
Value: cname.vercel-dns.com.
```

### 6.2 Dominio Staging

```
Type: CNAME
Name: staging
Value: cname.vercel-dns.com.
```

### 6.3 Verificar Propagación

```bash
nslookup resena.com
# Debe resolver a IP de Vercel
```

## 7. Workflow de Despliegue Automático

### 7.1 GitFlow → Vercel

```bash
# 1. Desarrollar en feature
git checkout -b feature/mi-feature develop

# 2. Hacer cambios
# git add, git commit, etc.

# 3. Push (auto: Preview Deployment)
git push origin feature/mi-feature
# ↓ crear PR → Vercel auto-genera preview
# https://pr-123.resena.vercel.app

# 4. Merge a develop (auto: Staging Deploy)
# Pull Request → Merge
git checkout develop
git merge feature/mi-feature
git push origin develop
# ↓ Vercel auto-despliega staging.resena.vercel.app

# 5. Release → Merge a master (auto: Prod Deploy)
git checkout master
git merge release
git push origin master
# ↓ Vercel auto-despliega resena.com 🚀
```

### 7.2 Ejemplo de Commit Message

```bash
git commit -m "feat: agregar filtros de búsqueda (PR-42)"
git push origin feature/search-filters
```

## 8. SPA Routing en Vercel

Vercel redirige automáticamente todas las rutas a `index.html` (configurado en `vercel.json`):

```json
"routes": [
  {
    "src": "/(.*)",
    "dest": "/index.html",
    "status": 200
  }
]
```

**Por lo que estas rutas funcionan:**
- `/explore` → `index.html`
- `/business/123` → `index.html`
- `/admin/dashboard` → `index.html`

Vue Router maneja la navegación en el cliente.

## 9. Caching y Optimizaciones

### 9.1 Assets Estáticos

Archivos en `/dist/assets/` se cachean 1 año:

```
js, css, woff, woff2, ttf, eot, svg
```

### 9.2 HTML

No se cachea (siempre fresco para que Vue Router funcione).

### 9.3 Proxying de API

Vercel redirige `vercel.json` → `/api/*` al backend en Dokploy:

```json
"redirects": [
  {
    "source": "/api/:path*",
    "destination": "https://api.resenaapp.com/api/:path*"
  }
]
```

Así el frontend accede a: `fetch('/api/businesses')` sin CORS issues.

## 10. Monitoreo en Vercel

### 10.1 Analytics

Vercel proporciona:
- Tiempo de build
- Tamaño de bundle
- Tiempo de respuesta
- Performance metrics

Dashboard → Analytics

### 10.2 Logs

```bash
# Ver logs en tiempo real
vercel logs <project-name>

# O en Dashboard → Deployments → Click deployment → Logs
```

### 10.3 Insights

Vercel + Web Vitals:
- Core Web Vitals
- First Contentful Paint
- Largest Contentful Paint
- Cumulative Layout Shift

## 11. Troubleshooting

### App no despliega

**Error: `npm install` fails**
```bash
# Solución: Verificar package-lock.json
git status package-lock.json

# Si hay conflictos, regenerar:
rm package-lock.json
npm install
git add package-lock.json
git commit -m "fix: npm lock"
```

**Error: `vite build` fails**
```bash
# Solución: Ejecutar localmente
npm run build

# Ver si hay errores TypeScript
npm run type-check
```

### Variables de entorno no funcionan

```bash
# 1. Verificar que existan en Vercel UI
# 2. Verificar que no tengan espacios extras
# 3. Redeploy (en Vercel UI → Redeploy)

# O en CLI:
vercel env pull
```

### API calls fallan en producción

**Problema:** CORS error
```bash
# Solución: Verificar VITE_API_URL
VITE_API_URL=https://api.resenaapp.com

# Backend debe tener CORS configurado:
app.frontend-url: https://resena.com
```

### Build muy lento

```bash
# Vercel cacha por defecto, pero:
# 1. Limpiar caché: Dashboard → Settings → Caching → Clear Cache
# 2. Optimizar imports en Vite
# 3. Usar dynamic imports: import().then()
```

## 12. Seguridad

### 12.1 Environment Variables

- ✅ Nunca commitar `.env` real
- ✅ Usar `.env.example` como plantilla
- ✅ En Vercel UI, variables se encriptan

### 12.2 HTTPS

- ✅ Vercel fuerza HTTPS automáticamente
- ✅ Redirección HTTP → HTTPS
- ✅ Certificados SSL auto-renovados

### 12.3 Rate Limiting

- ✅ Implementado en backend (Dokploy/Spring)
- ✅ Vercel también tiene rate limits (muy generosos)

## 13. Costos de Vercel

**Hobby Plan (Gratuito):**
- ✅ 100 GB bandwidth/mes
- ✅ Unlimited uploads
- ✅ 3 concurrent builds
- ✅ SSL/HTTPS incluido
- ✅ Custom domain

**Pro Plan ($20/mes):**
- 1 TB bandwidth
- 6 concurrent builds
- Priority support

## 14. Diferencias: Desarrollo vs Producción

| Aspecto | Dev | Prod |
|---------|-----|------|
| API URL | http://localhost:8080 | https://api.resenaapp.com |
| Logging | DEBUG | INFO |
| Sourcemaps | Sí | No |
| Bundle Size | Grande | Minificado |
| Cache | No | Agresivo |

## 15. Rollback de Despliegue

Si algo falla en producción:

```bash
# 1. Dashboard → Deployments
# 2. Encontrar deployment anterior
# 3. Click "..." → "Promote to Production"

# O revert código:
git revert <bad-commit>
git push origin master
# Vercel auto-redeploya
```

## 16. Próximos Pasos

- [ ] Crear cuenta Vercel
- [ ] Conectar repositorio
- [ ] Crear 2 projects (prod, staging)
- [ ] Configurar dominios custom
- [ ] Configurar variables de entorno
- [ ] Hacer primer deploy
- [ ] Monitorear logs y analytics
- [ ] Configurar alerts de errores (optional)

## 17. Integración Frontend-Backend

### Flujo de Datos

```
Browser → Vercel (Vue 3 SPA)
    ↓
    └─→ /api/* (proxied by Vercel)
             ↓
             └─→ Dokploy Backend (Spring Boot)
                    ↓
                    └─→ PostgreSQL
```

### Ejemplo de Llamada API

```typescript
// frontend/src/api/axios.ts
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000
});

// Uso en componente
const response = await apiClient.get('/api/businesses');
```

## Documentación Adicional

- [Vercel Docs](https://vercel.com/docs)
- [Vite Docs](https://vitejs.dev/)
- [Vue 3 Docs](https://vuejs.org/)
- [DOKPLOY.md](./DOKPLOY.md) - Backend deployment

---

**Última actualización**: Abril 2026  
**Versión**: 0.0.1  
**Status**: Multi-ambiente configurado ✅
