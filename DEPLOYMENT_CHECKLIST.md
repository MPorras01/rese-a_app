# ReseñaApp Deployment Checklist

Complete esta lista antes de desplegar a producción.

## ✅ Recently Fixed (June 2026)

- [x] **Health Checks**: `/actuator/health` y `/actuator/info` ahora permitAll() en SecurityConfig
- [x] **Log Levels**: LOGGING_LEVEL_COM_RESENIAS cambió de DEBUG a INFO en production
- [x] **Shutdown Endpoint**: Deshabilitado en application-prod.yml (`enabled: false`)
- [x] **CI Consistency**: Arreglado skipFrontend en workflows (usaba -Dskip.frontend, ahora -DskipFrontend)
- [x] **Vercel Config**: Agregado `"rootDirectory": "frontend"` en vercel.json
- [x] **Dokploy Workflow**: Mejorado para soportar staging y producción dinámicamente

## 🔒 Seguridad

- [ ] **JWT_SECRET**: Generado con `openssl rand -base64 64`, mínimo 32 caracteres
- [ ] **DB_PASSWORD**: Contraseña segura, mínimo 16 caracteres, mezcla de tipos
- [ ] **OAuth2 Credentials**: Validadas desde Google Console y Facebook App
- [ ] **HTTPS/SSL**: Configurado en Dokploy con Let's Encrypt
- [x] **CORS**: Configurable dinámicamente via `app.frontend-url` en SecurityConfig
- [ ] **.env**: NO commitido en Git (está en .gitignore)
- [ ] **Secretos**: NO hardcodeados en código (todas las variables están externalizadas)
- [x] **Health Endpoints**: `/actuator/health` accesible sin autenticación (para monitoring)
- [x] **Shutdown Endpoint**: Deshabilitado en producción
- [x] **Log Levels**: DEBUG solo en dev, INFO en staging/prod

## 🗄️ Base de datos

- [ ] **PostgreSQL**: Versión 16 en docker-compose.prod.yml
- [ ] **Migrations**: Todas las V*.sql ejecutadas sin errores (Flyway auto-ejecuta)
- [ ] **Backup**: Política de backup definida en Dokploy
- [x] **Connection Pool**: Configurado (hikari.maximum-pool-size=20 por defecto, override via env)

## 🚀 Aplicación

- [x] **Build**: `mvn clean package` completado sin errores (validado 2026-06-12)
- [x] **Frontend Build**: `npm run build` completado, dist/ generado
- [x] **Health Checks**: `/actuator/health` retorna 200 OK (permitAll en SecurityConfig)
- [x] **Logs**: Configurados por env, INFO en prod
- [ ] **Version**: semver actualizado en pom.xml (actualmente 0.0.1-SNAPSHOT)

## 🐳 Docker

- [x] **Dockerfile**: Multi-stage build, optimizado para prod (57MB JAR incluye Vue SPA)
- [x] **Image Size**: Razonable (~300MB con Alpine base)
- [x] **Security**: No root user, imágenes bases (Alpine + Eclipse Temurin)
- [x] **docker-compose.prod.yml**: Validado (healthcheck, restart policy, limites de resources)
- [x] **Health Checks**: Configurados en docker-compose y accesibles sin auth

## 🌐 Dokploy

