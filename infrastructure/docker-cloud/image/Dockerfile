FROM ubuntu:20.04

ENV CLOUD_HOME="/var/cloud" \
    DOWNLOAD_URL="https://product-downloads.atlassian.com/software/nextcloud/downloads" \
    CLOUD_RELEASE="8.8.0"   
    # CLOUD_RELEASE="20.1.0"???
ADD resources /tmp/resources
RUN /tmp/resources/install.sh

USER 901:901
