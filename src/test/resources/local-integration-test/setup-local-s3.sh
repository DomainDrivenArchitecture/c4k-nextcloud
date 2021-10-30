function main()
{
  local bucket_name="${1:-mybucket}"; shift

  ./start-k3s.sh

  sudo k3s kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v1.5.4/cert-manager.yaml

  sudo k3s kubectl apply -f localstack.yaml

  until sudo k3s kubectl apply -f certificate.yaml
  do
    echo "*** Waiting for certificate ... ***"
    sleep 10
  done
  echo

  echo
  echo "[INFO] Waiting for localstack health endpoint"
  until curl --connect-timeout 3 -s -f -o /dev/null "k3stesthost/health"
  do
    sleep 5
  done
  echo

  sudo k3s kubectl get secret localstack-secret -o jsonpath="{.data.ca\.crt}" | base64 --decode > ca.crt

  #aws --endpoint-url=http://localhost s3 mb s3://$bucket_name
  export RESTIC_PASSWORD="test-password"
  restic init --cacert ca.crt -r s3://k3stesthost/$bucket_name

}

main $@
