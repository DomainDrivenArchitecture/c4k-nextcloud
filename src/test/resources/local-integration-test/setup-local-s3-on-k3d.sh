#!/bin/bash

set -x

function main()
{
  date

  local bucket_name="${1:-mybucket}"; shift

  kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v1.5.4/cert-manager.yaml

  kubectl apply -f localstack.yaml

  date

  until kubectl apply -f certificate.yaml
  do
    echo "[INFO] Waiting for certificate ..."
    sleep 30
  done
  
  echo
  bash -c 'external_ip=""; while [ -z $external_ip ]; do echo "[INFO] Waiting for end point..."; external_ip=$(kubectl get ingress -o jsonpath="{$.items[*].status.loadBalancer.ingress[*].ip}"); [ -z "$external_ip" ] && sleep 10; done; echo "End point ready - $external_ip";'

  date

  echo
  export ENDPOINT=$(kubectl get ingress ingress-localstack -o=jsonpath="{.status.loadBalancer.ingress[0].ip}")
  sudo bash -c "echo \"$ENDPOINT k3stesthost cloudhost\" >> /etc/hosts" # Remove this, works for testing, but fills your /etc/hosts

  cd ../../../../    # c4k-nextcloud project root
  lein uberjar
  java -jar target/uberjar/c4k-nextcloud-standalone.jar config-local.edn auth-local.edn | kubectl apply -f -
  kubectl scale deployment backup-restore --replicas 1

  date
  echo
  until curl --fail --silent k3stesthost/health | grep -oe '"s3": "available"' -oe '"s3": "running"'
  do
    curl --fail k3stesthost/health
    echo "[INFO] Waiting for s3 running"
    sleep 10
  done
  echo

  POD=$(kubectl get pod -l app=backup-restore -o name)

  kubectl wait $POD --for=condition=Ready --timeout=240s

  kubectl exec -t $POD -- bash -c "echo \"$ENDPOINT k3stesthost cloudhost\" >> /etc/hosts"
  kubectl exec -t $POD -- /usr/local/bin/init.sh

  # debug
  kubectl exec -t $POD -- bash -c "sudo -u www-data php occ maintenance:mode --on"
  kubectl exec -t $POD -- bash -c "sudo -u www-data php occ maintenance:mode --off"
  kubectl exec -t $POD -- bash -c "la -la /var/"
  kubectl exec -t $POD -- bash -c "la -la /var/backups"
  kubectl exec -t $POD -- bash -c "la -la /var/backups/config"
  #debug end

  date
  echo ================= BACKUP =================
  kubectl exec -t $POD -- /usr/local/bin/backup.sh

  date
  sleep 10 # avoid race conditions

  echo ================= RESTORE =================

  kubectl exec -t $POD -- ls -l /var/backups/config
  kubectl exec -t $POD -- /usr/local/bin/restore.sh
}

main "$@"
