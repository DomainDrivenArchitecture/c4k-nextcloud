(ns dda.c4k-nextcloud.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-nextcloud.core :as core]
   [dda.c4k-nextcloud.nextcloud :as nextcloud]
   [dda.c4k-common.browser :as br]
   [dda.c4k-common.postgres :as pgc]))

(defn config-from-document []
  (let [nextcloud-data-volume-path (br/get-content-from-element "nextcloud-data-volume-path" :optional true)
        postgres-data-volume-path (br/get-content-from-element "postgres-data-volume-path" :optional true)
        restic-repository (br/get-content-from-element "restic-repository" :optional true)
        issuer (br/get-content-from-element "issuer" :optional true :deserializer keyword)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}
     (when (some? nextcloud-data-volume-path)
       {:nextcloud-data-volume-path nextcloud-data-volume-path})
     (when (some? postgres-data-volume-path)
       {:postgres-data-volume-path postgres-data-volume-path})
     (when (some? restic-repository)
       {:restic-repository restic-repository})
     (when (some? issuer)
       {:issuer issuer})
     )))

(defn validate-all! []
  (br/validate! "fqdn" ::nextcloud/fqdn)
  (br/validate! "nextcloud-data-volume-path" ::nextcloud/nextcloud-data-volume-path :optional true)
  (br/validate! "postgres-data-volume-path" ::pgc/postgres-data-volume-path :optional true)
  (br/validate! "restic-repository" ::nextcloud/restic-repository :optional true)
  (br/validate! "issuer" ::nextcloud/issuer :optional true :deserializer keyword)
  (br/validate! "auth" core/auth? :deserializer edn/read-string)
  (br/set-validated!))

(defn init []
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-all!)
                              (-> (core/generate 
                                   (config-from-document) 
                                   (br/get-content-from-element "auth" :deserializer edn/read-string))
                                  (br/set-output!)))))
  (-> (br/get-element-by-id "fqdn")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "nextcloud-data-volume-path")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "postgres-data-volume-path")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "restic-repository")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "issuer")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "auth")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  )