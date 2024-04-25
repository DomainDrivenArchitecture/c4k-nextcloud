# Rename Database

## Start

1. Scale down cloud deployment
`k -n nextcloud scale deployment cloud-deployment --replicas 0`

## Change db-name in postgres

1. Connect to postgres-pod
`k -n nextcloud exec -it postgresql-... -- bash`
2. Connect to a database
`PGPASSWORD=$POSTGRES_PASSWORD psql -h postgresql-service -U $POSTGRES_USER postgres`
3. List available databases
`\l`
4. Rename database
`ALTER DATABASE cloud RENAME TO nextcloud;`
5. Verify
`\l`
6. Quit
`\q`

## Update postgres-config

1. Edit configmap
`k -n nextcloud edit configmap postgres-config`
2. Update postgres-db value
3. Save

## Update nextcloud db-name

1. Scale up nextcloud
`k -n nextcloud scale deployment cloud-deployment --replicas 1`
2. Connect
`k -n nextcloud exec -it cloud-deployment-... -- bash`
3. Update db value in config.php
`apt update`
`apt install vim`
`vim config/config.php`
4. Update dbname field
5. Verify server+website is working
