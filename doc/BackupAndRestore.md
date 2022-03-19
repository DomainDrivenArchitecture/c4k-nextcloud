# Backup Architecture details

![](backup.svg)

* we use restic to produce small & encrypted backups
* backup is scheduled at `schedule: "10 23 * * *"`
* Cloud stores files on `/var/jira`, these files are backuped. If you create a jira xml backup located in /var/jira this file will also be backed up.
* postgres db is backed up as pgdump

## Manual init the restic repository for the first time

1. apply backup-and-restore pod:   
   `kubectl apply -f src/main/resources/backup/backup-restore-deployment.yaml`
1. Scale backup-restore deployment up:   
   `kubectl scale deployment backup-restore --replicas=1`
1. exec into pod and execute restore pod   
   `kubectl exec -it backup-restore -- /usr/local/bin/init.sh`
1. Scale backup-restore deployment down:   
  `kubectl scale deployment backup-restore --replicas=0`



## Manual backup the restic repository for the first time

1. Scale Cloud deployment down:   
  `kubectl scale deployment cloud --replicas=0`
1. apply backup-and-restore pod:   
  `kubectl apply -f src/main/resources/backup/backup-restore-deployment.yaml`
1. exec into pod and execute restore pod   
   `kubectl exec -it backup-restore -- /usr/local/bin/backup.sh`
1. remove backup-and-restore pod:   
   `kubectl delete pod backup-restore`
1. Scale Cloud deployment up:   
   `kubectl scale deployment cloud --replicas=1`


## Manual restore

1. Scale Cloud deployment down:   
  `kubectl scale deployment cloud --replicas=0`
1. apply backup-and-restore pod:   
  `kubectl apply -f src/main/resources/backup/backup-restore-deployment.yaml`
1. exec into pod and execute restore pod   
   `kubectl exec -it backup-restore -- /usr/local/bin/restore.sh`
1. remove backup-and-restore pod:   
   `kubectl delete pod backup-restore`
1. Scale Cloud deployment up:   
   `kubectl scale deployment cloud --replicas=1`
1. Update index of Cloud:   
   Cloud > Settings > System > Advanced > Indexing
