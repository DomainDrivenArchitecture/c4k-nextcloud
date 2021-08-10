(ns dda.c4k-nextcloud.nextcloud
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]))

(s/def ::fqdn cm/fqdn-string?)
(s/def ::issuer cm/letsencrypt-issuer?)
(s/def ::restic-repository string?)
(s/def ::nextcloud-data-volume-path string?)

#?(:cljs
   (defmethod yaml/load-resource :nextcloud [resource-name]
     (case resource-name
       "nextcloud/certificate.yaml" (rc/inline "nextcloud/certificate.yaml")
       "nextcloud/deployment.yaml" (rc/inline "nextcloud/deployment.yaml")
       "nextcloud/ingress.yaml" (rc/inline "nextcloud/ingress.yaml")
       "nextcloud/persistent-volume.yaml" (rc/inline "nextcloud/persistent-volume.yaml")
       "nextcloud/pvc.yaml" (rc/inline "nextcloud/pvc.yaml")
       "nextcloud/service.yaml" (rc/inline "nextcloud/service.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn generate-certificate [config]
  (let [{:keys [fqdn issuer]} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "nextcloud/certificate.yaml"))
     (assoc-in [:spec :commonName] fqdn)
     (assoc-in [:spec :dnsNames] [fqdn])
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn generate-deployment [config]
  (let [{:keys [fqdn]} config]
    (-> (yaml/from-string (yaml/load-resource "nextcloud/deployment.yaml"))
        (cm/replace-named-value "FQDN" fqdn))))

(defn generate-ingress [config]
  (let [{:keys [fqdn issuer]
         :or {issuer :staging}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "nextcloud/ingress.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (cm/replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn generate-persistent-volume [config]
  (let [{:keys [nextcloud-data-volume-path]} config]
    (-> 
     (yaml/from-string (yaml/load-resource "nextcloud/persistent-volume.yaml"))
     (assoc-in [:spec :hostPath :path] nextcloud-data-volume-path))))

(defn generate-pvc []
  (yaml/from-string (yaml/load-resource "nextcloud/pvc.yaml")))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "nextcloud/service.yaml")))
