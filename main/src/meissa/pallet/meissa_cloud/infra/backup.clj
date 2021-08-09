(ns meissa.pallet.meissa-cloud.infra.backup
  (:require
   [schema.core :as s]
   [dda.provision :as p]
   [dda.provision.pallet :as pp]))

(s/def Backup
  {:restic-repository s/Str
   :aws-access-key-id s/Str
   :aws-secret-access-key s/Str
   :restic-password s/Str})

(def MeissaBackupInfra {:backup Backup})

(def backup "backup")

(defn init [facility user config])

(defn install
  [facility user config]
  (let [facility-name (name facility)]
    (p/provision-log ::pp/pallet facility-name backup
                     ::p/info "install")
    (p/copy-resources-to-user
     ::pp/pallet user facility-name backup
     [{:filename "backup-secret.yml" :config config}
      {:filename "backup-config.yml" :config config}
      {:filename "configure-as-user.sh"}
      {:filename "backup-restore.yml"}
      {:filename "backup-cron.yml"}])))

(defn configure
  [facility user config]
  (let [facility-name (name facility)]
    (p/provision-log ::pp/pallet facility-name backup
                     ::p/info "configure")
    (p/exec-file-on-target-as-user
     ::pp/pallet user facility-name backup "configure-as-user.sh")
  ))
