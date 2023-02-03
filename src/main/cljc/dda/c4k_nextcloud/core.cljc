(ns dda.c4k-nextcloud.core
 (:require
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-nextcloud.nextcloud :as nextcloud]
  [dda.c4k-nextcloud.backup :as backup]))

(def default-storage-class :local-path)

(def config-defaults {:issuer "staging"})

(defn-spec k8s-objects any?
  [config (s/merge nextcloud/config? nextcloud/auth?)]
  (let [nextcloud-default-storage-config {:pvc-storage-class-name default-storage-class
                                          :pv-storage-size-gb 200}]
    (map yaml/to-string
         [(postgres/generate-config {:postgres-size :8gb})
          (postgres/generate-secret config)
          (postgres/generate-pvc {:pv-storage-size-gb 50
                                  :pvc-storage-class-name default-storage-class})
          (postgres/generate-deployment)
          (postgres/generate-service)
          (nextcloud/generate-secret config)
          (nextcloud/generate-pvc (merge nextcloud-default-storage-config config))
          (nextcloud/generate-deployment config)
          (nextcloud/generate-service)
          (nextcloud/generate-certificate config)]
         (nextcloud/generate-ingress config)
         (when (:contains? config :restic-repository)
           [(backup/generate-config config)
            (backup/generate-secret config)
            (backup/generate-cron)
            (backup/generate-backup-restore-deployment config)]))))

(defn-spec generate any?
  [my-config nextcloud/config?
   my-auth nextcloud/auth?]
  (cm/concat-vec
   (map yaml/to-string
        (filter #(not (nil? %))
                (merge config-defaults my-config my-auth)))))
