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
  (tasks/shell "mkdir" "-p" "/var/restic-repo/")
  (spit "/var/backups/file" "I was here"))

(defn restic-repo-init!
  []
  (rc/init! cf/file-config)
  (rc/init! (merge cf/db-config))
  (rc/init! (merge cf/db-role-config)))

(defn restic-backup!
  []
  (bak/backup-file! cf/file-config)
  (bak/backup-db-roles! (merge cf/db-role-config cf/dry-run))
  (bak/backup-db! (merge cf/db-config cf/dry-run)))

(defn list-snapshots!
  []
  (rc/list-snapshots! cf/file-config)
  (rc/list-snapshots! (merge cf/db-role-config cf/dry-run))
  (rc/list-snapshots! (merge cf/db-config cf/dry-run)))

(defn restic-restore!
  []
  (println "huhu")
  (rs/restore-file! (merge cf/file-restore-config  {:debug true}))
  (pg/drop-create-db! (merge cf/db-config cf/dry-run))
  (rs/restore-db-roles! (merge cf/db-role-config cf/dry-run))
  (rs/restore-db! (merge cf/db-config cf/dry-run)))

(prepare!)
(restic-repo-init!)
(restic-backup!)
(list-snapshots!)
(restic-restore!)
