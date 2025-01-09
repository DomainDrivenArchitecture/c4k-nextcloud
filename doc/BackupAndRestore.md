# Backup Architecture details

![](backup.svg)

* we use restic to produce small & encrypted backups
* backup is scheduled at `schedule: "10 23 * * *"`
* Cloud stores files on `/var/jira`, these files are backuped. If you create a jira xml backup located in /var/jira this file will also be backed up.
* postgres db is backed up as pgdump

## Manual backup

1. Scale Cloud deployment down:   
  `kubectl -n nextcloud scale deployment cloud-deployment --replicas=0`
1. Scale backup-restore deployment up:   
   `kubectl -n nextcloud scale deployment backup-restore --replicas=1`
1. exec into pod and execute restore pod   
   `kubectl -n nextcloud exec -it backup-restore -- backup.bb`
1. Scale backup-restore deployment down:   
  `kubectl -n nextcloud scale deployment backup-restore --replicas=0`
1. Scale Cloud deployment up:   
   `kubectl -n nextcloud scale deployment cloud-deployment --replicas=1`

## Manual restore

1. Scale Cloud deployment down:   
  `kubectl -n nextcloud scale deployment cloud-deployment --replicas=0`
2. Scale backup-restore deployment up:   
   `kubectl -n nextcloud scale deployment backup-restore --replicas=1`
3. exec into pod and execute restore pod   
   `kubectl -n nextcloud exec -it backup-restore -- restore.bb`
4. Scale backup-restore deployment down:   
  `kubectl -n nextcloud scale deployment backup-restore --replicas=0`
5. Scale Cloud deployment up:   
   `kubectl -n nextcloud scale deployment cloud-deployment --replicas=1`
