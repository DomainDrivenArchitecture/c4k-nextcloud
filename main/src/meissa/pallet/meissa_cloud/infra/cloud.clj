(ns meissa.pallet.meissa-cloud.infra.cloud
  (:require
   [schema.core :as s]
   [dda.provision :as p]
   [dda.provision.pallet :as pp]))

(s/def Cloud
  {:fqdn s/Str
   :secret-name s/Str
   :cluster-issuer s/Str
   :db-name s/Str
   :db-user-name s/Str 
   :db-user-password s/Str
   :admin-user s/Str
   :admin-password s/Str
   :storage-size s/Str})

(def MeissaCloudInfra {:cloud Cloud})

(def cloud "cloud")

(defn init
  [facility user config]
  (let [facility-name (name facility)]
    (p/provision-log ::pp/pallet facility-name cloud
                     ::p/info "init")
    (p/copy-resources-to-tmp
     ::pp/pallet facility-name cloud
     [{:filename "install-as-root.sh" :config {:user user}}])))


(defn install
  [facility user config]
  (let [facility-name (name facility)]
    (p/provision-log ::pp/pallet facility-name cloud
                     ::p/info "install")
    (p/copy-resources-to-user
     ::pp/pallet user facility-name cloud
     [{:filename "pod-running.sh"}
      {:filename "cloud-persistent-volume.yml" :config config}
      {:filename "cloud-secret.yml" :config config}
      {:filename "cloud-service.yml"}
      {:filename "cloud-pvc.yml" :config config}
      {:filename "cloud-pod.yml" :config config}
      {:filename "cloud-ingress.yml" :config config}
      {:filename "configure-as-user.sh"}
      {:filename "verify.sh" :config config}])
    (p/exec-file-on-target-as-root
     ::pp/pallet facility-name cloud "install-as-root.sh")))

(defn configure
  [facility user config]
  (let [facility-name (name facility)]
    (p/provision-log ::pp/pallet facility-name cloud
                     ::p/info "configure")
    (p/exec-file-on-target-as-user
     ::pp/pallet user facility-name cloud "configure-as-user.sh")))
