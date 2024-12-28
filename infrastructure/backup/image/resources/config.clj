(ns config
  (:require
   [dda.backup.core :as bc]))

(def restic-repo {:password-file (bc/env-or-file "RESTIC_PASSWORD_FILE")
                  :restic-repository (bc/env-or-file "RESTIC_REPOSITORY")})

(def file-config (merge restic-repo {:backup-path "files"
                                     :execution-directory "/var/backups"
                                     :restore-target-directory "/var/backups/"
                                     :files ["."]}))

(def file-restore-config (merge restic-repo {:backup-path "files"
                                     :restore-target-directory "/var/backups/"}))

(def db-config (merge restic-repo {:backup-path "pg-database"
                                   :pg-host (bc/env-or-file "POSTGRES_SERVICE")
                                   :pg-port (bc/env-or-file "POSTGRES_PORT")
                                   :pg-db (bc/env-or-file "POSTGRES_DB")
                                   :pg-user (bc/env-or-file "POSTGRES_USER")
                                   :pg-password (bc/env-or-file "POSTGRES_PASSWORD")}))

(def db-role-config (merge restic-repo {:backup-path "pg-role"
                                        :pg-role-prefix "oc_"
                                        :pg-host (bc/env-or-file "POSTGRES_SERVICE")
                                        :pg-port (bc/env-or-file "POSTGRES_PORT")
                                        :pg-db (bc/env-or-file "POSTGRES_DB")
                                        :pg-user (bc/env-or-file "POSTGRES_USER")
                                        :pg-password (bc/env-or-file "POSTGRES_PASSWORD")}))

(def aws-config {:aws-access-key-id (bc/env-or-file "AWS_ACCESS_KEY_ID")
                 :aws-secret-access-key (bc/env-or-file "AWS_SECRET_ACCESS_KEY")})

(def dry-run {:dry-run true :debug true})
