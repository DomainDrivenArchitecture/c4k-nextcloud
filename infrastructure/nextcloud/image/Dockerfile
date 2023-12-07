FROM nextcloud:27

# Prepare Entrypoint Script
ADD resources /tmp
RUN DEBIAN_FRONTEND=noninteractive DEBCONF_NOWARNINGS=yes /tmp/install.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["apache2-foreground"]
