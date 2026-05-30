# 📱 Guía Completa: OTP y Verificación de Teléfono

## Descripción General

El sistema de verificación de teléfono mediante OTP (One-Time Password) está completamente implementado con soporte para Twilio en producción y logs en desarrollo.

## Arquitectura

### 1. **SmsService** (`SmsService.java`)
Servicio centralizado para enviar SMS usando Twilio.

**Características:**
- ✅ Modo desarrollo: Logs en consola (sin costo)
- ✅ Modo producción: Envío real con Twilio
- ✅ Manejo robusto de errores
- ✅ Mensajes formatados con emojis para mejor UX

**Métodos:**
- `sendOtpCode(phoneNumber, code)` - Envía código OTP
- `sendNotification(phoneNumber, message)` - Envía notificaciones

**Ejemplo de log en DEV:**
```
📱 OTP ENVIADO A +573001112233: 123456
```

### 2. **OtpJwtService** (Mejorado)
Genera tokens JWT encriptados con OTP hash y **ENVÍA el código via SMS**.

**Flujo:**
1. Genera código aleatorio 6 dígitos (100000-999999)
2. Hashea el código con BCrypt (10 rounds)
3. **LLAMA AL SMSSERVICE** para enviar el código
4. Crea JWT con:
   - `subject`: Número de teléfono
   - `otp_hash`: Código hasheado
   - `type`: "OTP_VERIFY"
   - TTL: 5 minutos
5. Encripta todo con AES-256-GCM
6. Retorna token serializado

### 3. **AuthController** (Mejorado con logs y mejor respuesta)

**Endpoints:**

#### POST `/api/auth/otp/request`
Solicita un nuevo OTP para un teléfono.

**Request:**
```json
{
  "phone": "+573001112233"
}
```

**Response (200 OK):**
```json
{
  "otpToken": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "message": "Código OTP enviado a tu teléfono",
  "expiresIn": 300
}
```

**Errores:**
- `500` - Error generando OTP

#### POST `/api/auth/otp/verify`
Verifica el OTP y marca el teléfono como verificado.

**Request:**
```json
{
  "otpToken": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "code": "123456"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "message": "Teléfono verificado exitosamente",
  "user": {
    "id": "uuid...",
    "email": "user@example.com",
    "phone": "+573001112233",
    "phoneVerified": true,
    "status": "ACTIVE"
  }
}
```

**Errores:**
- `401 Unauthorized` - Usuario no autenticado
- `400 Bad Request` - Código inválido
- `410 Gone` - Token expirado
- `409 Conflict` - Token ya utilizado
- `500` - Error interno

## Configuración

### Variables de Entorno Requeridas en PRODUCCIÓN

```bash
# Twilio (obligatorio para SMS real)
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your_auth_token_here
TWILIO_FROM_PHONE=+1234567890  # Número de teléfono de Twilio

# OTP JWT Secret (obligatorio)
OTP_JWT_SECRET=64_hex_characters_here_256bit_key...

# JWT Secret (obligatorio)
JWT_SECRET=your_jwt_secret_here
```

### En DESARROLLO

Por defecto, el sistema:
- **NO requiere** credenciales de Twilio
- Logea los códigos OTP en consola
- Ejemplo de log:
  ```
  📱 OTP ENVIADO A +573001112233: 456789
  ```

Para usar Twilio en desarrollo, configura las variables de entorno.

### application.yml

```yaml
twilio:
  account-sid: ${TWILIO_ACCOUNT_SID:}
  auth-token: ${TWILIO_AUTH_TOKEN:}
  from-phone: ${TWILIO_FROM_PHONE:}
```

## Flujo Completo de Uso

### Escenario: Usuario Verificando su Teléfono

