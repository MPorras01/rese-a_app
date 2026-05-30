# 🚀 Despliegue en AWS — Free Tier

Guía paso a paso para desplegar ReseñaApp en AWS usando servicios **100% gratuitos** durante 12 meses.

## Arquitectura

```
Internet
   │
   ▼
EC2 t2.micro (Free Tier)
├── Docker
│   └── ReseñaApp JAR  ← backend + frontend embebido
│       puerto 8080
└── Nginx (reverse proxy)
    puerto 80/443
        │
        ▼
RDS PostgreSQL t3.micro (Free Tier)
puerto 5432 (solo acceso interno)
```

## Qué es gratis (12 meses desde registro)

| Servicio | Free Tier |
|----------|-----------|
| EC2 t2.micro | 750 horas/mes |
| RDS db.t3.micro PostgreSQL | 750 horas/mes + 20 GB storage |
| EBS (disco EC2) | 30 GB |
| Transferencia de datos saliente | 15 GB/mes |
| Elastic IP | 1 IP gratis si está asociada |

---

## Paso 1 — Crear cuenta AWS

1. Ve a https://aws.amazon.com/free
2. Crea tu cuenta (requiere tarjeta de crédito, no se cobra si usas Free Tier)
3. Activa MFA en tu cuenta root
4. Crea un usuario IAM para trabajar (no uses root)

---

## Paso 2 — Crear RDS PostgreSQL

### 2.1 Ir a RDS en la consola AWS

1. Consola AWS → **RDS** → **Create database**
2. Seleccionar:
   - **Standard create**
   - **PostgreSQL**
   - Versión: **16.x**

### 2.2 Configuración Free Tier

```
Template:          Free tier
DB instance ID:    reseniaapp-db
Master username:   resenias
Master password:   <elige una contraseña segura>

Instance class:    db.t3.micro
Storage:           20 GB gp2 (no habilitar autoscaling)

VPC:               Default VPC
Public access:     NO  ← importante por seguridad
VPC security group: Create new → nombre: "rds-reseniaapp"

Database name:     resenias
Port:              5432
```

### 2.3 Anotar el endpoint

Después de crear (tarda ~5 min), copia el **Endpoint**:
```
reseniaapp-db.xxxxxxxxxx.us-east-1.rds.amazonaws.com
```

---

## Paso 3 — Crear EC2 t2.micro

### 3.1 Ir a EC2

1. Consola AWS → **EC2** → **Launch Instance**

### 3.2 Configuración

```
Name:              reseniaapp-server
AMI:               Amazon Linux 2023 (gratis, recomendado)
Instance type:     t2.micro  ← Free Tier
Key pair:          Create new → nombre: "reseniaapp-key"
                   Tipo: RSA, formato: .pem
                   ⚠️ Descarga y guarda el .pem, no se puede recuperar

Network:           Default VPC
Subnet:            cualquiera
Auto-assign IP:    Enable
Security group:    Create new → nombre: "ec2-reseniaapp"
  Inbound rules:
    SSH    TCP  22    My IP (solo tu IP)
    HTTP   TCP  80    0.0.0.0/0
    HTTPS  TCP  443   0.0.0.0/0
    Custom TCP  8080  0.0.0.0/0  (temporal para pruebas)

Storage:           20 GB gp3
```

### 3.3 Asignar Elastic IP (para IP fija)

1. EC2 → **Elastic IPs** → **Allocate Elastic IP**
2. **Associate** → selecciona tu instancia
3. Anota la IP pública (ej: `54.123.45.67`)

### 3.4 Permitir que EC2 acceda a RDS

En el Security Group de RDS (`rds-reseniaapp`):
```
Inbound rule:
  Type:   PostgreSQL
  Port:   5432
  Source: Security group del EC2 (ec2-reseniaapp)
```

---

## Paso 4 — Configurar EC2

### 4.1 Conectarse por SSH

```bash
# En tu máquina local (Linux/Mac)
chmod 400 reseniaapp-key.pem
ssh -i reseniaapp-key.pem ec2-user@54.123.45.67

# En Windows (PowerShell)
ssh -i reseniaapp-key.pem ec2-user@54.123.45.67
```

### 4.2 Instalar Docker

```bash
# Actualizar paquetes
sudo dnf update -y

# Instalar Docker
sudo dnf install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Cerrar sesión y volver a entrar para que aplique el grupo
exit
ssh -i reseniaapp-key.pem ec2-user@54.123.45.67

# Verificar
docker --version
```

### 4.3 Instalar Docker Compose

```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### 4.4 Instalar Git y clonar el repo

```bash
sudo dnf install -y git

