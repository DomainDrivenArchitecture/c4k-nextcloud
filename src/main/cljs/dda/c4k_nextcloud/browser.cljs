(ns dda.c4k-nextcloud.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-common.common :as cm]
   [dda.c4k-nextcloud.core :as core]
   [dda.c4k-nextcloud.nextcloud :as nextcloud]
   [dda.c4k-common.browser :as br]
   [dda.c4k-common.postgres :as pgc]))

(defn generate-content []
  (cm/concat-vec
   [(assoc
     (br/generate-needs-validation) :content
     (cm/concat-vec
      (br/generate-group "domain"
                         (cm/concat-vec (br/generate-input-field "fqdn" "Your fqdn:" "nextcloud-neu.prod.meissa-gmbh.de")
                                        (br/generate-input-field "pv-storage-size-gb" "(Optional) Your nextcloud storage size in GB" "8")
                                        (br/generate-input-field "pvc-storage-class-name" "(Optional) Your storage class type (manual / local-path):" "local-path")
                                        (br/generate-input-field "postgres-data-volume-path" "(Optional) Your postgres-data-volume-path:" "/var/postgres")
                                        (br/generate-input-field "restic-repository" "(Optional) Your restic-repository:" "restic-repository")
                                        (br/generate-input-field "issuer" "(Optional) Your issuer prod/staging:" "")))
      (br/generate-group "credentials"
                         (br/generate-text-area "auth" "Your auth.edn:" "{:postgres-db-user \"nextcloud\"
         :postgres-db-password \"nextcloud-db-password\"
         :nextcloud-admin-password \"nextcloud-admin-password\"
         :nextcloud-admin-user \"nextcloud-admin-user\"                                                                                  
         :aws-access-key-id \"aws-id\"
         :aws-secret-access-key \"aws-secret\"
         :restic-password \"restic-password\"}"
                                                "5"))
      [(br/generate-br)]
      (br/generate-button "generate-button" "Generate c4k yaml")))]
   (br/generate-output "c4k-nextcloud-output" "Your c4k deployment.yaml:" "25")))

(defn generate-content-div
  []
  {:type :element
   :tag :div
   :content
   (generate-content)})

(defn config-from-document []
  (let [pv-storage-size-gb (br/get-content-from-element "pv-storage-size-gb" :optional true)
        pvc-storage-class-name (br/get-content-from-element "pvc-storage-class-name" :optional true)
        postgres-data-volume-path (br/get-content-from-element "postgres-data-volume-path" :optional true)
        restic-repository (br/get-content-from-element "restic-repository" :optional true)
        issuer (br/get-content-from-element "issuer" :optional true :deserializer keyword)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}
     (when (and (some? pv-storage-size-gb) (some? pvc-storage-class-name))
       {:pv-storage-size-gb pv-storage-size-gb :pvc-storage-class-name pvc-storage-class-name})
     (when (some? postgres-data-volume-path)
       {:postgres-data-volume-path postgres-data-volume-path})
     (when (some? restic-repository)
       {:restic-repository restic-repository})
     (when (some? issuer)
       {:issuer issuer}))))

(defn validate-all! []
  (br/validate! "fqdn" ::nextcloud/fqdn)
  (br/validate! "pv-storage-size-gb" ::nextcloud/pv-storage-size-gb :optional true)
  (br/validate! "pvc-storage-class-name" ::nextcloud/pvc-storage-class-name :optional true)
  (br/validate! "postgres-data-volume-path" ::pgc/postgres-data-volume-path :optional true)
  (br/validate! "restic-repository" ::nextcloud/restic-repository :optional true)
  (br/validate! "issuer" ::nextcloud/issuer :optional true)
  (br/validate! "auth" nextcloud/auth? :deserializer edn/read-string)
  (br/set-validated!))

(defn add-validate-listener [name]
  (-> (br/get-element-by-id name)
      (.addEventListener "blur" #(do (validate-all!)))))

(defn init []
  (br/append-hickory (generate-content-div))
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-all!)
                              (-> (cm/generate-common
                                   (config-from-document)
                                   (br/get-content-from-element "auth" :deserializer edn/read-string)
                                   {}
                                   core/k8s-objects)
                                  (br/set-output!)))))
  (add-validate-listener "fqdn")
  (add-validate-listener "pv-storage-size-gb")
  (add-validate-listener "pvc-storage-class-name")
  (add-validate-listener "postgres-data-volume-path")
  (add-validate-listener "restic-repository")
  (add-validate-listener "issuer")
  (add-validate-listener "auth"))
