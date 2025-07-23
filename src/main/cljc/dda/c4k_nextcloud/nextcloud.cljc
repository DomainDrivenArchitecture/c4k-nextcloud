(ns dda.c4k-nextcloud.nextcloud
  (:require
   [clojure.spec.alpha :as s]
   [orchestra.core :refer [defn-spec]]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-common.ingress :as ing]
   [dda.c4k-common.base64 :as b64]
   [dda.c4k-common.predicate :as cp]
   [dda.c4k-common.postgres :as postgres]
   [dda.c4k-common.common :as cm]
   [dda.c4k-common.monitoring :as mon]))

(s/def ::fqdn cp/fqdn-string?)
(s/def ::issuer cp/letsencrypt-issuer?)
(s/def ::nextcloud-admin-user cp/bash-env-string?)
(s/def ::nextcloud-admin-password cp/bash-env-string?)
(s/def ::pvc-storage-class-name cp/pvc-storage-class-name?)
(s/def ::pv-storage-size-gb pos?)

(s/def ::config (s/keys :req-un [::fqdn]
                     :opt-un [::issuer
                              ::pv-storage-size-gb
                              ::pvc-storage-class-name
                              ::mon/mon-cfg]))

(s/def ::auth (s/keys :req-un [::postgres/postgres-db-user ::postgres/postgres-db-password
                            ::nextcloud-admin-user ::nextcloud-admin-password
                            ::aws-access-key-id ::aws-secret-access-key]
                   :opt-un [::mon/mon-auth]))

(defn-spec generate-deployment cp/map-or-seq? 
  [config ::config]
  (let [{:keys [fqdn]} config]
    (-> (yaml/load-as-edn "nextcloud/deployment.yaml")
        (cm/replace-all-matching "fqdn" fqdn))))

(defn-spec generate-ingress-and-cert cp/map-or-seq?
  [config ::config]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "cloud-service"
     :service-port 80
     :fqdns [(:fqdn config)]}
    config)))

(defn-spec generate-pvc cp/map-or-seq?
  [config (s/keys :req-un [::pv-storage-size-gb ::pvc-storage-class-name])]
  (let [{:keys [pv-storage-size-gb pvc-storage-class-name]} config]
    (->
     (yaml/load-as-edn "nextcloud/pvc.yaml")
     (assoc-in [:spec :resources :requests :storage] (str pv-storage-size-gb "Gi"))
     (assoc-in [:spec :storageClassName] (name pvc-storage-class-name)))))

(defn generate-service []
  (yaml/load-as-edn "nextcloud/service.yaml"))

(defn-spec generate-secret cp/map-or-seq? 
  [auth ::auth]
  (let [{:keys [nextcloud-admin-user nextcloud-admin-password]} auth]
    (->
     (yaml/load-as-edn "nextcloud/secret.yaml")
     (cm/replace-key-value :nextcloud-admin-user (b64/encode nextcloud-admin-user))
     (cm/replace-key-value :nextcloud-admin-password (b64/encode nextcloud-admin-password)))))
