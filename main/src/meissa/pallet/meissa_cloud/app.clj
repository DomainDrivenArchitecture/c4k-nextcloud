(ns meissa.pallet.meissa-cloud.app
  (:require
   [schema.core :as s]
   [dda.pallet.commons.secret :as secret]
   [dda.config.commons.map-utils :as mu]
   [dda.pallet.core.app :as core-app]
   [dda.pallet.dda-config-crate.infra :as config-crate]
   [dda.pallet.dda-user-crate.app :as user]
   [dda.pallet.dda-k8s-crate.app :as k8s]
   [meissa.pallet.meissa-cloud.convention :as convention]
   [meissa.pallet.meissa-cloud.infra :as infra]))

(def with-cloud infra/with-cloud)

(def CloudConvention convention/CloudConvention)

(def CloudConventionResolved convention/CloudConventionResolved)

(def InfraResult convention/InfraResult)

(def CloudApp
  {:group-specific-config
   {s/Keyword (merge InfraResult
                     user/InfraResult
                     k8s/InfraResult)}})

(s/defn ^:always-validate
  app-configuration-resolved :- CloudApp
  [resolved-convention-config :- CloudConventionResolved
   & options]
  (let [{:keys [group-key] :or {group-key infra/facility}} options]
    (mu/deep-merge
      (k8s/app-configuration-resolved
       (convention/k8s-convention-configuration resolved-convention-config) :group-key group-key)
      {:group-specific-config
       {group-key
        (convention/infra-configuration resolved-convention-config)}})))

(s/defn ^:always-validate
  app-configuration :- CloudApp
  [convention-config :- CloudConvention
   & options]
  (let [resolved-convention-config (secret/resolve-secrets convention-config CloudConvention)]
    (apply app-configuration-resolved resolved-convention-config options)))

(s/defmethod ^:always-validate
  core-app/group-spec infra/facility
  [crate-app
   convention-config :- CloudConventionResolved]
  (let [app-config (app-configuration-resolved convention-config)]
    (core-app/pallet-group-spec
     app-config [(config-crate/with-config app-config)
                 user/with-user
                 k8s/with-k8s
                 with-cloud])))

(def crate-app (core-app/make-dda-crate-app
                :facility infra/facility
                :convention-schema CloudConvention
                :convention-schema-resolved CloudConventionResolved
                :default-convention-file "cloud.edn"))
