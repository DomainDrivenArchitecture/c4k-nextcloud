(ns meissa.pallet.meissa-cloud.convention.bash-test
  (:require
   [clojure.test :refer :all]
   [meissa.pallet.meissa-cloud.convention.bash :as sut]))


(deftest test-it
  (is (= false 
         (sut/bash-env-string? 4)))
  (is (= false
         (sut/bash-env-string? "1$0")))
  (is (= false
         (sut/bash-env-string? "'hallo")))
  (is (= false
         (sut/bash-env-string? "hallo\"")))
   (is (= false
          (sut/bash-env-string? "hall$o")))
  (is (= true
         (sut/bash-env-string? "test")))
  (is (= true
         (sut/bash-env-string? "test123")))
  )