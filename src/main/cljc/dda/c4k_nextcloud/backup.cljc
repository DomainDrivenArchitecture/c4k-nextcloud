(ns dda.c4k-nextcloud.backup
  (:require
   [clojure.spec.alpha :as s]
   [orchestra.core :refer [defn-spec]]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.base64 :as b64]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]))

(s/def ::aws-access-key-id cp/bash-env-string?)
(s/def ::aws-secret-access-key cp/bash-env-string?)
(s/def ::restic-password cp/bash-env-string?)
(s/def ::restic-new-password cp/bash-env-string?)
(s/def ::restic-repository cp/bash-env-string?)

(s/def ::config (s/keys :req-un [::restic-repository]))

(s/def ::auth (s/keys :req-un [::restic-password ::aws-access-key-id ::aws-secret-access-key]
                      :opt-un [::restic-new-password]))

(defn-spec generate-config map?
  [my-conf ::config]
  (let [{:keys [restic-repository]} my-conf]
    (->
     (yaml/load-as-edn "backup/config.yaml")
     (cm/replace-key-value :restic-repository restic-repository))))

(defn-spec generate-cron map?
  []
  (yaml/from-string (yaml/load-resource "backup/cron.yaml")))

(defn-spec generate-backup-restore-deployment map?
  [conf ::config]
  (yaml/load-as-edn "backup/backup-restore-deployment.yaml"))

(defn-spec generate-secret map?
  [auth ::auth]
  (let [{:keys [aws-access-key-id aws-secret-access-key
                restic-password restic-new-password]} auth]
    (as-> (yaml/load-as-edn "backup/secret.yaml") res
      (cm/replace-key-value res :aws-access-key-id (b64/encode aws-access-key-id))
      (cm/replace-key-value res :aws-secret-access-key (b64/encode aws-secret-access-key))
      (cm/replace-key-value res :restic-password (b64/encode restic-password))
      (if (contains? auth :restic-new-password)
        (assoc-in res [:data :restic-new-password] (b64/encode restic-new-password))
        res))))
