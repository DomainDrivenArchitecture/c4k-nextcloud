#!/bin/bash

set -ex

docker run -d --privileged --tmpfs /run  --tmpfs /var/run --restart always -e K3S_TOKEN=12345678901234 -e K3S_KUBECONFIG_OUTPUT=./kubeconfig.yaml -e  K3S_KUBECONFIG_MODE=666 -v k3s-server:/var/lib/rancher/k3s:z -v $(pwd):/output:z -p 6443:6443 -p 80:80 -p 443:443 rancher/k3s server --cluster-init

export timeout=60; while [ ! -f /var/lib/docker/volumes/k3s-server/_data/server/kubeconfig.yaml ]; do if [ "$timeout" == 0 ]; then echo "ERROR: Timeout while waiting for file."; break; fi; sleep 1; ((timeout--)); done

if [ "$timeout" == 0 ] 
then
  echo -------------------------------------------------------
  find . -name "kubeconfig.yaml"; 
  echo -------------------------------------------------------
  docker ps -a
  echo -------------------------------------------------------
  exit -1
fi

mkdir -p $HOME/.kube/
cp /var/lib/docker/volumes/k3s-server/_data/server/kubeconfig.yaml $HOME/.kube/config

apk add git curl bash sudo openjdk8 wget
wget -P /etc/apk/keys/ https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub
apk add --no-cache --repository=https://apkproxy.herokuapp.com/sgerrand/alpine-pkg-leiningen leiningen

curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.22.0/bin/linux/amd64/kubectl
chmod +x ./kubectl
mv ./kubectl /usr/local/bin/kubectl

git clone https://gitlab.com/domaindrivenarchitecture/c4k-nextcloud.git
cd /c4k-nextcloud && git checkout local-integration-test
cd /c4k-nextcloud/src/test/resources/local-integration-test && ./setup-local-s3-on-k3d.sh