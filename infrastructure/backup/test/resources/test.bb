#!/usr/bin/env bb
(require '[babashka.tasks :as tasks]
         '[dda.backup.core :as bc]
         '[dda.backup.config :as cfg]
         '[dda.backup.restic :as rc]
         '[dda.backup.postgresql :as pg]
         '[dda.backup.backup :as bak]
         '[dda.backup.restore :as rs])

(def config (cfg/read-config "/usr/local/bin/config.edn"))

(def file-pw-change-config (merge (:file-config config)
                                  {:new-password-file (bc/env-or-file "RESTIC_NEW_PASSWORD_FILE")}))

(defn prepare!
  []
  (tasks/shell "mkdir" "-p" "/var/backups/")
  (spit "/var/backups/file" "I was here")
  (tasks/shell "mkdir" "-p" "/var/restore"))

(defn restic-repo-init!
  []
  (rc/init! (:file-config config))
  (rc/init! (merge (:db-role-config config)
                   (:dry-run config)))
  (rc/init! (merge (:db-config config)
                   (:dry-run config))))

(defn restic-backup!
  []
  (bak/backup-file! (:file-config config))
  (bak/backup-db-roles! (merge (:db-role-config config)
                               (:dry-run config)))
  (bak/backup-db! (merge (:db-config config)
                         (:dry-run config))))

(defn list-snapshots!
  []
  (rc/list-snapshots! (:file-config config))
  (rc/list-snapshots! (merge (:db-role-config config)
                             (:dry-run config)))
  (rc/list-snapshots! (merge (:db-config config)
                             (:dry-run config))))


(defn restic-restore!
  []
  (pg/drop-create-db! (merge (:db-config config)
                             (:dry-run config)))
  (rs/restore-db-roles! (merge (:db-role-config config)
                               (:dry-run config)))
  (rs/restore-db! (merge (:db-config config)
                         (:dry-run config)))
  (rs/restore-file! (merge (:file-restore-config config)
                           (:dry-run config))))

(defn change-password!
  []
  (println "change-password!")
  (rc/change-password! file-pw-change-config))

(prepare!)
(restic-repo-init!)
(restic-backup!)
(list-snapshots!)
(restic-restore!)
(change-password!)
