# convention 4 kubernetes: c4k-nextcloud

[![Clojars Project](https://img.shields.io/clojars/v/org.domaindrivenarchitecture/c4k-nextcloud.svg)](https://clojars.org/org.domaindrivenarchitecture/c4k-nextcloud) [![pipeline status](https://gitlab.com/domaindrivenarchitecture/c4k-nextcloud/badges/master/pipeline.svg)](https://gitlab.com/domaindrivenarchitecture/c4k-nextcloud/-/commits/master) 

[<img src="https://domaindrivenarchitecture.org/img/delta-chat.svg" width=20 alt="DeltaChat"> chat over e-mail](mailto:buero@meissa-gmbh.de?subject=community-chat) | [<img src="https://meissa-gmbh.de/img/community/Mastodon_Logotype.svg" width=20 alt="team@social.meissa-gmbh.de"> team@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@team) | [Website & Blog](https://domaindrivenarchitecture.org)

## Purpose

c4k-nextcloud provides a k8s deployment for nextcloud containing:
* adjusted nextcloud docker image
* nextcloud
??? * ingress having a letsencrypt managed certificate
??? * postgres database

The package aims to a low load sceanrio.

## Status

This is under development.

## Manual restore

1) Scale Nextcloud deployment down:
kubectl scale deployment nextcloud --replicas=0

2)apply backup and restore pod:
kubectl apply -f src/main/resources/backup/backup-restore.yaml

3) exec into pod and execute restore pod
kubectl exec -it backup-restore -- /usr/local/bin/restore.sh

4) Scale Nextcloud deployment up:
kubectl scale deployment nextcloud --replicas=1

5) Update index of Nextcloud:
Nextcloud > Settings > System > Advanced > Indexing
## License

Copyright © 2021 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)