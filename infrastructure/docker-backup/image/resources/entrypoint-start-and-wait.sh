#!/bin/bash

function main() {
    file_env POSTGRES_DB
    file_env POSTGRES_PASSWORD
    file_env POSTGRES_USER

    create-pg-pass

    while true; do
        sleep 1m
    done
}

source /usr/local/lib/functions.sh
source /usr/local/lib/pg-functions.sh
main