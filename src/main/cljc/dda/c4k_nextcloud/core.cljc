(ns dda.c4k-nextcloud.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-nextcloud.nextcloud :as nextcloud]
  [dda.c4k-nextcloud.backup :as backup]))

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::nextcloud/fqdn]
                     :opt-un [::nextcloud/issuer ::nextcloud/nextcloud-data-volume-path
                              ::postgres/postgres-data-volume-path ::restic-repository
                              ::nextcloud/storage-size]))

(def auth? (s/keys :req-un [::postgres/postgres-db-user ::postgres/postgres-db-password
                            ::aws-access-key-id ::aws-secret-access-key
                            ::restic-password]))

(defn k8s-objects [config]
  (into
   []
   (concat [(yaml/to-string (postgres/generate-config :postgres-size :8gb))
            (yaml/to-string (postgres/generate-secret config))]
           (when (contains? config :postgres-data-volume-path)
             [(yaml/to-string (postgres/generate-persistent-volume config))])
           [(yaml/to-string (postgres/generate-pvc))
            (yaml/to-string (postgres/generate-deployment))
            (yaml/to-string (postgres/generate-service))]
           (when (contains? config :nextcloud-data-volume-path)
             [(yaml/to-string (nextcloud/generate-persistent-volume config))])
           [(yaml/to-string (nextcloud/generate-pvc))
            (yaml/to-string (nextcloud/generate-deployment config))
            (yaml/to-string (nextcloud/generate-service))
            (yaml/to-string (nextcloud/generate-certificate config))
            (yaml/to-string (nextcloud/generate-ingress config))
            (yaml/to-string (nextcloud/generate-service))]
           (when (contains? config :restic-repository)
             [(yaml/to-string (backup/generate-config config))
              (yaml/to-string (backup/generate-secret config))
              (yaml/to-string (backup/generate-cron))]))))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n---\n"
     (k8s-objects resulting-config))))
