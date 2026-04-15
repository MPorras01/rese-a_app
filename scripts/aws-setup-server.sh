#!/bin/bash
# ─────────────────────────────────────────────────────────────
# aws-setup-server.sh — Setup inicial de EC2 Amazon Linux 2023
# Ejecutar UNA SOLA VEZ después de crear la instancia
#
# Uso desde tu máquina local:
#   ssh -i reseniaapp-key.pem ec2-user@TU_IP "bash -s" < scripts/aws-setup-server.sh
#
# O copiarlo al servidor y ejecutarlo:
#   scp -i reseniaapp-key.pem scripts/aws-setup-server.sh ec2-user@TU_IP:~/
#   ssh -i reseniaapp-key.pem ec2-user@TU_IP "chmod +x aws-setup-server.sh && ./aws-setup-server.sh"
# ─────────────────────────────────────────────────────────────
set -e

echo "🚀 Setup inicial de EC2 para ReseñaApp"
echo "======================================="

# 1. Actualizar sistema
echo "📦 Actualizando sistema..."
sudo dnf update -y

# 2. Instalar herramientas básicas
echo "🔧 Instalando herramientas..."
sudo dnf install -y git curl wget htop nano

# 3. Instalar Docker
echo "🐳 Instalando Docker..."
sudo dnf install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# 4. Instalar Docker Compose plugin
echo "🐳 Instalando Docker Compose..."
COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep '"tag_name"' | cut -d'"' -f4)
sudo curl -SL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-x86_64" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# También instalar como plugin de Docker
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -SL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-x86_64" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# 5. Configurar swap (importante para t2.micro con 1GB RAM)
echo "💾 Configurando swap (1GB)..."
if [ ! -f /swapfile ]; then
  sudo fallocate -l 1G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  echo "   Swap configurado: $(free -h | grep Swap)"
fi

# 6. Configurar límites del sistema para Java
echo "⚙️  Configurando límites del sistema..."
cat << 'EOF' | sudo tee /etc/security/limits.d/reseniaapp.conf
ec2-user soft nofile 65536
ec2-user hard nofile 65536
EOF

# 7. Configurar rotación de logs de Docker
echo "📋 Configurando logs de Docker..."
sudo mkdir -p /etc/docker
cat << 'EOF' | sudo tee /etc/docker/daemon.json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF
sudo systemctl restart docker

# 8. Instalar cliente PostgreSQL (para diagnóstico)
echo "🗄️  Instalando cliente PostgreSQL..."
sudo dnf install -y postgresql15 || true

# 9. Clonar el repositorio
echo ""
echo "📂 Ahora clona tu repositorio:"
echo "   git clone https://github.com/TU_USUARIO/TU_REPO.git reseniaapp"
echo "   cd reseniaapp"
echo "   cp .env.example .env"
echo "   nano .env  # configura las variables"
echo ""
echo "✅ Setup completado. Cierra sesión y vuelve a entrar para aplicar cambios de grupo Docker:"
echo "   exit"
echo "   ssh -i reseniaapp-key.pem ec2-user@TU_IP"
