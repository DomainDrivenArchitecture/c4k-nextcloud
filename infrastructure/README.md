# Build images

## Prerequisites

See also https://pypi.org/project/ddadevops/

```bash
# Ensure that yout python3 version is at least Python 3.7!

sudo apt install python3-pip
pip3 install pip --upgrade --user
pip3 install pybuilder ddadevops deprecation --user
export PATH=$PATH:~/.local/bin

#  terraform
pip3 install dda-python-terraform --user

# AwsMixin
pip3 install boto3 --user

# AwsMfaMixin
pip3 install boto3 mfa --user
```

In folder "docker-backup" resp. "docker-nextcloud":

```bash
# step test is optional
pyb image test publish
```