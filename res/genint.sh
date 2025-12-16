#!/bin/bash

set -e

ROOT_CA="$1"
ROOT_CA_PASS="$2"
INT_NAME="$3"
INT_PASS="$4"



[ -z "$ROOT_CA" ] || [ -z "$ROOT_CA_PASS" ] || [ -z "$INT_NAME" ] || [ -z "$INT_PASS" ] && {
  echo "Usage: $0 <root_ca_name> <root_ca_password> <intermediate_name> <intermediate_password>"
  exit 1
}

SUBJ="/C=PT/ST=Braga/L=Braga/O=UMinho/OU=Grupo/CN=csr.local"

# Generate intermediate private key
openssl genrsa -aes256 -passout pass:"$INT_PASS" -out "$INT_NAME.key" 2048

# Create CSR
openssl req -new \
  -key "$INT_NAME.key" \
  -passin pass:"$INT_PASS" \
  -subj "$SUBJ" \
  -out "$INT_NAME.csr"

# Extensions for intermediate CA
EXT=$(mktemp)
cat > "$EXT" <<EOF
basicConstraints = critical, CA:TRUE, pathlen:0
keyUsage = critical, keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer
crlDistributionPoints = URI:socket://localhost:1888/CRL
EOF

# Sign intermediate with root CA
openssl x509 -req \
  -in "$INT_NAME.csr" \
  -CA "$ROOT_CA.crt" \
  -CAkey "$ROOT_CA.key" \
  -passin pass:"$ROOT_CA_PASS" \
  -CAcreateserial \
  -sha256 \
  -days 180 \
  -extfile "$EXT" \
  -out "$INT_NAME.crt"

rm -f "$INT_NAME.csr" "$EXT"

echo "Intermediate CA created:"
echo "  $INT_NAME.key"
echo "  $INT_NAME.crt"
