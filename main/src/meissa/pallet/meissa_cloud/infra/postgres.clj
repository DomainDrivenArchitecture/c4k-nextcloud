(ns meissa.pallet.meissa-cloud.infra.postgres
  (:require
   [schema.core :as s]
   [dda.provision :as p]
   [dda.provision.pallet :as pp]))

(s/def Postgres {:db-user-name s/Str :db-user-password s/Str})

(def MeissaPostgresInfra {:postgres Postgres})

(def postgres "postgres")

(defn init
  [facility user config]
  (let [facility-name (name facility)]
    (p/provision-log ::pp/pallet facility-name postgres
                     ::p/info "init")
    (p/copy-resources-to-tmp
     ::pp/pallet facility-name postgres
     [{:filename "install-as-root.sh" :config {:user user}}])))


(defn install
  [facility user config]
  (let [facility-name (name facility)]
    (p/provision-log ::pp/pallet facility-name postgres
                     ::p/info "install")
    (p/copy-resources-to-user
     ::pp/pallet user facility-name postgres
     [{:filename "postgres-persistent-volume.yml"}
      {:filename "postgres-secret.yml" :config config}
      {:filename "postgres-config.yml"}
      {:filename "postgres-service.yml"}
      {:filename "postgres-pvc.yml"}
      {:filename "postgres-deployment.yml" :config config}
      {:filename "configure-as-user.sh"}
      {:filename "verify.sh"}])
    (p/exec-file-on-target-as-root
     ::pp/pallet facility-name postgres "install-as-root.sh")))

(defn configure
  [facility user config]
  (let [facility-name (name facility)]
    (p/provision-log ::pp/pallet facility-name postgres
                     ::p/info "configure")
    (p/exec-file-on-target-as-user
     ::pp/pallet user facility-name postgres "configure-as-user.sh")))
