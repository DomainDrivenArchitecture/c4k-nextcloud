FROM domaindrivenarchitecture/dda-backup:latest

# Prepare Entrypoint Script
ADD resources /tmp
RUN /tmp/install.bb
RUN init.bb
ADD resources2 /tmp