# Clonar tu repositorio
git clone https://github.com/TU_USUARIO/TU_REPO.git reseniaapp
cd reseniaapp
```

---

## Paso 5 — Configurar variables de entorno

### 5.1 Crear el archivo .env en el servidor

```bash
cd ~/reseniaapp
cp .env.example .env
nano .env
```

### 5.2 Contenido del .env en producción

```env
# ── Base de datos (RDS) ──────────────────────────────────────
DB_URL=jdbc:postgresql://reseniaapp-db.xxxxxxxxxx.us-east-1.rds.amazonaws.com:5432/resenias
DB_USERNAME=resenias
DB_PASSWORD=TU_PASSWORD_SEGURO

# ── JWT ──────────────────────────────────────────────────────
# Genera con: openssl rand -base64 64
JWT_SECRET=GENERA_UN_SECRET_LARGO_AQUI_MINIMO_64_CHARS

# ── OTP JWT ──────────────────────────────────────────────────
# Genera con: openssl rand -hex 32
OTP_JWT_SECRET=GENERA_64_CHARS_HEX_AQUI

# ── OAuth2 (opcional para pruebas) ───────────────────────────
GOOGLE_CLIENT_ID=placeholder-not-real
GOOGLE_CLIENT_SECRET=placeholder-not-real
FACEBOOK_CLIENT_ID=placeholder-not-real
FACEBOOK_CLIENT_SECRET=placeholder-not-real

# ── App ──────────────────────────────────────────────────────
APP_FRONTEND_URL=http://54.123.45.67
```

Guarda con `Ctrl+O`, `Enter`, `Ctrl+X`.

### 5.3 Generar secrets seguros

```bash
# JWT_SECRET
openssl rand -base64 64

# OTP_JWT_SECRET (debe ser exactamente 64 chars hex = 32 bytes)
openssl rand -hex 32
```

---

## Paso 6 — Crear docker-compose para AWS

```bash
nano ~/reseniaapp/docker-compose.aws.yml
```

Pega el contenido del archivo `docker-compose.aws.yml` que está en el repo.

---

## Paso 7 — Build y arrancar

```bash
cd ~/reseniaapp

# Build de la imagen (tarda ~5 min la primera vez)
docker-compose -f docker-compose.aws.yml build

# Arrancar en background
docker-compose -f docker-compose.aws.yml up -d

# Ver logs
docker-compose -f docker-compose.aws.yml logs -f app
```

### Verificar que funciona

```bash
# Health check
curl http://localhost:8080/actuator/health

# API pública
curl http://localhost:8080/api/businesses
```

Desde tu navegador: `http://54.123.45.67`

---

## Paso 8 — Actualizaciones futuras

Cada vez que hagas cambios en el código:

```bash
cd ~/reseniaapp

# Traer cambios
git pull origin main

# Rebuild y restart
docker-compose -f docker-compose.aws.yml up --build -d

# Ver logs
docker-compose -f docker-compose.aws.yml logs -f app
```

O usa el script de deploy automático:

```bash
./scripts/aws-deploy.sh
```

---

## Paso 9 — Dominio propio (opcional)

Si tienes un dominio (ej: en GoDaddy, Namecheap, Cloudflare):

1. Crea un registro A apuntando a tu Elastic IP:
   ```
   Tipo: A
   Nombre: @ (o subdominio)
   Valor: 54.123.45.67
   TTL: 300
   ```

2. Instala Nginx + Certbot para HTTPS:
   ```bash
   sudo dnf install -y nginx certbot python3-certbot-nginx
   sudo systemctl start nginx
   sudo certbot --nginx -d tudominio.com
   ```

---

## Monitoreo básico

```bash
# Estado de contenedores
docker ps

# Uso de recursos
docker stats

# Logs en tiempo real
docker-compose -f docker-compose.aws.yml logs -f

# Espacio en disco
df -h

# Memoria
free -h
```

---

## Costos después del Free Tier (mes 13+)

| Servicio | Costo aprox/mes |
|----------|----------------|
| EC2 t2.micro | ~$8.50 |
| RDS db.t3.micro | ~$13 |
| EBS 20GB | ~$1.60 |
| Elastic IP | ~$3.60 |
| **Total** | **~$27/mes** |

Para reducir costos después del Free Tier, considera migrar a:
- **Railway** (~$5/mes todo incluido)
- **Render** (plan gratuito con limitaciones)
- **Fly.io** (~$3-5/mes)

---

## Troubleshooting

### La app no arranca

```bash
# Ver logs detallados
docker-compose -f docker-compose.aws.yml logs app

# Verificar variables de entorno
docker-compose -f docker-compose.aws.yml config
```

### No puedo conectar a RDS

```bash
# Probar conexión desde EC2
sudo dnf install -y postgresql15
psql -h reseniaapp-db.xxx.rds.amazonaws.com -U resenias -d resenias

# Si falla, verificar:
# 1. Security group de RDS permite el SG de EC2
# 2. RDS está en la misma VPC que EC2
# 3. Usuario y contraseña correctos
```

### Puerto 80 no responde

```bash
# Verificar que el security group de EC2 tiene puerto 80 abierto
# En la consola AWS: EC2 → Security Groups → ec2-reseniaapp → Inbound rules
```
