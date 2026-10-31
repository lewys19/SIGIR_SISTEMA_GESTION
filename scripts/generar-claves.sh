#!/bin/sh
# Genera el par de claves RSA para firmar (privada) y verificar (pública) los JWT.
set -e
DIR="$(dirname "$0")/../claves"
mkdir -p "$DIR"
if [ -f "$DIR/privateKey.pem" ]; then
  echo "Ya existen claves en $DIR (no se sobrescriben)"; exit 0
fi
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$DIR/privateKey.pem" 2>/dev/null
openssl rsa -in "$DIR/privateKey.pem" -pubout -out "$DIR/publicKey.pem" 2>/dev/null
chmod 644 "$DIR"/*.pem
echo "Claves creadas en $DIR"
