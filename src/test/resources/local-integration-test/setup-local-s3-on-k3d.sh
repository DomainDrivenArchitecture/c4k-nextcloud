function main()
{
  local bucket_name="${1:-mybucket}"; shift

  ./start-k3d.sh

  source kubectl.sh
  kubectl config use-context k3d-nextcloud

  kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v1.5.4/cert-manager.yaml

  kubectl apply -f localstack.yaml

  until kubectl apply -f certificate.yaml
  do
    echo "[INFO] Waiting for certificate ..."
    sleep 10
  done
  
  echo
  bash -c 'external_ip=""; while [ -z $external_ip ]; do echo "[INFO] Waiting for end point..."; external_ip=$(kubectl get ingress -o jsonpath="{$.items[*].status.loadBalancer.ingress[*].ip}"); [ -z "$external_ip" ] && sleep 10; done; echo "End point ready - $external_ip";'

  echo 
  export ENDPOINT=$(kubectl get ingress -o jsonpath="{$.items[*].status.loadBalancer.ingress[*].ip}")
  sudo bash -c "echo \"$ENDPOINT k3stesthost cloudhost\" >> /etc/hosts"

  echo
  until curl --silent --fail k3stesthost:80 k3stesthost/health | grep -o '"s3": "available"'
  do
    curl --fail k3stesthost:80 k3stesthost/health
    echo "[INFO] Waiting for s3 running"
    sleep 3
  done
  echo

  kubectl get secret localstack-secret -o jsonpath="{.data.ca\.crt}" | base64 --decode > ca.crt

  export RESTIC_PASSWORD="test-password"
  restic init --cacert ca.crt -r s3://k3stesthost/$bucket_name
  
}

main $@
