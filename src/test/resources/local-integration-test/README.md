# Requirements

* Restic (install with: `sudo apt install restic` )
* (optional) AWS-CLI


# Usage

`setup-local-s3.sh [BUCKET_NAME]`:
- [BUCKET_NAME] is optional, "mybucket" will be used if not specified
- sets up a k3s instance
- installs a localstack pod
- creates http and https routing to localstack via localhost
- saves the self-signed certificate as ca.crt
- uses the certificate to initialize a restic repo at `https://k3stesthost/BUCKET_NAME`

Note: In case of not being able to connect to "k3stesthost/health", you might need to ensure that the ingress' ip matches with the required host names: k3stesthost and cloudhost. With `sudo k3s kubectl get ingress` you can view the ingress' ip (e.g. 10.0.2.15), then add a line to file "/etc/hosts" e.g. `10.0.2.15	k3stesthost cloudhost`

`start-k3s.sh`:
- creates and starts a k3s instance

`k3s-uninstall.sh`:
- deletes everything k3s related

## Other useful commands
- `sudo k3s kubectl get pods`
- `curl k3stesthost/health`
  expected: `{"services": {"s3": "running"}, "features": {"persistence": "disabled", "initScripts": "initialized"}}` 

#### Requires AWS-CLI
- create bucket `aws --endpoint-url=http://k3stesthost s3 mb s3://mybucket`
- list buckets `aws --endpoint-url=http://k3stesthost s3 ls`
- upload something `aws --endpoint-url=http://k3stesthost s3 cp test.txt s3://mybucket`
- check files `aws --endpoint-url=http://k3stesthost s3 ls s3://mybucket`


## Deploy nextcloud

### Requirements

* leiningen (install with: `sudo apt install leiningen` )

### Deploy

* In the project's root execute: 
`lein uberjar`

* Change file "valid-config.edn" according to your settings (e.g. `:fqdn "cloudhost"` and `:restic-repository "s3://k3stesthost:mybucket"`).

* Create and deploy the k8s yaml:
`java -jar target/uberjar/c4k-nextcloud-standalone.jar valid-config.edn valid-auth.edn | sudo k3s kubectl apply -f -`

# TODO

* add possibility to use local certificate in dda-backup backup function
  * if ENV_VARIABLE set: use certificate
* get restic password from config