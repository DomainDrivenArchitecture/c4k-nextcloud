(ns meissa.pallet.meissa-cloud.convention
  (:require
   [schema.core :as s]
   [dda.pallet.commons.secret :as secret]
   [dda.config.commons.map-utils :as mu]
   [clojure.spec.alpha :as sp]
   [clojure.spec.test.alpha :as st]
   [dda.pallet.dda-k8s-crate.convention :as k8s-convention]
   [meissa.pallet.meissa-cloud.infra :as infra]
   [clojure.string :as str]
   [meissa.pallet.meissa-cloud.convention.bash :as bash]
   [meissa.pallet.meissa-cloud.convention.bash-php :as bash-php]))

(def InfraResult {infra/facility infra/MeissaCloudInfra})

(s/def CloudConvention
  {:user s/Keyword
   :external-ip s/Str
   :fqdn s/Str
   :cert-manager (s/enum :letsencrypt-prod-issuer :letsencrypt-staging-issuer)
   :db-user-password secret/Secret
   :admin-user s/Str
   :admin-password secret/Secret
   :storage-size s/Int
   :restic-repository s/Str
   :aws-access-key-id secret/Secret
   :aws-secret-access-key secret/Secret
   :restic-password secret/Secret
   (s/optional-key :u18-04) (s/enum true)})

(def CloudConventionResolved (secret/create-resolved-schema CloudConvention))

(sp/def ::user keyword?)
(sp/def ::external-ip string?)
(sp/def ::fqdn string?)
(sp/def ::cert-manager #{:letsencrypt-prod-issuer :letsencrypt-staging-issuer})
(sp/def ::db-user-password bash-php/bash-php-env-string?)
(sp/def ::admin-user bash-php/bash-php-env-string?)
(sp/def ::admin-password bash-php/bash-php-env-string?)
(sp/def ::storage-size int?)
(sp/def ::restic-repository string?)
(sp/def ::restic-password bash/bash-env-string?)
(sp/def ::aws-access-key-id bash/bash-env-string?)
(sp/def ::aws-secret-access-key bash/bash-env-string?)
(sp/def ::u18-04 #{true})
(def cloud-convention-resolved? (sp/keys :req-un [::user ::external-ip ::fqdn ::cert-manager
                                                 ::db-user-password ::admin-user ::admin-password
                                                 ::storage-size ::restic-repository ::restic-password
                                                 ::aws-access-key-id ::aws-secret-access-key ]
                                        :opt-un [::u18-04]))

(def cloud-spec-resolved nil)

(s/defn k8s-convention-configuration :- k8s-convention/k8sConventionResolved
  [convention-config :- CloudConventionResolved]
  {:pre [(sp/valid? cloud-convention-resolved? convention-config)]}
  (let [{:keys [cert-manager external-ip user u18-04]} convention-config
        cluster-issuer (name cert-manager)]
    (if u18-04
      {:user user
       :k8s {:external-ip external-ip
             :u18-04 true}
       :cert-manager cert-manager}
      {:user user
       :k8s {:external-ip external-ip}
       :cert-manager cert-manager})))


(s/defn ^:always-validate
  infra-configuration :- InfraResult
  [convention-config :- CloudConventionResolved]
  (let [{:keys [cert-manager fqdn user db-user-password admin-user admin-password storage-size 
                restic-repository aws-access-key-id aws-secret-access-key restic-password]} convention-config
        cluster-issuer (name cert-manager)
        db-user-name "cloud"]
    {infra/facility
      {:user user
       :backup {:restic-repository restic-repository
                :aws-access-key-id aws-access-key-id
                :aws-secret-access-key aws-secret-access-key
                :restic-password restic-password}
       :cloud {:fqdn fqdn
               :secret-name (str/replace fqdn #"\." "-")
               :cluster-issuer cluster-issuer
               :db-name "cloud"
               :db-user-password db-user-password
               :db-user-name db-user-name
               :admin-user admin-user
               :admin-password admin-password
               :storage-size (str storage-size)}
       :postgres {:db-user-password db-user-password
                  :db-user-name db-user-name}}}))

