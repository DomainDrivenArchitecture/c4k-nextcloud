(ns dda.c4k-nextcloud.backup-test
  (:require
   [clojure.test :refer [deftest is are testing run-tests]]
   [clojure.spec.test.alpha :as st]
   [dda.c4k-nextcloud.backup :as cut]))

(st/instrument `cut/generate-secret)
(st/instrument `cut/generate-config)
(st/instrument `cut/generate-cron)

(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "backup-secret", :namespace "nextcloud"}
          :type "Opaque"
          :data
          {:aws-access-key-id "YXdzLWlk", 
           :aws-secret-access-key "YXdzLXNlY3JldA==", 
           :restic-password "cmVzdGljLXB3"}}
         (cut/generate-secret {:aws-access-key-id "aws-id" 
                               :aws-secret-access-key "aws-secret" 
                               :restic-password "restic-pw"})))
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "backup-secret", :namespace "nextcloud"}
          :type "Opaque"
          :data
          {:aws-access-key-id "YXdzLWlk",
           :aws-secret-access-key "YXdzLXNlY3JldA==",
           :restic-password "cmVzdGljLXB3"
           :restic-new-password "bmV3LXJlc3RpYy1wdw=="}}
         (cut/generate-secret {:aws-access-key-id "aws-id"
                               :aws-secret-access-key "aws-secret"
                               :restic-password "restic-pw"
                               :restic-new-password "new-restic-pw"}))))

(deftest should-generate-config
  (is (= {:apiVersion "v1"
          :kind "ConfigMap"
          :metadata {:name "backup-config"
                     :namespace "nextcloud"
                     :labels {:app.kubernetes.io/name "backup"
                              :app.kubernetes.io/part-of "cloud"}}
          :data
          {:restic-repository "s3:restic-repository"}}
         (cut/generate-config {:restic-repository "s3:restic-repository"}))))

(deftest should-generate-cron
  (is (= {:apiVersion "batch/v1"
          :kind "CronJob"
          :metadata {:name "cloud-backup", :namespace "nextcloud", :labels {:app.kubernetes.part-of "cloud"}}
          :spec
          {:schedule "10 23 * * *"
           :successfulJobsHistoryLimit 1
           :failedJobsHistoryLimit 1
           :jobTemplate
           {:spec
            {:template
             {:spec
              {:containers
               [{:name "backup-app"
                 :image "domaindrivenarchitecture/c4k-cloud-backup"
                 :imagePullPolicy "IfNotPresent"
                 :command ["backup.bb"]
                 :env
                 [{:valueFrom
                   {:secretKeyRef
                    {:name "postgres-secret",
                     :key "postgres-user"}},
                   :name "POSTGRES_USER"}
                  {:valueFrom
                   {:secretKeyRef
                    {:name "postgres-secret",
                     :key "postgres-password"}},
                   :name "POSTGRES_PASSWORD"}
                  {:valueFrom
                   {:configMapKeyRef
                    {:name "postgres-config", :key "postgres-db"}},
                   :name "POSTGRES_DB"}
                  {:name "POSTGRES_HOST", :value "postgresql-service:5432"}
                  {:name "POSTGRES_SERVICE", :value "postgresql-service"}
                  {:name "POSTGRES_PORT", :value "5432"}
                  {:name "AWS_DEFAULT_REGION", :value "eu-central-1"}
                  {:name "AWS_ACCESS_KEY_ID_FILE", :value "/var/run/secrets/backup-secrets/aws-access-key-id"}
                  {:name "AWS_SECRET_ACCESS_KEY_FILE", :value "/var/run/secrets/backup-secrets/aws-secret-access-key"}
                  {:name "RESTIC_REPOSITORY", :valueFrom {:configMapKeyRef {:name "backup-config", :key "restic-repository"}}}
                  {:name "RESTIC_PASSWORD_FILE", :value "/var/run/secrets/backup-secrets/restic-password"}]
                 :volumeMounts
                 [{:name "cloud-data-volume", :mountPath "/var/backups"}
                  {:name "backup-secret-volume", :mountPath "/var/run/secrets/backup-secrets", :readOnly true}
                  {:name "cloud-secret-volume", :mountPath "/var/run/secrets/cloud-secrets", :readOnly true}]}]
               :volumes
               [{:name "cloud-data-volume", :persistentVolumeClaim {:claimName "cloud-pvc"}}
                {:name "cloud-secret-volume", :secret {:secretName "cloud-secret"}}
                {:name "backup-secret-volume", :secret {:secretName "backup-secret"}}]
               :restartPolicy "OnFailure"}}}}}}
         (cut/generate-cron))))
