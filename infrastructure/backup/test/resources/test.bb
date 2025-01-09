#!/usr/bin/env bb
(require
 '[babashka.fs :as fs])
(-> "/usr/local/bin/config.clj" fs/file load-file)

(require '[babashka.tasks :as tasks]
         '[dda.backup.core :as bc]
         '[dda.backup.restic :as rc]
         '[dda.backup.postgresql :as pg]
         '[dda.backup.backup :as bak]
         '[dda.backup.restore :as rs]
         '[config :as cf])

(defn prepare!
  []
  (println (bc/env-or-file "RESTIC_PASSWORD_FILE"))
  (println (bc/env-or-file "ENV_PASSWORD"))
  (tasks/shell "mkdir" "-p" "/var/backups/")
  (spit "/var/backups/file" "I was here")
  (tasks/shell "mkdir" "-p" "/var/restore"))

(defn restic-repo-init!
  []
  (rc/init! (merge cf/file-config cf/dry-run))
  (rc/init! (merge cf/db-config cf/dry-run)))

(defn restic-backup!
  []
  (bak/backup-file! cf/file-config)
  (bak/backup-db! (merge cf/db-config cf/dry-run)))

(defn list-snapshots!
  []
  (rc/list-snapshots! cf/file-config)
  (rc/list-snapshots! (merge cf/db-config cf/dry-run)))


(defn restic-restore!
  []
  (rs/restore-file! cf/file-config)
  (pg/drop-create-db! (merge cf/db-config cf/dry-run))
  (rs/restore-db! (merge cf/db-config cf/dry-run)))

(prepare!)
(restic-repo-init!)
#(restic-backup!)
#(list-snapshots!)
#(restic-restore!)
