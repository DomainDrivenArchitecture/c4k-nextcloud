# Backup Architecture details

![](backup.svg)

* we use restic to produce small & encrypted backups
* backup is scheduled at `schedule: "10 23 * * *"`
* Cloud stores files on `/var/cloud`, these files are backuped. If you create a jira xml backup located in /var/jira this file will also be backed up.
* postgres db is backed up as pgdump

## Manual backup

1. Scale backup-restore deployment up:   
   `kubectl -n nextcloud scale deployment backup-restore --replicas=1`
2. exec into pod and execute restore pod   
   `kubectl -n nextcloud exec -it backup-restore -- backup.bb`
3. Scale backup-restore deployment down:   
  `kubectl -n nextcloud scale deployment backup-restore --replicas=0`

## Manual restore

1. Scale backup-restore deployment up:   
   `kubectl -n nextcloud scale deployment backup-restore --replicas=1`
3. exec into pod and execute restore pod   
   `kubectl -n nextcloud exec -it backup-restore -- restore.bb`
4. Scale backup-restore deployment down:   
  `kubectl -n nextcloud scale deployment backup-restore --replicas=0`

## Change Password

1. Check restic-new-password env is set in backup deployment   
   ```
   kind: Deployment
   metadata:
     name: backup-restore
   spec:
       spec:
         containers:
         - name: backup-app
           env:
           - name: RESTIC_NEW_PASSWORD_FILE
             value: /var/run/secrets/backup-secrets/restic-new-password
   ```
2. Add restic-new-password to secret   
   ```
   kind: Secret
   metadata:
     name: backup-secret
   data:
     restic-password: old
     restic-new-password: new
   ```
3. Scale backup-restore deployment up:   
   `kubectl -n nextcloud scale deployment backup-restore --replicas=1`
4. exec into pod and execute restore pod   
   `kubectl -n nextcloud exec -it backup-restore -- change-password.bb`
5. Scale backup-restore deployment down:   
  `kubectl -n nextcloud scale deployment backup-restore --replicas=0`
6. Replace restic-password with restic-new-password in secret   
   ```
   kind: Secret
   metadata:
     name: backup-secret
   data:
     restic-password: new
   ```
