#!/bin/bash

set -e

INT_NAME="$1"
INT_PASS="$2"
LEAF_NAME="$3"
LEAF_PASS="$4"

[ -z "$INT_NAME" ] || [ -z "$INT_PASS" ] || [ -z "$LEAF_NAME" ] || [ -z "$LEAF_PASS" ] && {
  echo "Usage: $0 <intermediate_name> <intermediate_password> <leaf_name> <leaf_password>"
  exit 1
}

SUBJ="/C=PT/ST=Porto/L=Porto/O=UMinho/OU=Grupo/CN=${LEAF_NAME}"

# Generate leaf private key
openssl genrsa -aes256 -passout pass:"$LEAF_PASS" -out "$LEAF_NAME.key" 2048

# Create CSR
openssl req -new \
  -key "$LEAF_NAME.key" \
  -passin pass:"$LEAF_PASS" \
  -subj "$SUBJ" \
  -out "$LEAF_NAME.csr"

# Leaf certificate extensions
EXT=$(mktemp)
cat > "$EXT" <<EOF
basicConstraints = critical, CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth, serverAuth
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
EOF

# Sign leaf with intermediate CA
openssl x509 -req \
  -in "$LEAF_NAME.csr" \
  -CA "$INT_NAME.crt" \
  -CAkey "$INT_NAME.key" \
  -passin pass:"$INT_PASS" \
  -CAcreateserial \
  -sha256 \
  -days 365 \
  -extfile "$EXT" \
  -out "$LEAF_NAME.crt"

rm -f "$LEAF_NAME.csr" "$EXT"

echo "Leaf certificate created:"
echo "  $LEAF_NAME.key"
echo "  $LEAF_NAME.crt"
