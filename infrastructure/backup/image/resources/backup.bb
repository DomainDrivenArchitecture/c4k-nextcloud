#!/usr/bin/env bb
(require
 '[babashka.tasks :as t]
 '[dda.backup.core :as bc]
 '[dda.backup.config :as cfg]
 '[dda.backup.restic :as rc]
 '[dda.backup.postgresql :as pg]
 '[dda.backup.backup :as bak])

(def config (cfg/read-config "/usr/local/bin/config.edn"))


(defn prepare!
  []
  (bc/create-aws-credentials! (:aws-config config))
  (pg/create-pg-pass! (:db-config config)))

(defn restic-repo-init!
  []
  (rc/init! (:file-config config))
  (rc/init! (:db-role-config config))
  (rc/init! (:db-config config)))

(defn restic-backup!
  []
  (bak/backup-file! (:file-config config))
  (bak/backup-db-roles! (:db-role-config config))
  (bak/backup-db! (:db-config config)))

(t/shell "start-maintenance.sh")
(prepare!)
(restic-repo-init!)
(restic-backup!)
(t/shell "end-maintenance.sh")