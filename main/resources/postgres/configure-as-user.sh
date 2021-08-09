#!/bin/bash

kubectl delete --ignore-not-found=true -f postgres-deployment.yml
kubectl delete --ignore-not-found=true -f postgres-pvc.yml
kubectl delete --ignore-not-found=true -f postgres-service.yml
kubectl delete --ignore-not-found=true -f postgres-config.yml
kubectl delete --ignore-not-found=true -f postgres-secret.yml
kubectl delete --ignore-not-found=true -f postgres-persistent-volume.yml

kubectl apply -f postgres-persistent-volume.yml
kubectl apply -f postgres-secret.yml
kubectl apply -f postgres-config.yml
kubectl apply -f postgres-service.yml
kubectl apply -f postgres-pvc.yml
kubectl apply -f postgres-deployment.yml
