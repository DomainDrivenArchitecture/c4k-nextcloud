FROM domaindrivenarchitecture/dda-backup:5.1.0

# Prepare Entrypoint Script
ADD resources /tmp
RUN /tmp/install.bb
RUN init.bb
ADD resources2 /tmp
