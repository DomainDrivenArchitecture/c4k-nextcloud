#!/usr/bin/env bb
(require
 '[babashka.fs :as fs])
(-> "/usr/local/bin/config.clj" fs/file load-file)

(require
 '[dda.backup.core :as bc]
 '[dda.backup.postgresql :as pg]
 '[config :as cf])

(defn prepare!
  []
  (bc/create-aws-credentials! cf/aws-config)
  (pg/create-pg-pass! cf/db-config))

(defn wait! []
  (while true
    (Thread/sleep 1000)))

(prepare!)
(wait!)