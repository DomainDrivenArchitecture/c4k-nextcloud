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
          {:tls [{:hosts ["xx"], :secretName "nextcloud-secret"}]
           :rules
           [{:host "xx"
             :http
             {:paths
              [{:path "/"
                :backend
                {:serviceName "nextcloud-service", :servicePort 80}}]}}]}}
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
  (is (= {:containers
           [{:image "domaindrivenarchitecture/meissa-cloud-app"
             :name "cloud-app"
             :imagePullPolicy "IfNotPresent"
             :ports [{:containerPort 80}]
             :env
             [{:name "DB_USERNAME_FILE"
               :value
               "/var/run/secrets/postgres-secret/postgres-user"}
              {:name "DB_PASSWORD_FILE"
               :value
               "/var/run/secrets/postgres-secret/postgres-password"}
              {:name "FQDN", :value "xx"}]
             :command ["/app/entrypoint.sh"]
             :volumeMounts
             [{:mountPath "/var/nextcloud", :name "cloud-data-volume"}
              {:name "postgres-secret-volume"
               :mountPath "/var/run/secrets/postgres-secret"
               :readOnly true}]}]
           :volumes
           [{:name "cloud-data-volume"
             :persistentVolumeClaim {:claimName "cloud-pvc"}}
            {:name "postgres-secret-volume"
             :secret {:secretName "postgres-secret"}}]}
         (get-in (cut/generate-deployment {:fqdn "xx"}) [:spec :template :spec]))))
