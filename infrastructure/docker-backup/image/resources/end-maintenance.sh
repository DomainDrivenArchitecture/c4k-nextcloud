#!/bin/bash

if test -f "/var/backups/config/config.orig"; then

    rm /var/backups/config/config.php
    mv /var/backups/config/config.orig /var/backups/config/config.php
    chown www-data:root /var/backups/config/config.php
    touch /var/backups/config/config.php

fi