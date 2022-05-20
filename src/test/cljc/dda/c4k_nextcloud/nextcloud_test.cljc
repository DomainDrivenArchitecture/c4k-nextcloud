(ns dda.c4k-nextcloud.nextcloud-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-nextcloud.nextcloud :as cut]))

(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "cloud-secret"}
          :type "Opaque"
          :data
          {:nextcloud-admin-user "Y2xvdWRhZG1pbg=="
           :nextcloud-admin-password "Y2xvdWRwYXNzd29yZA=="}}
         (cut/generate-secret {:nextcloud-admin-user "cloudadmin"
                               :nextcloud-admin-password "cloudpassword"}))))

(deftest should-generate-certificate
  (is (= {:apiVersion "cert-manager.io/v1"
          :kind "Certificate"
          :metadata {:name "cloud-cert", :namespace "default"}
          :spec
          {:secretName "cloud-secret"
           :commonName "xx"
           :dnsNames ["xx"]
           :issuerRef
           {:name "prod", :kind "ClusterIssuer"}}}
         (cut/generate-certificate {:fqdn "xx" :issuer "prod"}))))

(deftest should-generate-ingress
  (is (= {:apiVersion "networking.k8s.io/v1"
          :kind "Ingress"
          :metadata
          {:name "ingress-cloud"
           :annotations
           {:cert-manager.io/cluster-issuer "staging"
            :ingress.kubernetes.io/proxy-body-size "256m"
            :ingress.kubernetes.io/ssl-redirect "true"
            :ingress.kubernetes.io/rewrite-target "/"
            :ingress.kubernetes.io/proxy-connect-timeout "300"
            :ingress.kubernetes.io/proxy-send-timeout "300"
            :ingress.kubernetes.io/proxy-read-timeout "300"}
           :namespace "default"}
          :spec
          {:tls [{:hosts ["xx"], :secretName "cloud-secret"}]
           :rules
           [{:host "xx"
             :http
             {:paths
              [{:path "/"
                :pathType "Prefix"
                :backend
                {:service
                 {:name "cloud-service", :port {:number 80}}}}]}}]}}
         (cut/generate-ingress {:fqdn "xx"}))))

(deftest should-generate-pvc
  (is (= {:apiVersion "v1"
          :kind "PersistentVolumeClaim"
          :metadata {:name "cloud-pvc"
                    :labels {:app.kubernetes.io/application "cloud"}}
          :spec {:storageClassName "local-path"
                  :accessModes ["ReadWriteOnce"]
                  :resources {:requests {:storage "50Gi"}}}}
         (cut/generate-pvc {:pv-storage-size-gb 50 :pvc-storage-class-name "local-path"}))))
 
(deftest should-generate-deployment
  (is (= {:apiVersion "apps/v1"
          :kind "Deployment"
          :metadata {:name "cloud-deployment"}
          :spec
          {:selector {:matchLabels #:app.kubernetes.io{:name "cloud-pod", :application "cloud"}}
           :strategy {:type "Recreate"}
           :template
           {:metadata {:labels {:app "cloud-app", :app.kubernetes.io/name "cloud-pod", :app.kubernetes.io/application "cloud", :redeploy "v3"}}
            :spec
            {:containers
             [{:image "domaindrivenarchitecture/c4k-cloud"
               :name "cloud-app"
               :imagePullPolicy "IfNotPresent"
               :ports [{:containerPort 80}]
               :livenessProbe
               {:exec
                {:command
                 ["/bin/sh"
                  "-c"
                  "PGPASSWORD=$POSTGRES_PASSWORD psql -h postgresql-service -U $POSTGRES_USER $POSTGRES_DB"]}
                :initialDelaySeconds 1
                :periodSeconds 5}
               :env
               [{:name "NEXTCLOUD_ADMIN_USER", :valueFrom {:secretKeyRef {:name "cloud-secret", :key "nextcloud-admin-user"}}}
                {:name "NEXTCLOUD_ADMIN_PASSWORD"
                 :valueFrom {:secretKeyRef {:name "cloud-secret", :key "nextcloud-admin-password"}}}
                {:name "NEXTCLOUD_TRUSTED_DOMAINS", :value "xx"}
                {:name "POSTGRES_USER", :valueFrom {:secretKeyRef {:name "postgres-secret", :key "postgres-user"}}}
                {:name "POSTGRES_PASSWORD", :valueFrom {:secretKeyRef {:name "postgres-secret", :key "postgres-password"}}}
                {:name "POSTGRES_DB", :valueFrom {:configMapKeyRef {:name "postgres-config", :key "postgres-db"}}}
                {:name "POSTGRES_HOST", :value "postgresql-service:5432"}]
               :volumeMounts [{:name "cloud-data-volume", :mountPath "/var/www/html"}]}]
             :volumes [{:name "cloud-data-volume", :persistentVolumeClaim {:claimName "cloud-pvc"}}]}}}}
         (cut/generate-deployment {:fqdn "xx"}))))
