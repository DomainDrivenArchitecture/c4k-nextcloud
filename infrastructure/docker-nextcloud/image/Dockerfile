FROM nextcloud:21

# Prepare Entrypoint Script
ADD resources /tmp
RUN /tmp/install.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["apache2-foreground"]
