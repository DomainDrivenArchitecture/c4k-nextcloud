(ns meissa.pallet.meissa-cloud.convention-test
  (:require
   [clojure.test :refer :all]
   [data-test :refer :all]
   [meissa.pallet.meissa-cloud.convention :as sut]
   [clojure.spec.alpha :as sp]))

(defdatatest should-generate-infra-for-convention [input expected]
  (is (= expected
         (sut/infra-configuration input))))

(defdatatest should-generate-k8s-convention [input expected]
  (is (= expected
         (sut/k8s-convention-configuration input))))

(defdatatest should-validate-input [input expected]
  (is (= expected 
         (sp/valid? sut/cloud-convention-resolved? input))))
