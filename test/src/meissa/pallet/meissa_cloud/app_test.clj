(ns meissa.pallet.meissa-nextcloud.app-test
  (:require
   [clojure.test :refer :all]
   [schema.core :as s]
   [meissa.pallet.meissa-nextcloud.app :as sut]))

(s/set-fn-validation! true)

(s/def test-convention-conf
  {:user :k8s
   :external-ip "12.121.111.121"
   :fqdn "some.domain.de"
   :cert-manager :letsencrypt-staging-issuer
   :db-user-password "test1234"
   :admin-user "root"
   :admin-password "test1234"
   :storage-size 50
   :restic-repository "nextcloud"
   :aws-access-key-id "10"
   :aws-secret-access-key "secret"
   :restic-password "test4321"})

(deftest app-config
  (testing
   "test plan-def"
    (is (map? (sut/app-configuration-resolved test-convention-conf)))))

(deftest plan-def
  (testing
   "test plan-def"
    (is (map? sut/with-nextcloud))))
