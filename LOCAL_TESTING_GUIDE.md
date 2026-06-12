# 🚀 Guía Rápida: Levantar & Testear ReseñaApp Localmente

**Requisitos Mínimos**:
- Java 21+
- Node.js 22+
- Maven 3.9+
- PostgreSQL 16+ (o Docker)
- Git

**Tiempo Estimado**: 15 minutos

---

## 📦 Opción A: Con Docker Compose (Recomendado)

### Paso 1: Levantar Stack Completo

```bash
cd /ruta/a/ReseñaApp
docker compose up -d
```

**Esperado**:
```
✅ Creating reseniaapp-db ... done
✅ Creating reseniaapp-app ... done
```

### Paso 2: Esperar Health Check

```bash
# Verificar que la app esté lista (timeout ~40s)
docker compose logs -f app | grep "Started"
```

**Esperado**:
```
2026-06-12 22:55:30 ... Started ReviewsApiApplication
```

### Paso 3: Acceder a la Aplicación

- **Frontend SPA**: http://localhost:8080
- **API Backend**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/actuator/health

---

## 💻 Opción B: Local Sin Docker

### Paso 1: Levantar PostgreSQL (en Docker)

```bash
# Terminal 1: Base de datos
docker run -d \
  --name resenias-db \
  -e POSTGRES_DB=resenias \
  -e POSTGRES_USER=resenias \
  -e POSTGRES_PASSWORD=resenias123 \
  -p 5432:5432 \
  postgres:16-alpine

# Verificar que está corriendo
docker ps | grep postgres
```

### Paso 2: Levantar Backend

```bash
# Terminal 2: Backend Spring Boot
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Esperado después de ~10s:
# 18:55:30 INFO ReviewsApiApplication: Started ReviewsApiApplication
```

### Paso 3: Levantar Frontend

```bash
# Terminal 3: Frontend Vue 3 + Vite
cd frontend
npm install  # si es primera vez
npm run dev

# Esperado:
# ➜ Local: http://localhost:5173/
```

### Paso 4: Acceder

- **Frontend**: http://localhost:5173
- **Backend**: http://localhost:8080
- **API**: http://localhost:8080/api

---

## ✅ Panel de Pruebas Funcionales

### 1️⃣ Health & Connectivity

```bash
# Health check (sin auth)
curl -s http://localhost:8080/actuator/health | jq

# App info (sin auth)
curl -s http://localhost:8080/actuator/info | jq

# Esperado:
# {
#   "status": "UP",
#   "components": {...}
# }
```

✅ **PASS** si ambos retornan status: "UP"

---

### 2️⃣ Endpoints Públicos

```bash
# Listar negocios (sin auth)
curl -s http://localhost:8080/api/businesses | jq '.content[] | {id, name}'

# Esperado:
# [
#   {"id": 1, "name": "Negocio Demo 1"},
#   {"id": 2, "name": "Negocio Demo 2"}
# ]
```

✅ **PASS** si retorna lista de negocios

---

### 3️⃣ Autenticación - Email/Password

```bash
# Login con credenciales de demo
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@resena.local",
    "password": "Admin12345!"
  }' | jq

# Esperado:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "user": {
#     "id": 1,
#     "email": "admin@resena.local",
#     "role": "ADMIN"
#   }
# }
```

✅ **PASS** si retorna token JWT válido  
❌ **FAIL** si retorna 401 Unauthorized

**Si FAIL**: Verificar que la BD tiene datos de seed (V4__seed_full_demo_data.sql)

---

### 4️⃣ Endpoints Protegidos (con JWT)

```bash
# Usar token del paso anterior
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Obtener mis reseñas
curl -s http://localhost:8080/api/reviews/mine \
  -H "Authorization: Bearer $TOKEN" | jq

# Esperado:
# {
#   "content": [],  # vacío si es nuevo usuario
#   "empty": true
# }
```

✅ **PASS** si retorna 200 OK y lista de reseñas  
❌ **FAIL** si retorna 401 Unauthorized (token inválido)

---

### 5️⃣ Frontend Visual

**URL**: http://localhost:5173 (o :8080 si es Docker)

#### Checklist Visual:

- [ ] **Home page carga** → Lista de negocios visible
- [ ] **Navbar renderiza** → Logo, Navigation, Login button
- [ ] **Business cards visibles** → Nombre, descripción, rating
- [ ] **Routing funciona** → Click About/Contact navega sin refrescar
- [ ] **Login button visible** → en navbar o hero section
- [ ] **Map renderiza** → Si hay Leaflet configuration

#### Screenshots Esperadas:

```
┌─────────────────────────────────────┐
│  ReseñaApp                 [Login]  │  ← Navbar
├─────────────────────────────────────┤
│                                     │
│  Explore Businesses                 │  ← Hero
│  [Search box]                       │
│                                     │
├─ ┬─ ─────────────────────────────┐ ┤
│  │ Business 1                    │ │
│  │ ★★★★★ 4.8 (120 reseñas)     │ │  ← Business Cards
│  │ [Ver Detalles]                │ │
│  └─ ─────────────────────────────┘ ┤
│                                     │
│  ┌─ ─────────────────────────────┐ │
│  │ Business 2                    │ │
│  │ ★★★★☆ 4.2 (45 reseñas)      │ │
│  │ [Ver Detalles]                │ │
│  └─ ─────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

✅ **PASS** si todo lo anterior es visible

---

### 6️⃣ Login (Email/Password)

**Pasos**:

1. Click en botón "Login" → Abre modal/form
2. Ingresa:
   - Email: `admin@resena.local`
   - Password: `Admin12345!`
3. Click "Enviar"

**Esperado**:
- Token guardado en localStorage
- Redirige a home o dashboard
- Navbar muestra "Hola Admin"

**Comando para verificar localStorage**:
```javascript
// En DevTools console (F12)
localStorage.getItem('token')

