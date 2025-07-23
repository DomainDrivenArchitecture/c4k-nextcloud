(ns dda.c4k-nextcloud.core
  (:require
   [clojure.spec.alpha :as s]
   [orchestra.core :refer [defn-spec]]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.ingress :as ing]
   [dda.c4k-common.postgres :as postgres]
   [dda.c4k-nextcloud.nextcloud :as nextcloud]
   [dda.c4k-nextcloud.backup :as backup]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-common.namespace :as ns]))

(def config-defaults {:namespace "nextcloud"
                      :issuer "staging"
                      :pvc-storage-class-name "hcloud-volumes-encrypted"
                      :pv-storage-size-gb 200})

(s/def ::config (s/merge ::nextcloud/config
                      ::backup/config))

(s/def ::auth (s/merge ::nextcloud/auth
                    ::backup/auth))

(s/def ::config-select (s/* #{"auth" "deployment"}))


(defn-spec config-objects seq?
  [config-select ::config-select
   config ::config]
  (let [resolved-config (merge config-defaults config)
        {:keys [fqdn max-rate max-concurrent-requests namespace]} resolved-config
        config-parts (if (empty? config-select)
                       ["auth" "deployment"]
                       config-select)]
    (map yaml/to-string
         (if (some #(= "deployment" %) config-parts)
           (cm/concat-vec
            (ns/generate resolved-config)
            (postgres/config-objects (merge resolved-config
                                            {:postgres-image "postgres:16"
                                             :postgres-size :8gb
                                             :db-name "cloud"
                                             :pv-storage-size-gb 50}))
            [(nextcloud/generate-pvc resolved-config)
             (nextcloud/generate-deployment resolved-config)
             (nextcloud/generate-service)]
            (ing/config-objects (merge
                                 {:service-name "cloud-service"
                                  :service-port 80
                                  :fqdns [fqdn]
                                  :average-rate max-rate
                                  :burst-rate max-concurrent-requests
                                  :namespace namespace}
                                 resolved-config))
            (when (:contains? resolved-config :restic-repository)
              [(backup/generate-config resolved-config)
               (backup/generate-cron)
               (backup/generate-backup-restore-deployment resolved-config)])
            (when (:contains? resolved-config :mon-cfg)
              (mon/config-objects resolved-config)))
           []))))

(defn-spec auth-objects cp/map-or-seq?
  [config-select ::config-select
   config ::config
   auth ::auth]
  (let [resolved-config (merge config-defaults config)
        config-parts (if (empty? config-select)
                       ["auth" "deployment"]
                       config-select)]
    (map yaml/to-string
         (if (some #(= "auth" %) config-parts)
           (cm/concat-vec
            (postgres/auth-objects (merge resolved-config {:postgres-size :8gb
                                                           :db-name "cloud"
                                                           :pv-storage-size-gb 50})
                                   auth)
            [(nextcloud/generate-secret auth)]
            (when (:contains? resolved-config :restic-repository)
              [(backup/generate-secret auth)])
            (when (:contains? resolved-config :mon-cfg)
              (mon/auth-objects (:mon-cfg resolved-config) (:mon-auth auth)))) 
           []))))