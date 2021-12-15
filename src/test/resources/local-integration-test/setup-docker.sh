#!/bin/bash

set -x

docker volume create k3s-server

name='inttst'

[[ $(docker ps -f "name=$name" --format '{{.Names}}') == $name ]] || docker run --name $name -d --privileged --tmpfs /run  --tmpfs /var/run --restart always -e K3S_TOKEN=12345678901234 -e K3S_KUBECONFIG_OUTPUT=./kubeconfig.yaml -e  K3S_KUBECONFIG_MODE=666 -v k3s-server:/var/lib/rancher/k3s:z -v $(pwd):/output:z -p 6443:6443 -p 80:80 -p 443:443 rancher/k3s server --cluster-init --tls-san k3stesthost --tls-san cloudhost

docker ps

export timeout=30; while ! docker exec   $name    sh -c "test -f /var/lib/rancher/k3s/server/kubeconfig.yaml"; do if [ "$timeout" == 0 ]; then echo "ERROR: Timeout while waiting for file."; break; fi; sleep 1; ((timeout--)); done

mkdir -p $HOME/.kube/

docker cp $name:/var/lib/rancher/k3s/server/kubeconfig.yaml $HOME/.kube/config

if [ "$timeout" == 0 ]
then
  echo -------------------------------------------------------
  find / -name "kubeconfig.yaml"; 
  echo -------------------------------------------------------
  docker ps -a
  echo -------------------------------------------------------
  exit 1
fi

echo "127.0.0.1 kubernetes" >> /etc/hosts

apk add wget curl bash sudo openjdk8

wget -P /etc/apk/keys/ https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub
apk add --no-cache --repository=https://apkproxy.herokuapp.com/sgerrand/alpine-pkg-leiningen leiningen

curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.22.0/bin/linux/amd64/kubectl
chmod +x ./kubectl
mv ./kubectl /usr/local/bin/kubectl

sleep 20 #allow some time to startup k3s
docker ps -a

swapoff -a   # can this be removed ?

export KUBECONFIG=$HOME/.kube/config

pwd
cd ./c4k-nextcloud/src/test/resources/local-integration-test && ./setup-local-s3-on-k3d.sh