// Esperado: JWT token string
// "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

✅ **PASS** si token está en localStorage  
❌ **FAIL** si muestra error 401 o no se guarda token

---

### 7️⃣ Create Review (Funcionalidad Post-Auth)

**Pasos** (requiere estar logueado):

1. En business detail, click "Escribir Reseña"
2. Selecciona rating ⭐⭐⭐⭐⭐
3. Ingresa texto: "Great place!"
4. Click "Publicar"

**Esperado**:
- Toast/notificación: "Reseña publicada"
- Review aparece en lista
- API response: 201 Created

**Comando para crear reseña vía API**:
```bash
TOKEN="tu-jwt-token"

curl -X POST http://localhost:8080/api/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "businessId": 1,
    "rating": 5,
    "comment": "Excelente servicio!"
  }' | jq

# Esperado:
# {
#   "id": 123,
#   "businessId": 1,
#   "userId": 1,
#   "rating": 5,
#   "comment": "Excelente servicio!",
#   "createdAt": "2026-06-12T..."
# }
```

✅ **PASS** si retorna 201 Created  
❌ **FAIL** si retorna 401 (token inválido) o 400 (bad request)

---

### 8️⃣ Admin Panel (si tienes rol ADMIN)

**URL**: http://localhost:5173/admin (o :8080/admin)

**Funcionalidades esperadas**:
- [ ] Panel de moderación visible
- [ ] Lista de reseñas para aprobar/rechazar
- [ ] Lista de negocios para administrar
- [ ] Control de usuarios (si está implementado)

✅ **PASS** si el dashboard carga sin errores  
❌ **FAIL** si acceso denegado (403 Forbidden)

---

## 📋 Full Test Suite Script

Copia y pega en bash/PowerShell:

```bash
#!/bin/bash
set -e

API="http://localhost:8080"
FRONTEND="http://localhost:5173"

echo "🔍 Testing ReseñaApp..."
echo ""

# 1. Health Check
echo "1️⃣  Health Check..."
curl -s $API/actuator/health | jq .status
echo "✅ Health OK"
echo ""

# 2. Get businesses
echo "2️⃣  Fetching businesses..."
curl -s $API/api/businesses | jq '.content | length'
echo "✅ Businesses loaded"
echo ""

# 3. Login
echo "3️⃣  Testing login..."
RESPONSE=$(curl -s -X POST $API/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@resena.local",
    "password": "Admin12345!"
  }')

TOKEN=$(echo $RESPONSE | jq -r '.token // empty')

if [ -z "$TOKEN" ]; then
  echo "❌ Login failed"
  echo $RESPONSE | jq
  exit 1
fi

echo "✅ Login successful"
echo "Token: ${TOKEN:0:20}..."
echo ""

# 4. Protected endpoint
echo "4️⃣  Testing protected endpoint..."
curl -s $API/api/reviews/mine \
  -H "Authorization: Bearer $TOKEN" | jq '.empty'
echo "✅ Protected endpoint works"
echo ""

# 5. Frontend accessibility
echo "5️⃣  Checking frontend..."
if curl -s $FRONTEND | grep -q "ReseñaApp"; then
  echo "✅ Frontend is accessible"
else
  echo "⚠️  Frontend may not be running on port 5173"
  echo "   Try: npm run dev"
fi

echo ""
echo "🎉 All tests passed!"
```

**Guardar como**: `test.sh` (Mac/Linux) o transliterate a PowerShell

---

## 🐛 Troubleshooting

| Problema | Causa | Solución |
|----------|-------|----------|
| `Connection refused :8080` | Backend no está corriendo | `mvn spring-boot:run` en `/backend` |
| `Connection refused :5173` | Frontend no está corriendo | `npm run dev` en `/frontend` |
| `Connection refused :5432` | PostgreSQL no está corriendo | `docker run postgres:16-alpine` |
| `401 Unauthorized` en Login | Credenciales incorrectas | Usa: `admin@resena.local` / `Admin12345!` |
| `404 Not Found /api/businesses` | API ruta no existe | Verificar `curl http://localhost:8080/` devuelve HTML |
| `CORS Error` en Frontend | Backend no permite origen | Verificar `app.frontend-url` en `.env` |
| `Database does not exist` | Migrations no se ejecutaron | Recrear DB: `docker volume rm reseniaapp_pgdata` |

---

## 📊 Performance Baseline

**Esperado (local hardware típico)**:

| Métrica | Target | Status |
|---------|--------|--------|
| Backend startup | < 10s | ⏳ Depends on hardware |
| Frontend dev server startup | < 5s | ⏳ Depends on hardware |
| API response (businesses list) | < 100ms | ⏳ TBD |
| Page load (SPA) | < 1s | ⏳ TBD |
| Database query time | < 50ms | ⏳ TBD |

---

## ✨ Next Steps After Testing

✅ **If all tests pass**:
1. Commit any changes: `git add -A && git commit -m "test: Local testing passed"`
2. Push to dev: `git push origin dev`
3. GitHub Actions will run CI/CD automatically
4. Check status: https://github.com/your-user/ReseñaApp/actions

❌ **If tests fail**:
1. Check logs: `docker logs reseniaapp-app` (Docker) or terminal output (local)
2. Review [TESTING_REPORT.md](TESTING_REPORT.md) for detailed diagnostics
3. Ask for help with error messages

---

**Generated**: 2026-06-12  
**Last Updated**: June 12, 2026
