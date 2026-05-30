# ReseñaApp Deployment Checklist

Complete esta lista antes de desplegar a producción.

## 🔒 Seguridad

- [ ] **JWT_SECRET**: Generado con `openssl rand -base64 64`, mínimo 32 caracteres
- [ ] **DB_PASSWORD**: Contraseña segura, mínimo 16 caracteres, mezcla de tipos
- [ ] **OAuth2 Credentials**: Validadas desde Google Console y Facebook App
- [ ] **HTTPS/SSL**: Configurado en Dokploy con Let's Encrypt
- [ ] **CORS**: Configurado solo para dominios autorizados
- [ ] **.env**: NO commitido en Git (está en .gitignore)
- [ ] **Secretos**: NO hardcodeados en código
- [ ] **Rate Limiting**: Habilitado en Spring Security (si aplica)

## 🗄️ Base de datos

- [ ] **PostgreSQL**: Versión 16 validated
- [ ] **Migrations**: Todas las V*.sql ejecutadas sin errores
- [ ] **Backup**: Política de backup definida y probada
- [ ] **Connection Pool**: Configurado correctamente (hikari.maximum-pool-size=20)

## 🚀 Aplicación

- [ ] **Build**: `mvn clean package` completado sin errores
- [ ] **Frontend Build**: `npm run build` completado, dist/ generado
- [ ] **Health Checks**: `/actuator/health` retorna 200 OK
- [ ] **Logs**: No contienen información sensible
- [ ] **Version**: semver actualizado en pom.xml

## 🐳 Docker

- [ ] **Dockerfile**: Multi-stage build, optimizado para prod
- [ ] **Image Size**: < 500MB (idealmente < 300MB)
- [ ] **Security**: No root user, imágenes bases up-to-date
- [ ] **docker-compose.prod.yml**: Validado y probado localmente
- [ ] **Health Checks**: Configurados en docker-compose

## 🌐 Dokploy

- [ ] **Proyecto creado**: En Dokploy Dashboard
- [ ] **Repositorio conectado**: GitHub, GitLab, o Gitea
- [ ] **Branch configurada**: `master` para prod, `release` para staging
- [ ] **Variables de entorno**: Todas configuradas en Dokploy
- [ ] **Dominio**: Apuntado a Dokploy (CNAME record)
- [ ] **SSL Certificate**: Auto-issued via Let's Encrypt
- [ ] **Auto-deploy**: Habilitado (webhook configurado)

## 📡 API & Endpoints

- [ ] **GET /api/businesses**: Funciona sin auth
- [ ] **POST /api/businesses**: Funciona con auth
- [ ] **POST /api/products**: Funciona con auth
- [ ] **GET /api/reviews**: Funciona sin auth
- [ ] **GET /api/admin/**: Funciona solo con rol ADMIN
- [ ] **POST /api/auth/login**: Funciona con email/password
- [ ] **OAuth2**: Login funciona sin errores

## 🔄 CI/CD

- [ ] **GitHub Actions**: Workflows creados y activos
- [ ] **Build Pipeline**: Compila backend y frontend sin errores
- [ ] **Tests**: Pasan antes de desplegar
- [ ] **Docker Build**: Se construye y pushea a registry
- [ ] **Dokploy Webhook**: Integrado en git provider

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
