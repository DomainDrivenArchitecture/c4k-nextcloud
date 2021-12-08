#!/bin/bash

set -x

docker volume create k3s-server

name='inttst'

#[[ $(docker ps -f "name=$name" --format '{{.Names}}') == $name ]] ||
docker run --name $name -d --privileged --tmpfs /run  --tmpfs /var/run --restart always -e K3S_TOKEN=12345678901234 -e K3S_KUBECONFIG_OUTPUT=./kubeconfig.yaml -e  K3S_KUBECONFIG_MODE=666 -v k3s-server:/var/lib/rancher/k3s:z -v $(pwd):/output:z -p 6443:6443 -p 80:80 -p 443:443 rancher/k3s server --cluster-init --tls-san k3stesthost --tls-san cloudhost
docker ps

#export timeout=30; while [ ! -f /var/lib/docker/volumes/k3s-server/_data/server/kubeconfig.yaml ]; do if [ "$timeout" == 0 ]; then echo "ERROR: Timeout while waiting for file."; docker ps -a; ls /var/lib/docker/volumes/k3s-server/_data/; break; fi; sleep 1; ((timeout--)); done
export timeout=30; while ! docker exec   $name    sh -c "test -f /var/lib/rancher/k3s/server/kubeconfig.yaml"; do if [ "$timeout" == 0 ]; then echo "ERROR: Timeout while waiting for file."; docker ps -a; ls /var/lib/docker/volumes/k3s-server/_data/; break; fi; sleep 1; ((timeout--)); done



#sleep 60

mkdir -p $HOME/.kube/

docker cp $name:/var/lib/rancher/k3s/server/kubeconfig.yaml $HOME/.kube/config

#docker cp $name:/var/lib/rancher/k3s/server/kubeconfig.yaml /var/lib/docker/volumes/k3s-server/_data/server/
#ls /var/lib/docker/volumes/k3s-server/_data/server/

if [ "$timeout" == 0 ] 
then
  echo -------------------------------------------------------
  find / -name "kubeconfig.yaml"; 
  echo -------------------------------------------------------
  docker ps -a
  echo -------------------------------------------------------
  exit -1
fi


#cp /var/lib/docker/volumes/k3s-server/_data/server/kubeconfig.yaml $HOME/.kube/config

apk add wget curl bash sudo openjdk8
wget -P /etc/apk/keys/ https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub
apk add --no-cache --repository=https://apkproxy.herokuapp.com/sgerrand/alpine-pkg-leiningen leiningen

curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.22.0/bin/linux/amd64/kubectl
chmod +x ./kubectl
mv ./kubectl /usr/local/bin/kubectl

echo "127.0.0.1  kubernetes" >> /etc/hosts

#cd /c4k-nextcloud/src/test/resources/local-integration-test && ./setup-local-s3-on-k3d.sh
cd /builds/domaindrivenarchitecture/c4k-nextcloud/src/test/resources/local-integration-test && ./setup-local-s3-on-k3d.sh