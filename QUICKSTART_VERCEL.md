# 🚀 Despliegue Rápido en Vercel

Guía paso a paso para desplegar **ReseñaApp Frontend** en Vercel en 5 minutos.

## ✅ Pre-Requisitos

- [ ] Cuenta en [GitHub](https://github.com)
- [ ] Cuenta en [Vercel](https://vercel.com)
- [ ] Repositorio conectado entre GitHub y Vercel
- [ ] Backend desplegado en Dokploy (see [DOKPLOY.md](DOKPLOY.md))

## 🎯 5 Pasos para Desplegar

### Paso 1: Ir a Vercel Dashboard (2 min)

1. Abre https://vercel.com/dashboard
2. Haz login con tu cuenta GitHub
3. Click en **"Add New"** → **"Project"**

### Paso 2: Seleccionar Repositorio (1 min)

1. Busca y selecciona **"ReseñaApp"** (o tu nombre del repo)
2. Click **"Import"**
3. Vercel auto-detecta:
   - Framework: Vite ✓
   - Build Command: `npm run build` ✓
   - Output Directory: `dist` ✓

### Paso 3: Configurar Variables de Entorno (1 min)

Antes de deploy, agregar variables:

**Para Producción:**
```
VITE_API_URL = https://api.resenaapp.com
VITE_ENVIRONMENT = production
```

**En Vercel UI:**
1. Click **"Continue to Project"**
2. → **"Settings"** → **"Environment Variables"**
3. Agregar cada variable:
   - Name: `VITE_API_URL`
   - Value: `https://api.resenaapp.com`
   - Click **"Add"**
4. Repetir para `VITE_ENVIRONMENT`

### Paso 4: Desplegar (1 min)

1. Click **"Deploy"**
2. Esperar a que termine (usualmente 2-3 min)
3. ¡Listo! Tu app está en `<project>.vercel.app`

### Paso 5: Configurar Dominio Custom (Opcional, 1 min)

1. En Vercel → **Settings** → **Domains**
2. Click **"Add Domain"**
3. Ingresa tu dominio: `resena.com`
4. Vercel te da opciones:
   - **CNAME** (más fácil): Apuntar CNAME en tu registrador
   - **Nameservers**: Cambiar NS en tu registrador
5. Verificado: ✅ Tu domini apunta a Vercel

## 🔄 Próximas Deployments (Automáticas)

**El flujo es automático ahora:**

```bash
# En tu repo local
git checkout master
git pull origin master

# Haz cambios y commit
echo "// mis cambios" >> frontend/src/main.ts
git add .
git commit -m "feat: nuevo feature"
git push origin master

# ↓ GitHub webhook → Vercel
# ↓ Vercel auto-build y deploy
# ✅ En producción en 2-3 minutos
```

## 📊 Tabla de Ramas → Deployments

| Rama | Tipo Deploy | URL |
|------|-------------|-----|
| `master` | Production | `resena.com` (custom) |
| `release` | Preview | `staging.resena.vercel.app` |
| `develop` | Preview | `develop-xxx.resena.vercel.app` |
| Pull Requests | Preview | `pr-123-xxx.resena.vercel.app` |

## 🔗 Conectar Frontend + Backend

**El frontend en Vercel necesita acceder al backend en Dokploy:**

En `frontend/.env.production`:
```env
VITE_API_URL=https://api.resenaapp.com
```

Y en `vercel.json` (ya está configurado):
```json
"redirects": [
  {
    "source": "/api/:path*",
    "destination": "https://api.resenaapp.com/api/:path*"
  }
]
```

**Así que cuando el frontend hace:**
```typescript
await fetch('/api/businesses')
```

Vercel automáticamente redirige a:
```
https://api.resenaapp.com/api/businesses
```

## 🆘 Troubleshooting Rápido

### "Build failed"

```bash
# Ejecutar localmente para ver error:
cd frontend
npm install
npm run build
# Verás el error exacto
```

### "API returns 404"

```bash
# Verificar URL del backend:
# Settings → Environment Variables
# VITE_API_URL debe ser correcto
# Ejemplo: https://api-staging.resenaapp.com
```

### "Dominio no funciona"

```bash
# Verificar DNS propagación:
nslookup resena.com
# Debe resolver a IP de Vercel

# Si no:
1. Esperar 24-48h para propagación
2. O cambiar TTL a 300 en tu registrador
```

## 📱 Monitoreo Post-Deploy

Vercel proporciona:

1. **Analytics** - Visitas, performance
2. **Logs** - Ver errores en tiempo real
3. **Web Vitals** - Métricas de performance

Acceder en: Dashboard → Project → Analytics

## 🎓 Próximas Lecturas

- [VERCEL.md](VERCEL.md) - Guía completa
- [DOKPLOY.md](DOKPLOY.md) - Backend deployment
- [README.md](README.md) - Arquitectura general

---

**¿Listo?** 

1. Ve a https://vercel.com/dashboard
2. Presiona "Add New" → "Project"
3. Selecciona tu repo
4. ¡Listo en 5 minutos! 🚀

**Dudas?** Ver [VERCEL.md](VERCEL.md) para guía completa.
