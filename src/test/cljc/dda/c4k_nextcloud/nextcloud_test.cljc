(ns dda.c4k-nextcloud.nextcloud-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   #?(:cljs [shadow.resource :as rc])
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as st]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-nextcloud.nextcloud :as cut]))

(st/instrument)

#?(:cljs
   (defmethod yaml/load-resource :nextcloud-test [resource-name]
     (case resource-name
       "nextcloud-test/valid-auth.yaml" (rc/inline "nextcloud-test/valid-auth.yaml")
       "nextcloud-test/valid-config.yaml" (rc/inline "nextcloud-test/valid-config.yaml")
       "nextcloud-test/invalid-auth.yaml" (rc/inline "nextcloud-test/invalid-auth.yaml")
       "nextcloud-test/invalid-config.yaml" (rc/inline "nextcloud-test/invalid-config.yaml"))))

(deftest validate-valid-resources
  (is (s/valid? cut/config? (yaml/load-as-edn "nextcloud-test/valid-config.yaml")))
  (is (s/valid? cut/auth? (yaml/load-as-edn "nextcloud-test/valid-auth.yaml")))
  (is (not (s/valid? cut/config? (yaml/load-as-edn "nextcloud-test/invalid-config.yaml"))))
  (is (not (s/valid? cut/auth? (yaml/load-as-edn "nextcloud-test/invalid-auth.yaml")))))

(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "cloud-secret"}
          :type "Opaque"
          :data
          {:nextcloud-admin-user "Y2xvdWRhZG1pbg=="
           :nextcloud-admin-password "Y2xvdWRwYXNzd29yZA=="}}
         (cut/generate-secret {:postgres-db-user "postgres-user"
                               :postgres-db-password "postgres-password"
                               :aws-access-key-id "aws-key"
                               :aws-secret-access-key "aws-secret-key"
                               :restic-password "restic-password"
                               :nextcloud-admin-user "cloudadmin"
                               :nextcloud-admin-password "cloudpassword"}))))

(deftest should-generate-ingress-and-cert
  (is (= [{:apiVersion "cert-manager.io/v1",
           :kind "Certificate",
           :metadata
           {:name "cloud-service",
            :labels {:app.kubernetes.part-of "cloud-service"},
            :namespace "default"},
           :spec
           {:secretName "cloud-service",
            :commonName "somefqdn.de",
            :duration "2160h",
            :renewBefore "720h",
            :dnsNames ["somefqdn.de"],
            :issuerRef {:name "staging", :kind "ClusterIssuer"}}}
          {:apiVersion "networking.k8s.io/v1",
           :kind "Ingress",
           :metadata
           {:name "cloud-service",
            :namespace "default",
            :labels {:app.kubernetes.part-of "cloud-service"},
            :annotations
            {:traefik.ingress.kubernetes.io/router.entrypoints "web, websecure",
             :traefik.ingress.kubernetes.io/router.middlewares
             "default-redirect-https@kubernetescrd",
             :metallb.universe.tf/address-pool "public"}},
           :spec
           {:tls [{:hosts ["somefqdn.de"], :secretName "cloud-service"}],
            :rules
            [{:host "somefqdn.de",
              :http
              {:paths
               [{:pathType "Prefix",
                 :path "/",
                 :backend
                 {:service {:name "cloud-service", :port {:number 80}}}}]}}]}}]
         (cut/generate-ingress-and-cert {:fqdn "somefqdn.de"}))))

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
             [{:image "domaindrivenarchitecture/c4k-cloud:7.0.0"
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
                {:name "NEXTCLOUD_TRUSTED_DOMAINS", :value "somefqdn.de"}
                {:name "POSTGRES_USER", :valueFrom {:secretKeyRef {:name "postgres-secret", :key "postgres-user"}}}
                {:name "POSTGRES_PASSWORD", :valueFrom {:secretKeyRef {:name "postgres-secret", :key "postgres-password"}}}
                {:name "POSTGRES_DB", :valueFrom {:configMapKeyRef {:name "postgres-config", :key "postgres-db"}}}
                {:name "POSTGRES_HOST", :value "postgresql-service:5432"}]
               :volumeMounts [{:name "cloud-data-volume", :mountPath "/var/www/html"}]}]
             :volumes [{:name "cloud-data-volume", :persistentVolumeClaim {:claimName "cloud-pvc"}}]}}}}
         (cut/generate-deployment {:fqdn "somefqdn.de"}))))