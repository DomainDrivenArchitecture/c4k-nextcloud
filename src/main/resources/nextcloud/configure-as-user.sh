#!/bin/bash

kubectl delete --ignore-not-found=true -f cloud-ingress.yml
kubectl delete --ignore-not-found=true -f cloud-pod.yml
kubectl delete --ignore-not-found=true -f cloud-pvc.yml
kubectl delete --ignore-not-found=true -f cloud-service.yml
kubectl delete --ignore-not-found=true -f cloud-secret.yml
kubectl delete --ignore-not-found=true -f cloud-persistent-volume.yml

#Wait for postgres to be running
while [$POSTGRES = ""]
do
    POSTGRES=$(kubectl get pods --selector=app=postgresql -o jsonpath='{.items[*].metadata.name}')
done
kubectl wait --for=condition=ready pod/$POSTGRES

kubectl apply -f cloud-persistent-volume.yml
kubectl apply -f cloud-secret.yml
kubectl apply -f cloud-service.yml
kubectl apply -f cloud-pvc.yml
kubectl apply -f cloud-pod.yml
kubectl apply -f cloud-ingress.yml
