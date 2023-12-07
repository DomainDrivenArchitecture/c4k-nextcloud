FROM domaindrivenarchitecture/dda-backup:latest

# Prepare Entrypoint Script
ADD resources /tmp
RUN DEBIAN_FRONTEND=noninteractive DEBCONF_NOWARNINGS=yes /tmp/install.sh
