#!/bin/bash
set -Eeo pipefail

apt update && apt -qqy install postgresql-client > /dev/null

mkdir /var/data

install -m 0700 /tmp/install-debug.sh /usr/local/bin/
install -m 0544 /tmp/upload-max-limit.ini /usr/local/etc/php/conf.d/
install -m 0544 /tmp/memory-limit.ini /usr/local/etc/php/conf.d/
install -m 0755 /tmp/entrypoint.sh /