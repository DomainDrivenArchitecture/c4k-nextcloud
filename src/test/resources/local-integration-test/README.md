# Requirements

* Restic
* (optional) AWS-CLI


# Usage

`setup-local-s3.sh [BUCKET_NAME]`:
- sets up a k3s instance
- installs a localstack pod
- creates http and https routing to localstack via localhost
- saves the self-signed certificate as ca.crt
- uses the certificate to initialize a restic repo at `https://localhost/BUCKET_NAME`

Note: In case of not being able to connect to "localhost/health", you might need to ensure that the ip of localhost matches with the ingress' ip. (See file /etc/hosts). With `sudo k3s kubectl get ingress` you can show the ingress' ip.

`start-k3s.sh`:
- creates and starts a k3s instance

`k3s-uninstall.sh`:
- deletes everything k3s related

## Other useful commands
- `sudo k3s kubectl get pods`
- `curl localhost/health`
  expected: `{"services": {"s3": "running"}, "features": {"persistence": "disabled", "initScripts": "initialized"}}` 

#### Requires AWS-CLI
- create bucket `aws --endpoint-url=http://localhost s3 mb s3://mybucket`
- list buckets `aws --endpoint-url=http://localhost s3 ls`
- upload something `aws --endpoint-url=http://localhost s3 cp test.txt s3://mybucket`
- check files `aws --endpoint-url=http://localhost s3 ls s3://mybucket`


# TODO

* add possibility to use local certificate in dda-backup backup function
  * if ENV_VARIABLE set: use certificate
* get restic password from config