- [ ] **Proyecto creado**: En Dokploy Dashboard
- [ ] **Repositorio conectado**: GitHub con webhook automático
- [x] **Branch configurada**: `master` para prod, `release` para staging (workflow lo maneja dinámicamente)
- [ ] **Variables de entorno**: Todas configuradas en Dokploy Dashboard
  - DB_USER, DB_PASSWORD, DB_NAME
  - JWT_SECRET, JWT_EXPIRATION
  - SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE (mín 20)
  - GOOGLE_CLIENT_ID/SECRET, FACEBOOK_CLIENT_ID/SECRET
  - APP_FRONTEND_URL (ej: https://resena.com)
  - LOG_LEVEL=INFO (production)
  - SPRING_PROFILES=docker,prod
- [ ] **Dominio**: Apuntado a Dokploy (CNAME record)
- [ ] **SSL Certificate**: Auto-issued via Let's Encrypt
- [x] **Auto-deploy**: Habilitado via deploy-to-dokploy.yml workflow

## 📡 API & Endpoints

- [ ] **GET /api/businesses**: Funciona sin auth
- [ ] **POST /api/businesses**: Funciona con auth + JWT
- [ ] **GET /api/reviews**: Funciona sin auth
- [ ] **GET /api/admin/**: Funciona solo con rol ADMIN
- [ ] **POST /api/auth/login**: Funciona con email/password válido
- [ ] **POST /api/auth/otp/****: Funciona para verificación SMS
- [ ] **OAuth2 (Google/Facebook)**: Login + callback funcionan sin errores
- [x] **GET /actuator/health**: Accesible sin auth (para monitoring)
- [x] **GET /actuator/info**: Accesible sin auth (info básica)

## 🔄 CI/CD

- [x] **GitHub Actions**: 5 workflows creados y activos
  - ci-build.yml: Build backend + frontend (feature/dev/release)
  - docker-build.yml: Docker image build (master/release/tags)
  - deploy-to-dokploy.yml: Deploy automático a Dokploy (master→prod, release→staging)
  - qa-validation.yml: Smoke tests en rama qa
  - merge-dev-to-release.yml: Auto-merge y release tagging
- [x] **Build Pipeline**: Compila backend (Maven) y frontend (npm/Vite) sin errores
- [ ] **Tests**: Smoke tests en qa-validation.yml (actualmente no ejecutan, agregar backend/src/test)
- [x] **Docker Build**: Se construye y cache-optimizado con BuildKit
- [x] **Dokploy Webhook**: Integrado via deploy-to-dokploy.yml (soporta staging y prod)

## 📊 Monitoramiento

- [ ] **Health Endpoint**: `/actuator/health` monitorizado
- [ ] **Logs**: Colectados y accesibles en Dokploy
- [ ] **Metrics**: `/actuator/metrics` habilitado
- [ ] **Alertas**: Configuradas (opcional)

## 🎯 Pre-Deployment

```bash
# 1. Validar configuración local
docker compose -f docker-compose.prod.yml up --abort-on-container-exit

# 2. Verificar endpoints
curl http://localhost:8080/api/businesses
curl http://localhost:8080/actuator/health

# 3. Test de login
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@resena.local","password":"Admin12345!"}' \
  http://localhost:8080/api/auth/login

# 4. Ver logs
docker compose logs -f app

# 5. Limpiar (después de tests)
docker compose down -v
```

## 📝 Deployment Final

```bash
# 1. Crear tag de release
git tag -a v0.0.1 -m "Production Release v0.0.1"
git push origin v0.0.1

# 2. Push a rama master (trigger automático en Dokploy)
git push origin master

# 3. Esperar auto-deploy en Dokploy (ver logs en dashboard)

# 4. Verificar en producción
curl https://tu-dominio.com/actuator/health
curl https://tu-dominio.com/api/businesses

# 5. Notificar stakeholders
```

## 🚨 Troubleshooting en Producción

Si algo falla:

```bash
# Ver logs en Dokploy UI
Dokploy Dashboard → Logs → Search "ERROR"

# O acceder vía SSH:
ssh dokploy@dokploy-server
docker compose logs -f app

# Rollback a versión anterior:
docker compose down
git revert <bad-commit>
git push origin master
# Dokploy auto-redeploya
```

## ✅ Post-Deployment

- [ ] **Email al equipo**: Notificar deployment exitoso
- [ ] **HealthCheck 30min**: Verificar que sistema está estable
- [ ] **User Testing**: Algunos usuarios finales prueban
- [ ] **Monitor Metrics**: Watch `/actuator/metrics` por 24h
- [ ] **Document Learnings**: Agregar notas a wiki/notion

---

**Checkear esta lista antes de cada despliegue a producción.**

Última modificación: Abril 2026
