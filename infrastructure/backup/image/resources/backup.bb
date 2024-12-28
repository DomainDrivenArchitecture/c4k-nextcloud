#!/usr/bin/env bb
(require
 '[babashka.fs :as fs])
(-> "/usr/local/bin/config.clj" fs/file load-file)

(require
 '[babashka.tasks :as t]
 '[dda.backup.core :as bc]
 '[dda.backup.restic :as rc]
 '[dda.backup.postgresql :as pg]
 '[dda.backup.backup :as bak]
 '[config :as cf])

(defn prepare!
  []
  (bc/create-aws-credentials! cf/aws-config)
  (pg/create-pg-pass! cf/db-config))

(defn restic-repo-init!
  []
  (rc/init! cf/file-config)
  (rc/init! cf/db-role-config)
  (rc/init! cf/db-config))

(defn restic-backup!
  []
  (bak/backup-file! cf/file-config)
  (bak/backup-db-roles! cf/db-role-config)
  (bak/backup-db! cf/db-config))

(t/shell "start-maintenance.sh")
(prepare!)
(restic-repo-init!)
(restic-backup!)
(t/shell "end-maintenance.sh")