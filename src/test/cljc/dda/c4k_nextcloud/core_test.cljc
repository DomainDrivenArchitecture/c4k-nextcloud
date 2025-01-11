(ns dda.c4k-nextcloud.core-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [clojure.spec.alpha :as s]
   [dda.c4k-common.yaml :as yaml]
   [dda.c4k-nextcloud.core :as cut]
   #?(:cljs [dda.c4k-common.macros :refer-macros [inline-resources]])))


#?(:cljs
   (defmethod yaml/load-resource :nextcloud-test [resource-name]
     (get (inline-resources "nextcloud-test") resource-name)))

(deftest validate-valid-resources
  (is (s/valid? cut/config? (yaml/load-as-edn "nextcloud-test/valid-config.yaml")))
  (is (s/valid? cut/auth? (yaml/load-as-edn "nextcloud-test/valid-auth.yaml")))
  (is (not (s/valid? cut/config? (yaml/load-as-edn "nextcloud-test/invalid-config.yaml"))))
  (is (not (s/valid? cut/auth? (yaml/load-as-edn "nextcloud-test/invalid-auth.yaml")))))
