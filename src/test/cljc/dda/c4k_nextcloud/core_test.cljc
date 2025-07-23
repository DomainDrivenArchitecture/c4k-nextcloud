(ns dda.c4k-nextcloud.core-test
  (:require
   [clojure.test :refer [deftest is are testing run-tests]]
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-nextcloud.core :as cut]))

(deftest validate-valid-resources
  (is (s/valid? cut/config? (yaml/load-as-edn "nextcloud-test/valid-config.yaml")))
  (is (s/valid? cut/auth? (yaml/load-as-edn "nextcloud-test/valid-auth.yaml")))
  (is (not (s/valid? cut/config? (yaml/load-as-edn "nextcloud-test/invalid-config.yaml"))))
  (is (not (s/valid? cut/auth? (yaml/load-as-edn "nextcloud-test/invalid-auth.yaml")))))
