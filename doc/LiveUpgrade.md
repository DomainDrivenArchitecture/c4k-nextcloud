# Upgrade major or minor versions of nextcloud

## Nextcloud versions of c4k-nextcloud docker images

- 4.0.3: nextcloud 22
- 5.0.0: nextcloud 23
- 6.0.0: nextcloud 24
- 7.0.0: nextcloud 25
- 7.1.0: nextcloud 26 (manual publish)
- 7.2.0: nextcloud 27 (manual publish)
- 8.0.6: nextcloud 28

## Uprgrading process

1. Change the version of the docker image in the deployment to the next major version
    - `kubectl edit deploy cloud-deployment`
    - change `image: domaindrivenarchitecture/c4k-cloud:4.0.3`
2. Wait for the pod to finish restarting
3. Verify the website is working and https://URL/settings/admin/overview shows the correct version
4. Repeat until desired version is reached
