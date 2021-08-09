#!/bin/bash

kubectl delete --ignore-not-found=true -f backup-secret.yml
kubectl delete --ignore-not-found=true -f backup-config.yml
kubectl delete --ignore-not-found=true -f backup-cron.yml

kubectl apply -f backup-secret.yml
kubectl apply -f backup-config.yml
kubectl apply -f backup-cron.yml
