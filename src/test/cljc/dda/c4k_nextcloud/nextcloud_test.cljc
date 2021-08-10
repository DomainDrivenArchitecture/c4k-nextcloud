(ns dda.c4k-nextcloud.nextcloud-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-nextcloud.nextcloud :as cut]))

(deftest should-generate-certificate
  (is (= {:apiVersion "cert-manager.io/v1alpha2"
          :kind "Certificate"
          :metadata {:name "cloud-cert", :namespace "default"}
          :spec
          {:secretName "cloud-secret"
           :commonName "xx"
           :dnsNames ["xx"]
           :issuerRef
           {:name "letsencrypt-prod-issuer", :kind "ClusterIssuer"}}}
         (cut/generate-certificate {:fqdn "xx" :issuer :prod}))))

(deftest should-generate-ingress
  (is (= {:apiVersion "extensions/v1beta1"
          :kind "Ingress"
          :metadata
          {:name "ingress-cloud"
           :annotations
           {:cert-manager.io/cluster-issuer
            "letsencrypt-staging-issuer"
            :nginx.ingress.kubernetes.io/proxy-body-size "256m"
            :nginx.ingress.kubernetes.io/ssl-redirect "true"
            :nginx.ingress.kubernetes.io/rewrite-target "/"
            :nginx.ingress.kubernetes.io/proxy-connect-timeout "300"
            :nginx.ingress.kubernetes.io/proxy-send-timeout "300"
            :nginx.ingress.kubernetes.io/proxy-read-timeout "300"}
           :namespace "default"}
          :spec
          {:tls [{:hosts ["xx"], :secretName "cloud-secret"}]
           :rules
           [{:host "xx"
             :http
             {:paths
              [{:path "/"
                :backend
                {:serviceName "cloud-service", :servicePort 80}}]}}]}}
         (cut/generate-ingress {:fqdn "xx"}))))

(deftest should-generate-persistent-volume
  (is (= {:kind "PersistentVolume"
          :apiVersion "v1"
          :metadata {:name "cloud-pv-volume", :labels {:type "local" :app "cloud"}}
          :spec
          {:storageClassName "manual"
           :accessModes ["ReadWriteOnce"]
           :capacity {:storage "200Gi"}
           :hostPath {:path "xx"}}}
         (cut/generate-persistent-volume {:nextcloud-data-volume-path "xx"}))))

(deftest should-generate-deployment
  (is (= {:apiVersion "apps/v1"
          :kind "Deployment"
          :metadata {:name "cloud"}
          :spec
          {:selector {:matchLabels {:app "cloud"}}
           :strategy {:type "Recreate"}
           :template
           {:metadata {:labels {:app "cloud"}}
            :spec
            {:containers
             [{:image "domaindrivenarchitecture/meissa-cloud-app"
               :name "cloud-app"
               :imagePullPolicy "IfNotPresent"
               :ports [{:containerPort 80}]
               :env
               [{:name "NEXTCLOUD_ADMIN_USER_FILE", :value "/var/run/secrets/cloud-secrets/nextcloud-admin-user"}
                {:name "NEXTCLOUD_ADMIN_PASSWORD_FILE", :value "/var/run/secrets/cloud-secrets/nextcloud-admin-password"}
                {:name "NEXTCLOUD_TRUSTED_DOMAINS", :value "xx"}
                {:name "POSTGRES_USER_FILE", :value "/var/run/secrets/cloud-secrets/postgres-user"}
                {:name "POSTGRES_PASSWORD_FILE", :value "/var/run/secrets/cloud-secrets/postgres-password"}
                {:name "POSTGRES_DB_FILE", :value "/var/run/secrets/cloud-secrets/postgres-db"}
                {:name "POSTGRES_HOST", :value "postgresql-service:5432"}]
               :volumeMounts
               [{:name "cloud-data-volume", :mountPath "/var/www/html"}
                {:name "cloud-secret-volume", :mountPath "/var/run/secrets/cloud-secrets", :readOnly true}]}]
             :volumes
             [{:name "cloud-data-volume", :persistentVolumeClaim {:claimName "cloud-pvc"}}
              {:name "cloud-secret-volume", :secret {:secretName "cloud-secret"}}
              {:name "backup-secret-volume", :secret {:secretName "backup-secret"}}]}}}}
         (cut/generate-deployment {:fqdn "xx"}))))
