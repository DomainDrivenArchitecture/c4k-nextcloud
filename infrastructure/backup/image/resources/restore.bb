#!/usr/bin/env bb
(require
 '[babashka.fs :as fs])
(-> "/usr/local/bin/config.clj" fs/file load-file)

(require
 '[babashka.tasks :as t]
 '[dda.backup.core :as bc]
 '[dda.backup.postgresql :as pg]
 '[dda.backup.restore :as rs]
 '[config :as cf])

(defn prepare!
  []
  (bc/create-aws-credentials! cf/aws-config)
  (pg/create-pg-pass! cf/db-config))

(defn restic-restore!
  []
  (pg/drop-create-db! cf/db-config)
  (rs/restore-db-roles! cf/db-role-config)
  (rs/restore-db! cf/db-config)
  (rs/restore-file! cf/file-config)
  )

(t/shell "start-maintenance.sh")
(prepare!)
(restic-restore!)
(t/shell "end-maintenance.sh")