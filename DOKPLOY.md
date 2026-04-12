# Despliegue en Dokploy

Esta guía explica cómo desplegar **ReseñaApp** en [Dokploy](https://dokploy.com/), una plataforma moderna de despliegue similar a Vercel o Railway.

## Prerequisitos

- Cuenta activa en [Dokploy](https://dokploy.com/)
- Acceso a repositorio Git (GitHub, GitLab, Gitea, etc.)
- Variables de entorno configuradas (ver [.env.example](.env.example))

## 1. Preparación del Repositorio

### 1.1 Verificar Dockerfile

El Dockerfile ya está optimizado para producción con:
- Multi-stage build
- Health checks integrados
- Optimizaciones JVM para contenedores
- Caché de capas

```bash
# Verificar que Dockerfile existe
cat Dockerfile
```

### 1.2 Verificar docker-compose.prod.yml

```bash
# Archivo de configuración de producción
cat docker-compose.prod.yml
```

### 1.3 Configurar .env

```bash
# Copiar plantilla
cp .env.example .env

# Editar con valores reales (NO commitar)
# Importante: Generar JWT_SECRET seguro
openssl rand -base64 64 > jwt_secret.txt
```

## 2. Configuración en Dokploy

### 2.1 Crear Nuevo Proyecto

1. Acceder a [Dokploy Dashboard](https://dokploy.com/dashboard)
2. Click en "New Project"
3. Seleccionar "Docker Compose" como tipo de proyecto
4. Conectar repositorio Git

### 2.2 Configurar Aplicación

**General Settings:**
- **Nombre:** ReseñaApp
- **Descripción:** Plataforma de reseñas de negocios
- **Rama:** `master` (o `release` para staging)

**Docker Compose:**
- **Path:** `docker-compose.prod.yml`
- **Auto-deploy:** Habilitado ✓
- **Auto-update:** Habilitado ✓

**Variables de Entorno:**

En Dokploy, agregar las siguientes variables:

```env
# Database
DB_USER=resena
DB_PASSWORD=<generate-secure-password>
DB_NAME=resena_db
DB_POOL_SIZE=20

# Security
JWT_SECRET=<generate-with: openssl rand -base64 64>
JWT_EXPIRATION=86400

# OAuth2
GOOGLE_CLIENT_ID=<from-google-console>
GOOGLE_CLIENT_SECRET=<from-google-console>
FACEBOOK_CLIENT_ID=<from-facebook-dev>
FACEBOOK_CLIENT_SECRET=<from-facebook-dev>

# URLs
FRONTEND_URL=https://tu-dominio.com
APP_PORT=8080

# Resources
CPU_LIMIT=2
MEMORY_LIMIT=1024M
```

### 2.3 Configurar Dominio

**Si tienes dominio propio:**

1. En Dokploy, ir a "Domains"
2. Click "Add Domain"
3. Ingresar tu dominio (ej: `resena.tu-dominio.com`)
4. Apuntar CNAME de tu DNS a: `dokploy.app` (o el valor que Dokploy proporcione)

**Ejemplo DNS (Cloudflare):**
```
Type: CNAME
Name: resena
Content: c1234567.dokploy.app
TTL: Auto
Proxy: Off (o On si deseas)
```

### 2.4 Configurar Certificado SSL

Dokploy proporciona SSL automático vía Let's Encrypt:
- Seleccionar "Auto" en SSL settings
- Confirmación automática del dominio

## 3. Ports y Networking

### Puertos Expuestos

| Servicio | Puerto | Acceso | Notas |
|----------|--------|--------|-------|
| App (Spring Boot) | 8080 | HTTP | Redirigido a HTTPS en prod |
| PostgreSQL | 5432 | Interno | Solo dentro del contenedor |

### Health Checks

Dokploy monitorea automáticamente:

```
GET http://localhost:8080/actuator/health
```

## 4. Proceso de Despliegue

### 4.1 Despliegue Automático (Recomendado)

1. Push a rama configurada (`master`)
2. GitHub Actions ejecuta CI/CD
3. Dokploy recibe webhook y auto-despliega
4. Health checks validan la aplicación

```bash
# Para desplegar
git push origin master
```

### 4.2 Despliegue Manual

1. En Dokploy Dashboard
2. Click "Deploy Now"
3. Esperar a que los contenedores inicien

### 4.3 Logs en Tiempo Real

```bash
# En Dokploy UI, ir a "Logs"
# O usando CLI (si está disponible):
dokploy logs -f resena-app
```

## 5. GitFlow Integration

Esta app usa GitFlow, que se integra perfectamente con Dokploy:

```
feature/* → dev branch
    ↓
dev → release branch (Dokploy: Staging)
    ↓
release → qa branch (Dokploy: QA Testing)
    ↓
release → master branch (Dokploy: Production)
```

### Configurar Múltiples Entornos en Dokploy

**Entorno de Staging (rama: release)**
- Variables: `SPRING_ENVIRONMENT=staging`
- Dominio: `staging.tu-dominio.com`
- CPU: 1 core, Memory: 512MB

**Entorno de Producción (rama: master)**
- Variables: `SPRING_ENVIRONMENT=production`
- Dominio: `tu-dominio.com`
- CPU: 2 cores, Memory: 1024MB

## 6. Monitoreo y Mantenimiento

### 6.1 Health Checks

Dokploy monitorea automáticamente:
```
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

### 6.2 Base de datos

PostgreSQL se persistent automáticamente con volumen `postgres_data`:
- Backups automáticos recomendados
- Retención: Configurar según políticas de tu empresa

### 6.3 Escalado

Dokploy soporta escalado horizontal:
```yaml
# En docker-compose.prod.yml, usar replicas:
services:
  app:
    deploy:
      replicas: 3
```

## 7. Troubleshooting

### App no inicia

```bash
# Ver logs
Dokploy Dashboard → Logs → Ver últimos 100 líneas

# Verificar variables de entorno
Dokploy Dashboard → Settings → Environment Variables
```

### Conexión a BD falla

```bash
# Verificar que postgres está healthy
DB_HOST=postgres
DB_PORT=5432
PGPASSWORD=$DB_PASSWORD psql -h postgres -U $DB_USER $DB_NAME -c "SELECT 1"
```

### OAuth2 no funciona

```bash
# Verificar credenciales:
1. Google Console → Authorized redirect URIs
   https://tu-dominio.com/login/oauth2/code/google
   
2. Facebook App → Settings → Valid OAuth Redirect URIs
   https://tu-dominio.com/login/oauth2/code/facebook
```

### Performance lento

1. Aumentar CPU/Memory en Dokploy
2. Revisar logs: `LOGGING_LEVEL_ROOT=DEBUG`
3. Analizar metrics en `/actuator/metrics`

## 8. Backup y Recovery

### Backup Automático de BD

Configurar en Dokploy:
```bash
# Frecuencia: Diaria
# Retención: 30 días
# Destino: S3-compatible (opcional)
```

### Restore desde Backup

1. Dokploy Dashboard → PostgreSQL → Backups
2. Seleccionar fecha
3. Click "Restore"

## 9. Seguridad

### Checklist de Seguridad Antes de Producción

- [ ] JWT_SECRET generado con `openssl rand -base64 64`
- [ ] DB_PASSWORD es fuerte (16+ caracteres, mezcla de tipos)
- [ ] OAuth2 credentials son válidas
- [ ] SSL/TLS activado en Dokploy
- [ ] CORS configurado correctamente en `application.yml`
- [ ] Rate limiting habilitado (Spring Security)
- [ ] Logs no exponen datos sensibles

### Rotación de Secretos

Mensualmente:
```bash
# 1. Generar nuevo JWT_SECRET
openssl rand -base64 64

# 2. Actualizar en Dokploy
# 3. Redeploy automático

# 4. Cambiar DB_PASSWORD
# (Requiere migraciones en RDS/Dokploy)
```

## 10. Costos Estimados (Dokploy)

Basado en plan estándar:

| Componente | Recursos | Costo/mes |
|-----------|----------|----------|
| App Container | 2 CPU, 1GB RAM | ~$10-15 |
| PostgreSQL | 1 CPU, 1GB RAM | ~$10-15 |
| Storage (100GB) | - | ~$5 |
| **Total** | - | **~$25-35** |

*Precios aproximados, verificar en [Dokploy Pricing](https://dokploy.com/pricing)*

## 11. Soporte y Recursos

- **Documentación Dokploy:** https://dokploy.com/docs
- **GitHub Issues:** https://github.com/tu-org/ResenaApp/issues
- **Discord Komunidad:** https://dokploy.com/discord

---

**Última actualización:** Abril 2026
**Versión app:** 0.0.1
