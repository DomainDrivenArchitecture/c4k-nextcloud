#!/bin/bash

set -Eeox pipefail

function main() {

#    start-maintenance.sh

    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY

    file_env POSTGRES_DB
    file_env POSTGRES_PASSWORD
    file_env POSTGRES_USER

echo re-add lines !!!
#    drop-create-db

    #skip for fast testing
    #restore-roles
    #restore-db
#    restore-directory '/var/backups/'

}

source /usr/local/lib/functions.sh
source /usr/local/lib/pg-functions.sh
source /usr/local/lib/file-functions.sh

main

