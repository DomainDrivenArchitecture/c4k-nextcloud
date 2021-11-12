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
    echo "*** Waiting for certificate ... ***"
    sleep 10
  done
  echo

  echo
  echo "[INFO] Waiting for localstack health endpoint"
  until curl --connect-timeout 3 -s -f -o /dev/null "k3stesthost/health"
  do
    sleep 1
  done
  echo

  kubectl get secret localstack-secret -o jsonpath="{.data.ca\.crt}" | base64 --decode > ca.crt

  export RESTIC_PASSWORD="test-password"
  restic init --cacert ca.crt -r s3://k3stesthost/$bucket_name

}

main $@
