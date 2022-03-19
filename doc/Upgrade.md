# Upgrade with a new nextcloud version

1. Pull a new image
1. Trigger deployment

```
k3s crictl pull domaindrivenarchitecture/c4k-cloud
k3s crictl images
kubectl set env deployment cloud-deployment DEPLOY_DATE="$(date)"
```