FROM nextcloud:29

# REQUIRES docker >= 2.10.10
# https://docs.docker.com/engine/release-notes/20.10/#201010

# Prepare Entrypoint Script
ADD resources /tmp
RUN /tmp/install.sh

ENTRYPOINT ["/entrypoint.sh"]
CMD ["apache2-foreground"]