```
┌─────────────────┐
│  Usuario logueado
│  Ingresa a /verify-phone
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ 1. Frontend solicita OTP            │
│ POST /api/auth/otp/request          │
│ { "phone": "+573001112233" }        │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ 2. Backend:                         │
│ - Genera OTP: 123456                │
│ - Hashea: bcrypt(123456)            │
│ - Envía SMS via Twilio              │
│ - Retorna otpToken                  │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ 📱 Usuario recibe SMS:              │
│ "Tu código: 123456"                 │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ 3. Usuario ingresa código (6 dígitos)
│    Frontend envía:                  │
│ POST /api/auth/otp/verify          │
│ {                                   │
│   "otpToken": "...",                │
│   "code": "123456"                  │
│ }                                   │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ 4. Backend:                         │
│ - Desencripta token                 │
│ - Verifica firma JWT                │
│ - Compara código con hash           │
│ - Marca teléfono como verificado    │
│ - Retorna nuevo JWT                 │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ ✅ Teléfono Verificado              │
│ Usuario redirigido a /               │
└─────────────────────────────────────┘
```

## Seguridad

### Protecciones Implementadas

1. **Hash BCrypt:** Código nunca se almacena en plain text
2. **Encriptación JWT:** Token encriptado con AES-256-GCM
3. **TTL Corto:** OTP expira en 5 minutos
4. **Blacklist:** Tokens usados no se pueden reutilizar
5. **Validación E.164:** Teléfono debe estar en formato internacional

### Attack Prevention

| Ataque | Mitigación |
|--------|-----------|
| Fuerza bruta (OTP) | TTL de 5 min, blacklist de tokens |
| Replay attack | Token blacklist inmediata post-uso |
| Man-in-the-middle | HTTPS requerido en producción |
| Teléfono falso | E.164 format validation |

## Testing en Desarrollo

### 1. Solicitar OTP

```bash
curl -X POST http://localhost:8080/api/auth/otp/request \
  -H "Content-Type: application/json" \
  -d '{"phone": "+573001112233"}'
```

**Esperado en logs:**
```
📱 OTP ENVIADO A +573001112233: 456789
```

### 2. Copiar el código de los logs

Ejemplo: `456789`

### 3. Obtener otpToken de la respuesta

```json
{
  "otpToken": "eyJ0eXAi...",
  "message": "Código OTP enviado a tu teléfono",
  "expiresIn": 300
}
```

### 4. Verificar el OTP (requiere JWT token de usuario)

```bash
curl -X POST http://localhost:8080/api/auth/otp/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "otpToken": "eyJ0eXAi...",
    "code": "456789"
  }'
```

## Logs Importantes

### Success
```
✅ OTP generado y enviado a: +573001112233
✅ Teléfono verificado exitosamente para usuario: user@example.com
📱 OTP ENVIADO A +573001112233: 123456
```

### Warnings
```
⚠️ SMS no enviado - Twilio no configurado. Código: 456789
⏱️ OTP expirado para usuario: user@example.com
❌ OTP inválido para usuario: user@example.com
```

## Troubleshooting

### "No se pudo generar el código OTP"
**Causas:**
- La base de datos no está disponible
- El servicio OtpJwtService falló

**Solución:**
```bash
# Verificar logs
docker compose logs backend | grep "Error generando OTP"
```

### "SMS no enviado - Twilio no configurado"
**Esto es NORMAL en desarrollo.** El código se logea en consola.

**Para usar Twilio real:**
1. Obtener credenciales de [Twilio](https://twilio.com)
2. Configurar variables de entorno
3. Reiniciar la aplicación

### "Código inválido" pero el código es correcto
**Posibles causas:**
- El código expiró (> 5 minutos)
- El token ya fue usado
- Espacios en blanco en el código

**Solución:**
```
Solicitar un código nuevo (nuevo OTP request)
```

## Próximas Mejoras

- [ ] Reintentos limitados (máx 3)
- [ ] Incremento de delay entre intentos
- [ ] Notificación de actividad sospechosa
- [ ] Múltiples teléfonos por usuario
- [ ] Backup códigos sin SMS

---

**Última actualización:** Abril 2026
**Versión:** 2.0 con Twilio SMS
