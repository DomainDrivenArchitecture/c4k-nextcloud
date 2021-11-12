(ns dda.c4k-nextcloud.backup-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-nextcloud.backup :as cut]))


(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "backup-secret"}
          :type "Opaque"
          :data
          {:aws-access-key-id "YXdzLWlk", :aws-secret-access-key "YXdzLXNlY3JldA==", :restic-password "cmVzdGljLXB3"}}
         (cut/generate-secret {:aws-access-key-id "aws-id" :aws-secret-access-key "aws-secret" :restic-password "restic-pw"}))))

(deftest should-generate-config
  (is (= {:apiVersion "v1"
          :kind "ConfigMap"
          :metadata {:name "backup-config"
                     :labels {:app.kubernetes.io/name "backup"
                              :app.kubernetes.io/part-of "cloud"}}
          :data
          {:restic-repository "s3:restic-repository"}}
         (cut/generate-config {:restic-repository "s3:restic-repository"}))))

(deftest should-generate-cron
  (is (= {:apiVersion "batch/v1"
          :kind "CronJob"
          :metadata {:name "cloud-backup", :labels {:app.kubernetes.part-of "cloud"}}
          :spec
          {:schedule "10 23 * * *"
           :successfulJobsHistoryLimit 0
           :failedJobsHistoryLimit 0
           :jobTemplate
           {:spec
            {:template
             {:spec
              {:containers
               [{:name "backup-app"
                 :image "domaindrivenarchitecture/c4k-cloud-backup"
                 :imagePullPolicy "IfNotPresent"
                 :command ["/entrypoint.sh"]
                 :env
                 [{:name "POSTGRES_USER_FILE", :value "/var/run/secrets/cloud-secrets/postgres-user"}
                  {:name "POSTGRES_DB_FILE", :value "/var/run/secrets/cloud-secrets/postgres-db"}
                  {:name "POSTGRES_PASSWORD_FILE", :value "/var/run/secrets/cloud-secrets/postgres-password"}
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
