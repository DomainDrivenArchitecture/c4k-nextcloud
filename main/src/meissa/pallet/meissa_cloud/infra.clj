(ns meissa.pallet.meissa-cloud.infra
  (:require
   [schema.core :as s]
   [dda.pallet.core.infra :as core-infra]
   [meissa.pallet.meissa-cloud.infra.backup :as backup]
   [meissa.pallet.meissa-cloud.infra.cloud :as cloud]
   [meissa.pallet.meissa-cloud.infra.postgres :as postgres]))

(def facility :meissa-cloud)

(def MeissaCloudInfra 
  (merge 
   {:user s/Keyword}
    backup/MeissaBackupInfra
    cloud/MeissaCloudInfra
    postgres/MeissaPostgresInfra))

(s/defmethod core-infra/dda-init facility
  [dda-crate config]
  (let [facility (:facility dda-crate)
        {:keys [user backup postgres cloud]} config
        user-str (name user)]
    (postgres/init facility user-str postgres)
    (cloud/init facility user-str cloud)
    (backup/init facility user-str backup)))

(s/defmethod core-infra/dda-install facility
  [dda-crate config]
  (let [facility (:facility dda-crate)
        {:keys [user backup postgres cloud]} config
        user-str (name user)]
    (postgres/install facility user-str postgres)
    (cloud/install facility user-str cloud)
    (backup/install facility user-str backup)))

(s/defmethod core-infra/dda-configure facility
  [dda-crate config]
  (let [facility (:facility dda-crate)
        {:keys [user backup postgres cloud]} config
        user-str (name user)]
    (postgres/configure facility user-str postgres)
    (cloud/configure facility user-str cloud)
    (backup/configure facility user-str backup)))

(def meissa-cloud
  (core-infra/make-dda-crate-infra
   :facility facility
   :infra-schema MeissaCloudInfra))

(def with-cloud
  (core-infra/create-infra-plan meissa-cloud))
