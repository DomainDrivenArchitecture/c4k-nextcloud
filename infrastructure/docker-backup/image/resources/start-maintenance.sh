#!/bin/bash

set -x

if [ ! -f "/var/backups/config/config.orig" ]; then

    rm -f /var/backups/config/config.orig
    cp /var/backups/config/config.php /var/backups/config/config.orig

    # put nextcloud in maintenance mode
    sed -i "s/);/  \'maintenance\' => true,\n);/g" /var/backups/config/config.php

    chown www-data:root /var/backups/config/config.php
    touch /var/backups/config/config.php

fi
