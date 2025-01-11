FROM domaindrivenarchitecture/dda-backup:5.2.1

# Prepare Entrypoint Script
ADD resources /tmp
RUN /tmp/install.bb
RUN init.bb
ADD resources2 /tmp
