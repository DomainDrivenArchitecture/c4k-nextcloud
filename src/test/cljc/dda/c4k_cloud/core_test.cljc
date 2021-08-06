(ns dda.c4k-cloud.core-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-cloud.core :as cut]))

(deftest should-k8s-objects
  (is (= 16
         (count (cut/k8s-objects {:fqdn "cloud-neu.prod.meissa-gmbh.de"
                                  :postgres-db-user "cloud"
                                  :postgres-db-password "cloud-db-password"
                                  :issuer :prod
                                  :cloud-data-volume-path "/var/cloud"
                                  :postgres-data-volume-path "/var/postgres"
                                  :aws-access-key-id "aws-id"
                                  :aws-secret-access-key "aws-secret"
                                  :restic-password "restic-pw"
                                  :restic-repository "restic-repository"}))))
  (is (= 14
         (count (cut/k8s-objects {:fqdn "cloud-neu.prod.meissa-gmbh.de"
                                  :postgres-db-user "cloud"
                                  :postgres-db-password "cloud-db-password"
                                  :issuer :prod
                                  :aws-access-key-id "aws-id"
                                  :aws-secret-access-key "aws-secret"
                                  :restic-password "restic-pw"
                                  :restic-repository "restic-repository"}))))
  (is (= 11
         (count (cut/k8s-objects {:fqdn "cloud-neu.prod.meissa-gmbh.de"
                                  :postgres-db-user "cloud"
                                  :postgres-db-password "cloud-db-password"
                                  :issuer :prod
                                  :aws-access-key-id "aws-id"
                                  :aws-secret-access-key "aws-secret"
                                  :restic-password "restic-pw"})))))
