#!/usr/bin/env bb
(require
 '[babashka.fs :as fs])
(-> "/usr/local/bin/config.clj" fs/file load-file)

(require
 '[dda.backup.core :as bc]
 '[dda.backup.restic :as rc]
 '[config :as cf])

(def file-pw-change-config (merge cf/file-config {:new-password-file (bc/env-or-file "RESTIC_NEW_PASSWORD_FILE")}))
(def db-pw-change-config (merge cf/db-config {:new-password-file (bc/env-or-file "RESTIC_NEW_PASSWORD_FILE")}))
(def db-role-pw-change-config (merge cf/db-role-config {:new-password-file (bc/env-or-file "RESTIC_NEW_PASSWORD_FILE")}))

(defn prepare!
  []
  (bc/create-aws-credentials! cf/aws-config))

(defn change-password!
  []
  (rc/change-password! file-pw-change-config)
  (rc/change-password! db-pw-change-config)
  (rc/change-password! db-role-pw-change-config))

(prepare!)
(change-password!)
