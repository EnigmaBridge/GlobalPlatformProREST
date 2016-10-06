#!/usr/bin/env bash
KEYSTORE="onetimekeystore.jks"
if [ ! -f "${KEYSTORE}" ]; then
    echo "Generating key store ${KEYSTORE} at "`pwd`
    keytool -genkey -keyalg RSA \
        -alias tomcat \
        -keystore "${KEYSTORE}" \
        -storepass password \
        -keypass password \
        -validity 3600 \
        -keysize 2048 \
        -dname 'CN=localhost, OU=EnigmaBridge, O=EnigmaBridge, L=Cambridge, C=GB'
fi
