(ns dda.c4k-cloud.cloud
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]))

(s/def ::fqdn cm/fqdn-string?)
(s/def ::issuer cm/letsencrypt-issuer?)
(s/def ::cloud-data-volume-path string?)

#?(:cljs
   (defmethod yaml/load-resource :cloud [resource-name]
     (case resource-name
       "cloud/certificate.yaml" (rc/inline "cloud/certificate.yaml")
       "cloud/deployment.yaml" (rc/inline "cloud/deployment.yaml")
       "cloud/ingress.yaml" (rc/inline "cloud/ingress.yaml")
       "cloud/persistent-volume.yaml" (rc/inline "cloud/persistent-volume.yaml")
       "cloud/pvc.yaml" (rc/inline "cloud/pvc.yaml")
       "cloud/service.yaml" (rc/inline "cloud/service.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn generate-certificate [config]
  (let [{:keys [fqdn issuer]} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "cloud/certificate.yaml"))
     (assoc-in [:spec :commonName] fqdn)
     (assoc-in [:spec :dnsNames] [fqdn])
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn generate-deployment [config]
  (let [{:keys [fqdn]} config]
    (-> (yaml/from-string (yaml/load-resource "cloud/deployment.yaml"))
        (cm/replace-named-value "FQDN" fqdn))))

(defn generate-ingress [config]
  (let [{:keys [fqdn issuer]
         :or {issuer :staging}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "cloud/ingress.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (cm/replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn generate-persistent-volume [config]
  (let [{:keys [cloud-data-volume-path]} config]
    (-> 
     (yaml/from-string (yaml/load-resource "cloud/persistent-volume.yaml"))
     (assoc-in [:spec :hostPath :path] cloud-data-volume-path))))

(defn generate-pvc []
  (yaml/from-string (yaml/load-resource "cloud/pvc.yaml")))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "cloud/service.yaml")))
