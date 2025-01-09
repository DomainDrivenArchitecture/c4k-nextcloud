# Upgrade major or minor versions of nextcloud

## Nextcloud versions of c4k-nextcloud docker images

- 4.0.3: nextcloud 22
- 5.0.0: nextcloud 23
- 6.0.0: nextcloud 24
- 7.0.7: nextcloud 25.0.13
- 7.1.1: nextcloud 26.0.0 (manual publish) => attention - only upgrade to 26.0.0 is working
- 7.1.0: nextcloud 26.0.13 (manual publish)
- 7.2.0: nextcloud 27 (manual publish)
- 10.0.0: nextcloud 28.0.5
- 10.1.0: nextcloud 29.0.0
- 10.4.0: nextcloud 30

## Uprgrading process

1. Change the version of the docker image in the deployment to the next major version
    - `kubectl -n=nextcloud edit deploy cloud-deployment`
    - change `image: domaindrivenarchitecture/c4k-cloud:4.0.3`
2. Wait for the pod to finish restarting
3. Verify the website is working and https://URL/settings/admin/overview shows the correct version
4. Repeat until desired version is reached
