#!/usr/bin/env bb
(require
 '[babashka.fs :as fs])
(-> "/usr/local/bin/config.clj" fs/file load-file)

(require
 '[dda.backup.core :as bc]
 '[dda.backup.restic :as rc]
 '[config :as cf])

(defn prepare!
  []
  (bc/create-aws-credentials! cf/aws-config))

(defn list-snapshots!
  []
  (rc/list-snapshots! cf/file-config)
  (rc/list-snapshots! cf/db-role-config)
  (rc/list-snapshots! cf/db-config))

(prepare!)
(list-snapshots!)
