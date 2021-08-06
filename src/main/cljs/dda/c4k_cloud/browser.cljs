(ns dda.c4k-cloud.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-cloud.core :as core]
   [dda.c4k-cloud.cloud :as cloud]
   [dda.c4k-common.browser :as br]))

(defn config-from-document []
  (let [cloud-data-volume-path (br/get-content-from-element "cloud-data-volume-path" :optional true :deserializer keyword)
        postgres-data-volume-path (br/get-content-from-element "postgres-data-volume-path" :optional true :deserializer keyword)
        restic-repository (br/get-content-from-element "restic-repository" :optional true :deserializer keyword)
        issuer (br/get-content-from-element "issuer" :optional true :deserializer keyword)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}
     (when (some? cloud-data-volume-path)
       {:cloud-data-volume-path cloud-data-volume-path})
     (when (some? postgres-data-volume-path)
       {:postgres-data-volume-path postgres-data-volume-path})
     (when (some? restic-repository)
       {:restic-repository restic-repository})
     (when (some? issuer)
       {:issuer issuer})
     )))

(defn validate-all! []
  (br/validate! "fqdn" ::cloud/fqdn)
  (br/validate! "cloud-data-volume-path" ::cloud/cloud-data-volume-path :optional true :deserializer keyword)
  (br/validate! "postgres-data-volume-path" ::cloud/cloud-data-volume-path :optional true :deserializer keyword)
  (br/validate! "restic-repository" ::cloud/restic-repository :optional true :deserializer keyword)
  (br/validate! "issuer" ::cloud/issuer :optional true :deserializer keyword)
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
  (-> (br/get-element-by-id "cloud-data-volume-path")
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