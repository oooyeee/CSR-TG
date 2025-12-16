#!/bin/bash

set -e

CA_NAME="$1"
CA_PASS="$2"

if [ -z "$CA_NAME" ] || [ -z "$CA_PASS" ]; then
  echo "Usage: $0 <ca_name> <ca_password>"
  exit 1
fi

SUBJ="/C=PT/ST=Guimaraes/L=Guimaraes City/O=UMinho/OU=Grupo/CN=csr.local"

openssl genrsa \
  -aes256 \
  -passout pass:"$CA_PASS" \
  -out "$CA_NAME.key" \
  4096

openssl req \
  -x509 \
  -new \
  -key "$CA_NAME.key" \
  -passin pass:"$CA_PASS" \
  -subj "$SUBJ" \
  -sha256 \
  -days 720 \
  -out "$CA_NAME.crt"

openssl x509 -in "$CA_NAME.crt" -noout -text

echo "DONE!!"
