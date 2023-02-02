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
    (into
     []
     (concat [(yaml/to-string (postgres/generate-config {:postgres-size :8gb}))
              (yaml/to-string (postgres/generate-secret config))
              (yaml/to-string (postgres/generate-pvc {:pv-storage-size-gb 50
                                                      :pvc-storage-class-name default-storage-class}))
              (yaml/to-string (postgres/generate-deployment))
              (yaml/to-string (postgres/generate-service))
              (yaml/to-string (nextcloud/generate-secret config))
              (yaml/to-string (nextcloud/generate-pvc (merge nextcloud-default-storage-config config)))
              (yaml/to-string (nextcloud/generate-deployment config))
              (yaml/to-string (nextcloud/generate-service))
              (yaml/to-string (nextcloud/generate-certificate config))
              (yaml/to-string (nextcloud/generate-ingress config))]
             (when (contains? config :restic-repository)
               [(yaml/to-string (backup/generate-config config))
                (yaml/to-string (backup/generate-secret config))
                (yaml/to-string (backup/generate-cron))
                (yaml/to-string (backup/generate-backup-restore-deployment config))])))))

(defn-spec generate any?
  [my-config nextcloud/config?
   my-auth nextcloud/auth?]
  (cm/concat-vec
   (map yaml/to-string
        (filter #(not (nil? %))
                (merge config-defaults my-config my-auth)))))
