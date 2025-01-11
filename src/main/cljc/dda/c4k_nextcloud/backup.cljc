(ns dda.c4k-nextcloud.backup
 (:require
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.base64 :as b64]
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.predicate :as cp]
  #?(:cljs [dda.c4k-common.macros :refer-macros [inline-resources]])))

(s/def ::aws-access-key-id cp/bash-env-string?)
(s/def ::aws-secret-access-key cp/bash-env-string?)
(s/def ::restic-password cp/bash-env-string?)
(s/def ::restic-repository cp/bash-env-string?)

(s/def ::config (s/keys :req-un [::restic-repository]))

(s/def ::auth (s/keys :req-un [::restic-password ::aws-access-key-id ::aws-secret-access-key]))


#?(:cljs
   (defmethod yaml/load-resource :backup [resource-name]
     (get (inline-resources "backup") resource-name)))

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
  [my-conf ::config]
  (let [backup-restore-yaml (yaml/load-as-edn "backup/backup-restore-deployment.yaml")]
    (if (and (contains? my-conf :local-integration-test) (= true (:local-integration-test my-conf)))
      (cm/replace-named-value backup-restore-yaml "CERTIFICATE_FILE" "/var/run/secrets/localstack-secrets/ca.crt")
      backup-restore-yaml)))

(defn-spec generate-secret map?
  [my-auth ::auth]
  (let [{:keys [aws-access-key-id aws-secret-access-key restic-password]} my-auth]
    (->
     (yaml/load-as-edn "backup/secret.yaml")
     (cm/replace-key-value :aws-access-key-id (b64/encode aws-access-key-id))
     (cm/replace-key-value :aws-secret-access-key (b64/encode aws-secret-access-key))
     (cm/replace-key-value :restic-password (b64/encode restic-password)))))
