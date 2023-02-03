(ns dda.c4k-nextcloud.core
 (:require
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.predicate :as cp]
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-nextcloud.nextcloud :as nextcloud]
  [dda.c4k-nextcloud.backup :as backup]))

(def default-storage-class :local-path)

(def config-defaults {:issuer "staging"})

(defn-spec k8s-objects cp/map-or-seq?
  [config nextcloud/config?
   auth nextcloud/auth?]
  (let [nextcloud-default-storage-config {:pvc-storage-class-name default-storage-class
                                          :pv-storage-size-gb 200}]
    (map yaml/to-string
         [(postgres/generate-config {:postgres-size :8gb :db-name "nextcloud"})
          (postgres/generate-secret auth)
          (postgres/generate-pvc {:pv-storage-size-gb 50
                                  :pvc-storage-class-name default-storage-class})
          (postgres/generate-deployment)
          (postgres/generate-service)
          (nextcloud/generate-secret auth)
          (nextcloud/generate-pvc (merge nextcloud-default-storage-config config))
          (nextcloud/generate-deployment config)
          (nextcloud/generate-service)]
         (nextcloud/generate-ingress-and-cert config)
         (when (:contains? config :restic-repository)
           [(backup/generate-config config)
            (backup/generate-secret auth)
            (backup/generate-cron)
            (backup/generate-backup-restore-deployment config)]))))
