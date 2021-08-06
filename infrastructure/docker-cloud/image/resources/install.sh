#!/bin/bash

function main() {

    upgradeSystem
    apt-get -qqy install curl openjdk-11-jre-headless xmlstarlet > /dev/null
    
    adduser --system --disabled-login --home ${CLOUD_HOME} --uid 901 --group cloud 

    mkdir -p /app
    
    cd /tmp
    curl --location ${DOWNLOAD_URL}/atlassian-cloud-software-${CLOUD_RELEASE}.tar.gz \
        -o atlassian-cloud-software-${CLOUD_RELEASE}.tar.gz
    tar -xvf atlassian-cloud-software-${CLOUD_RELEASE}.tar.gz -C /tmp/
    mv /tmp/atlassian-cloud-software-${CLOUD_RELEASE}-standalone \
        /opt/atlassian-cloud-software-standalone
    chown -R cloud:cloud /opt/atlassian-cloud-software-standalone

    install -ocloud -gcloud -m744 /tmp/resources/entrypoint.sh /app/entrypoint.sh
    install -ocloud -gcloud -m744 /tmp/resources/setenv.sh /opt/atlassian-cloud-software-standalone/bin/setenv.sh
    install -ocloud -gcloud -m660 /tmp/resources/dbconfig.xml /app/dbconfig.xml
    install -ocloud -gcloud -m660 /tmp/resources/server.xml /opt/atlassian-cloud-software-standalone/conf/server.xml
    install -ocloud -gcloud -m660 /tmp/resources/logging.properties /opt/atlassian-cloud-software-standalone/conf/logging.properties

    cleanupDocker
}

source /tmp/resources/install_functions.sh
main