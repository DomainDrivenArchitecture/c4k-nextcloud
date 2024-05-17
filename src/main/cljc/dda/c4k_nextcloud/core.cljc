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

(def default-storage-class "local-path")

(def config-defaults {:issuer "staging"})

(defn-spec k8s-objects cp/map-or-seq?
  [config nextcloud/config?
   auth nextcloud/auth?]
  (let [nextcloud-default-storage-config {:pvc-storage-class-name default-storage-class
                                          :pv-storage-size-gb 200}]
    (map yaml/to-string
         (filter
          #(not (nil? %))
          (cm/concat-vec
           (ns/generate (merge {:namespace "nextcloud"} config))
           (postgres/generate {:postgres-size :8gb
                               :db-name "cloud"
                               :pv-storage-size-gb 50
                               :pvc-storage-class-name default-storage-class
                               :namespace "nextcloud"}
                              auth)
           [(nextcloud/generate-secret auth)
            (nextcloud/generate-pvc (merge nextcloud-default-storage-config config))
            (nextcloud/generate-deployment config)
            (nextcloud/generate-service)]
           (nextcloud/generate-ingress-and-cert (merge {:namespace "nextcloud"} config))
           (when (:contains? config :restic-repository)
             [(backup/generate-config config)
              (backup/generate-secret auth)
              (backup/generate-cron)
              (backup/generate-backup-restore-deployment config)])
           (when (:contains? config :mon-cfg)
             (mon/generate (:mon-cfg config) (:mon-auth auth))))))))
