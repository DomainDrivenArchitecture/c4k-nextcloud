function main()
{
  local bucket_name="${1:-mybucket}"; shift

  # ./start-k3d.sh

  # source kubectl.sh
  # kubectl config use-context k3d-nextcloud

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
  export ENDPOINT=$(kubectl get ingress ingress-localstack -o=jsonpath="{.status.loadBalancer.ingress[0].ip}")
  sudo bash -c "echo \"$ENDPOINT k3stesthost cloudhost\" >> /etc/hosts" # Remove this, works for testing, but fills your /etc/hosts

  cd ../../../../
  lein uberjar
  java -jar target/uberjar/c4k-nextcloud-standalone.jar config-local.edn auth-local.edn | kubectl apply -f -
  kubectl scale deployment backup-restore --replicas 1
  
  echo
  until curl --fail --silent k3stesthost/health | grep -oe '"s3": "available"' -oe '"s3": "running"'
  do
    curl --fail k3stesthost/health
    echo "[INFO] Waiting for s3 running"
    sleep 3
  done
  echo

  POD=$(kubectl get pod -l app=backup-restore -o name)
  kubectl wait $POD --for=condition=Ready --timeout=240s
  kubectl exec -t $POD -- bash -c "echo \"$ENDPOINT k3stesthost cloudhost\" >> /etc/hosts"
  kubectl exec -t $POD -- /usr/local/bin/init.sh
  kubectl exec -t $POD -- /usr/local/bin/backup.sh
  kubectl exec -t $POD -- /usr/local/bin/restore.sh
}

main $@
