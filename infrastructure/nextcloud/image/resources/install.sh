#!/bin/bash
set -exo pipefail

function main() {
    {
        upgradeSystem
        apt-get install -qqy ca-certificates curl gnupg postgresql-client
        mkdir /var/data
    } > /dev/null

    install -m 0700 /tmp/install-debug.sh /usr/local/bin/
    install -m 0544 /tmp/upload-max-limit.ini /usr/local/etc/php/conf.d/
    install -m 0544 /tmp/memory-limit.ini /usr/local/etc/php/conf.d/
    install -m 0755 /tmp/entrypoint.sh /
    
    cleanupDocker
}

source /tmp/install_functions.sh
main