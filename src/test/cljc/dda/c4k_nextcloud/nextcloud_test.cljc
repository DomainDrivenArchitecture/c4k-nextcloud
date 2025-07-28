(ns dda.c4k-nextcloud.nextcloud-test
  (:require
   [clojure.test :refer [deftest is are testing run-tests]]
   [clojure.spec.test.alpha :as st]
   [dda.c4k-nextcloud.nextcloud :as cut]))

(st/instrument `cut/generate-secret)
(st/instrument `cut/generate-ingress-and-cert)
(st/instrument `cut/generate-pvc)
(st/instrument `cut/generate-deployment)

(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "cloud-secret", :namespace "nextcloud"}
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

(deftest should-generate-pvc
  (is (= {:apiVersion "v1"
          :kind "PersistentVolumeClaim"
          :metadata {:name "cloud-pvc"
                     :namespace "nextcloud"
                     :labels {:app.kubernetes.io/application "cloud"}}
          :spec {:storageClassName "local-path"
                 :accessModes ["ReadWriteOnce"]
                 :resources {:requests {:storage "50Gi"}}}}
         (cut/generate-pvc {:pv-storage-size-gb 50 :pvc-storage-class-name "local-path"}))))

(deftest should-generate-deployment
  (is (= {:apiVersion "apps/v1"
          :kind "Deployment"
          :metadata {:name "cloud-deployment", :namespace "nextcloud"}
          :spec
          {:selector {:matchLabels #:app.kubernetes.io{:name "cloud-pod", :application "cloud"}}
           :strategy {:type "Recreate"}
           :template
           {:metadata {:labels {:app "cloud-app", :app.kubernetes.io/name "cloud-pod", :app.kubernetes.io/application "cloud", :redeploy "v3"}}
            :spec
            {:containers
             [{:image "domaindrivenarchitecture/c4k-cloud:10.5.3"
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