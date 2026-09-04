#!/bin/bash

# Verifica che sia stato passato un file come argomento
if [ -z "$1" ]; then
    echo "Uso: $0 <percorso_del_documento>"
    exit 1
fi

FILE="$1"

# Verifica che il file esista
if [ ! -f "$FILE" ]; then
    echo "Errore: il file '$FILE' non esiste."
    exit 1
fi

# 1. Calcolo del Digest in Esadecimale (raw binary -> hex, 64 caratteri)
HEX_DIGEST=$(openssl dgst -sha256 "$FILE" | awk '{print $2}')

# 2. Calcolo del Digest in Base64 (raw binary -> base64, 44 caratteri)
BASE64_DIGEST=$(openssl dgst -sha256 -binary "$FILE" | openssl base64)

# Visualizzazione dei risultati e verifica delle lunghezze
echo "--- Risultati Hash SHA-256 ---"
echo "File analizzato: $FILE"
echo ""
echo "Digest Esadecimale (${#HEX_DIGEST} caratteri):"
echo "$HEX_DIGEST"
echo ""
echo "Digest Base64 (${#BASE64_DIGEST} caratteri):"
echo "$BASE64_DIGEST"