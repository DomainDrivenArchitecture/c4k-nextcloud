(ns dda.c4k-nextcloud.core
  (:require
   #?(:clj [orchestra.core :refer [defn-spec]]
      :cljs [orchestra.core :refer-macros [defn-spec]])
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.postgres :as postgres]
   [dda.c4k-nextcloud.nextcloud :as nextcloud]
   [dda.c4k-nextcloud.backup :as backup]
   [dda.c4k-common.monitoring :as mon]
   [dda.c4k-common.namespace :as ns]))

(def config-defaults {:namespace "nextcloud"
                      :issuer "staging"
                      :pvc-storage-class-name "hcloud-volumes-encrypted"
                      :pv-storage-size-gb 200})

(defn-spec config-objects cp/map-or-seq?
  [config nextcloud/config?]
  (let [resolved-config (merge config-defaults config)]
    (map yaml/to-string
         (filter
          #(not (nil? %))
          (cm/concat-vec
           (ns/generate resolved-config)
           (postgres/generate-config (merge resolved-config {:postgres-image "postgres:17"
                                                             :postgres-size :8gb
                                                             :db-name "cloud"
                                                             :pv-storage-size-gb 50}))
           [(nextcloud/generate-pvc resolved-config)
            (nextcloud/generate-deployment resolved-config)
            (nextcloud/generate-service)]
           (nextcloud/generate-ingress-and-cert resolved-config)
           (when (:contains? resolved-config :restic-repository)
             [(backup/generate-config resolved-config)
              (backup/generate-cron)
              (backup/generate-backup-restore-deployment resolved-config)])
           (when (:contains? resolved-config :mon-cfg)
             (mon/generate-config)))))))

(defn-spec auth-objects cp/map-or-seq?
  [config nextcloud/config?
   auth nextcloud/auth?]
  (let [resolved-config (merge config-defaults config)]
    (map yaml/to-string
         (filter
          #(not (nil? %))
          (cm/concat-vec
           (postgres/generate-auth (merge resolved-config {:postgres-size :8gb
                                                           :db-name "cloud"
                                                           :pv-storage-size-gb 50})
                                   auth)
           [(nextcloud/generate-secret auth)]
           (when (:contains? resolved-config :restic-repository)
             [(backup/generate-secret auth)])
           (when (:contains? resolved-config :mon-cfg)
             (mon/generate-auth (:mon-cfg resolved-config) (:mon-auth auth))))))))