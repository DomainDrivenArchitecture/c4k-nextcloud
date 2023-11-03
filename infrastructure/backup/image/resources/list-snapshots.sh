#!/bin/bash

set -eux pipefail

function list-snapshot-files() {
  if [ -z ${CERTIFICATE_FILE} ];
  then
    restic -r ${RESTIC_REPOSITORY}/${backup_file_path} snapshots
  else
    restic -r ${RESTIC_REPOSITORY}/${backup_file_path} snapshots --cacert ${CERTIFICATE_FILE}
  fi
}

function main() {
    file_env AWS_ACCESS_KEY_ID
    file_env AWS_SECRET_ACCESS_KEY

    file_env POSTGRES_DB
    file_env POSTGRES_PASSWORD
    file_env POSTGRES_USER

    list-snapshot-roles
    list-snapshot-db
    list-snapshot-files
}

source /usr/local/lib/functions.sh
source /usr/local/lib/file-functions.sh
source /usr/local/lib/pg-functions.sh

main
