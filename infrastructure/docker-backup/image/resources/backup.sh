#!/bin/bash

set -xo pipefail

function main() {

    start-maintenance.sh

    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY
    file_env POSTGRES_DB
    file_env POSTGRES_PASSWORD
    file_env POSTGRES_USER
    file_env RESTIC_DAYS_TO_KEEP 14

    backup-roles 'oc_'
    backup-db-dump
    backup-directory '/var/backups/'

    end-maintenance.sh
}

source /usr/local/lib/functions.sh
source /usr/local/lib/pg-functions.sh
source /usr/local/lib/file-functions.sh

main
