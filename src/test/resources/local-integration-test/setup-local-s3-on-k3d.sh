#!/bin/bash

set -x

function main()
{
  # enable tls for k3s with cert-manager
  kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v1.5.4/cert-manager.yaml

  kubectl apply -f localstack.yaml

  until kubectl apply -f certificate.yaml
  do
    echo "[INFO] Waiting for certificate ..."
    sleep 30
  done

  # wait for ingress to be ready
  bash -c 'external_ip=""; while [ -z $external_ip ]; do echo "[INFO] Waiting for end point..."; external_ip=$(kubectl get ingress -o jsonpath="{$.items[*].status.loadBalancer.ingress[*].ip}"); [ -z "$external_ip" ] && sleep 10; done; echo "End point ready - $external_ip";'

  export INGRESS_IP=$(kubectl get ingress ingress-localstack -o=jsonpath="{.status.loadBalancer.ingress[0].ip}")

  cd ../../../../    # c4k-nextcloud project root
  lein uberjar
  java -jar target/uberjar/c4k-nextcloud-standalone.jar config-local.edn auth-local.edn | kubectl apply -f -

  CLOUD_POD=$(kubectl get pod -l app=cloud-app -o name)
  kubectl wait $CLOUD_POD --for=condition=Ready --timeout=240s

  # wait for nextcloud config file available
  timeout 180 bash -c "kubectl exec -t $POD -- bash -c \"until [ -f /var/www/html/config/config.php ]; do sleep 10; done\""

  # ensure an instance of pod backup-restore
  kubectl scale deployment backup-restore --replicas 1

  # wait for localstack health endpoint
  echo "$INGRESS_IP k3stesthost cloudhost" >> /etc/hosts
  until curl --fail --silent k3stesthost/health | grep -oe '"s3": "available"' -oe '"s3": "running"'
  do
    curl --fail k3stesthost/health
    echo "[INFO] Waiting for s3 running"
    sleep 10
  done

  BACKUP_POD=$(kubectl get pod -l app=backup-restore -o name)
  kubectl wait $BACKUP_POD --for=condition=Ready --timeout=240s

  kubectl exec -t $BACKUP_POD -- bash -c "echo \"$INGRESS_IP k3stesthost cloudhost\" >> /etc/hosts"
  kubectl exec -t $BACKUP_POD -- /usr/local/bin/init.sh

  echo ================= BACKUP =================
  kubectl exec -t $BACKUP_POD -- /usr/local/bin/backup.sh

  sleep 10 # avoid race conditions

  echo ================= RESTORE =================
  kubectl exec -t $BACKUP_POD -- /usr/local/bin/restore.sh
}

main "$@"
