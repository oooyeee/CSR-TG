#!/usr/bin/env bash

set -e

# Arguments
CERT_NAME="$1"
CERT_PASS="$2"

if [ -z "$CERT_NAME" ] || [ -z "$CERT_PASS" ]; then
  echo "Usage: $0 <cert_name> <cert_password>"
  exit 1
fi

# Paths
RES_DIR="./res"
KEY_FILE="$RES_DIR/${CERT_NAME}.key"
CSR_FILE="$RES_DIR/${CERT_NAME}.csr"
CRT_FILE="$RES_DIR/${CERT_NAME}.crt"

SIGNER_KEY="$RES_DIR/signer.key"
SIGNER_CRT="$RES_DIR/signer.crt"
SIGNER_PASS="sign"

mkdir -p "$RES_DIR"

# Generate private key
openssl genrsa \
  -aes256 \
  -passout pass:"$CERT_PASS" \
  -out "$KEY_FILE" 2048

# Generate CSR
openssl req \
  -new \
  -key "$KEY_FILE" \
  -passin pass:"$CERT_PASS" \
  -out "$CSR_FILE" \
  -subj "/CN=${CERT_NAME}"

# Sign CSR with parent certificate
openssl x509 \
  -req \
  -in "$CSR_FILE" \
  -CA "$SIGNER_CRT" \
  -CAkey "$SIGNER_KEY" \
  -CAcreateserial \
  -passin pass:"$SIGNER_PASS" \
  -out "$CRT_FILE" \
  -days 365 \
  -sha256

# Cleanup
rm -f "$CSR_FILE"

echo "Certificate generated:"
echo "  Private key: $KEY_FILE"
echo "  Certificate: $CRT_FILE"

