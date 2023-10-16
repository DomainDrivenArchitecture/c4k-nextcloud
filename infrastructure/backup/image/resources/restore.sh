#!/bin/bash

set -Eeox pipefail

function main() {
    local role_snapshot_id="${1:-latest}"
    local db_snapshot_id="${2:-latest}"
    local file_snapshot_id="${3:-latest}"


    start-maintenance.sh

    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY

    file_env POSTGRES_DB
    file_env POSTGRES_PASSWORD
    file_env POSTGRES_USER

    drop-create-db

    restore-roles ${role_snapshot_id}
    restore-db ${db_snapshot_id}
    restore-directory '/var/backups/' ${file_snapshot_id}

    end-maintenance.sh
}

source /usr/local/lib/functions.sh
source /usr/local/lib/pg-functions.sh
source /usr/local/lib/file-functions.sh

main "$@"
