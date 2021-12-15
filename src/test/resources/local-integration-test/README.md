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

## Run docker locally


```
docker pull docker:19.03.12-dind
docker run -d --privileged --name integration-test docker:19.03.12-dind
docker exec integration-test sh -c "apk add bash"

```

Set up docker container integration-test:

```
docker cp  ../../../../../c4k-nextcloud/ integration-test:/
docker exec -it integration-test sh
cd /c4k-nextcloud/src/test/resources/local-integration-test
./setup-docker.sh
```

## Deploy nextcloud 

### Requirements

* leiningen (install with: `sudo apt install leiningen` )
* In the project's root execute: `lein uberjar`
* Change file "valid-config.edn" according to your settings (e.g. `:fqdn "cloudhost"` and `:restic-repository "s3://k3stesthost:mybucket"`).

### Deploy to k3s

* Create and deploy the k8s yaml:
`java -jar target/uberjar/c4k-nextcloud-standalone.jar valid-config.edn valid-auth.edn | sudo k3s kubectl apply -f -`

Some of the steps may take some min to be effective, but eventually nextcloud should be available at: https://cloudhost

### Deploy to k3d

k3d is a k3s system which is running inside of a container. To install k3d run `curl -s https://raw.githubusercontent.com/rancher/k3d/main/install.sh | bash` or have a look at https://k3d.io/v5.0.3/ .

* Start a k3d cluster to deploy s3, nextcloud and test backup and restore on it: `./setup-local-s3-on-k3d.sh`

Some steps may take a couple of minutes to be effective, but eventually nextcloud should be available at: https://cloudhost

#### Remove k3d cluster

`k3d cluster delete nextcloud`

## Test in local gitlab runner

See https://stackoverflow.com/questions/32933174/use-gitlab-ci-to-run-tests-locally

This needs to be done in the project root

`docker run -d --name gitlab-runner --restart always -v $PWD:$PWD -v /var/run/docker.sock:/var/run/docker.sock gitlab/gitlab-runner:latest`

`docker exec -it -w $PWD gitlab-runner gitlab-runner exec docker nextcloud-integrationtest --docker-privileged --docker-volumes '/var/run/docker.sock:/var/run/docker.sock'`