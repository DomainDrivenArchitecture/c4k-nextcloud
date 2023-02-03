(ns dda.c4k-nextcloud.nextcloud
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.ingress :as ing]
  [dda.c4k-common.base64 :as b64]
  [dda.c4k-common.predicate :as cp]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-common.common :as cm]))

(s/def ::fqdn cp/fqdn-string?)
(s/def ::issuer cp/letsencrypt-issuer?)
(s/def ::restic-repository string?)
(s/def ::nextcloud-admin-user cp/bash-env-string?)
(s/def ::nextcloud-admin-password cp/bash-env-string?)
(s/def ::pvc-storage-class-name cp/pvc-storage-class-name?)
(s/def ::pv-storage-size-gb pos?)

(def strong-config? (s/keys :req-un [::fqdn ::issuer ::pv-storage-size-gb 
                                       ::pvc-storage-class-name]
                     :opt-un [::restic-repository]))

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer
                              ::restic-repository
                              ::pv-storage-size-gb
                              ::pvc-storage-class-name]))

(def auth? (s/keys :req-un [::postgres/postgres-db-user ::postgres/postgres-db-password
                            ::nextcloud-admin-user ::nextcloud-admin-password
                            ::aws-access-key-id ::aws-secret-access-key
                            ::restic-password]))

#?(:cljs
   (defmethod yaml/load-resource :nextcloud [resource-name]
     (case resource-name
       "nextcloud/certificate.yaml" (rc/inline "nextcloud/certificate.yaml")
       "nextcloud/deployment.yaml" (rc/inline "nextcloud/deployment.yaml")
       "nextcloud/ingress.yaml" (rc/inline "nextcloud/ingress.yaml")
       "nextcloud/pvc.yaml" (rc/inline "nextcloud/pvc.yaml")
       "nextcloud/service.yaml" (rc/inline "nextcloud/service.yaml")
       "nextcloud/secret.yaml" (rc/inline "nextcloud/secret.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn-spec generate-certificate cp/map-or-seq? 
  [config config?]
  (let [{:keys [fqdn issuer]} config
        letsencrypt-issuer issuer]
    (->
     (yaml/load-as-edn "nextcloud/certificate.yaml")
     (assoc-in [:spec :commonName] fqdn)
     (assoc-in [:spec :dnsNames] [fqdn])
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn-spec generate-deployment cp/map-or-seq? 
  [config config?]
  (let [{:keys [fqdn]} config]
    (-> (yaml/load-as-edn "nextcloud/deployment.yaml")
        (cm/replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn-spec generate-ingress cp/map-or-seq?
  [config config?]
  (ing/generate-ingress-and-cert
   (merge
    {:service-name "nextcloud"
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
  [config config?]
  (let [{:keys [nextcloud-admin-user nextcloud-admin-password]} config]
    (->
     (yaml/load-as-edn "nextcloud/secret.yaml")
     (cm/replace-key-value :nextcloud-admin-user (b64/encode nextcloud-admin-user))
     (cm/replace-key-value :nextcloud-admin-password (b64/encode nextcloud-admin-password)))))
