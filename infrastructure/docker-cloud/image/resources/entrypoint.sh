#!/bin/bash

# usage: file_env VAR [DEFAULT]
#    ie: file_env 'XYZ_DB_PASSWORD' 'example'
# (will allow for "$XYZ_DB_PASSWORD_FILE" to fill in the value of
#  "$XYZ_DB_PASSWORD" from a file, especially for Docker's secrets feature)
function file_env() {
    local var="$1"
    local fileVar="${var}_FILE"
    local def="${2:-}"
    local varValue=$(env | grep -E "^${var}=" | sed -E -e "s/^${var}=//")
    local fileVarValue=$(env | grep -E "^${fileVar}=" | sed -E -e "s/^${fileVar}=//")
    if [ -n "${varValue}" ] && [ -n "${fileVarValue}" ]; then
        echo >&2 "error: both $var and $fileVar are set (but are exclusive)"
        exit 1
    fi
    if [ -n "${varValue}" ]; then
        export "$var"="${varValue}"
    elif [ -n "${fileVarValue}" ]; then
        export "$var"="$(cat "${fileVarValue}")"
    elif [ -n "${def}" ]; then
        export "$var"="$def"
    fi
    unset "$fileVar"
}

function main() {
    file_env "FQDN"
    file_env "DB_USERNAME"
    file_env "DB_PASSWORD"

    xmlstarlet ed -L -u "/Server/Service/Connector[@proxyName='{subdomain}.{domain}.com']/@proxyName" \
        -v "$FQDN" /opt/atlassian-cloud-software-standalone/conf/server.xml
    xmlstarlet ed -L -u "/cloud-database-config/jdbc-datasource/username" \
        -v "$DB_USERNAME" /app/dbconfig.xml
    xmlstarlet ed -L -u "/cloud-database-config/jdbc-datasource/password" \
        -v "$DB_PASSWORD" /app/dbconfig.xml

    install -ocloud -gcloud -m660 /app/dbconfig.xml /var/cloud/dbconfig.xml
    /opt/atlassian-cloud-software-standalone/bin/setenv.sh run
    /opt/atlassian-cloud-software-standalone/bin/start-cloud.sh run
}

main
