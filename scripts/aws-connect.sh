#!/bin/bash
# ─────────────────────────────────────────────────────────────
# aws-connect.sh — Conectarse rápido al servidor EC2
# Uso: ./scripts/aws-connect.sh
# ─────────────────────────────────────────────────────────────

KEY_FILE="${AWS_KEY_FILE:-./reseniaapp-key.pem}"
EC2_IP="${AWS_EC2_IP:-}"
EC2_USER="${AWS_EC2_USER:-ec2-user}"

if [ -z "$EC2_IP" ]; then
  echo "❌ Configura la IP de tu EC2:"
  echo "   export AWS_EC2_IP=54.123.45.67"
  echo "   export AWS_KEY_FILE=./reseniaapp-key.pem"
  echo "   ./scripts/aws-connect.sh"
  exit 1
fi

if [ ! -f "$KEY_FILE" ]; then
  echo "❌ No se encontró el archivo de clave: $KEY_FILE"
  exit 1
fi

chmod 400 "$KEY_FILE"
echo "🔗 Conectando a EC2: $EC2_USER@$EC2_IP"
ssh -i "$KEY_FILE" "$EC2_USER@$EC2_IP"
