(ns meissa.pallet.meissa-cloud.convention.bash-php-test
  (:require
   [clojure.test :refer :all]
   [meissa.pallet.meissa-cloud.convention.bash-php :as sut]))


(deftest test-it
  (is (= false 
         (sut/bash-php-env-string? 4)))
  (is (= false
         (sut/bash-php-env-string? "hal-lo")))
  (is (= false
         (sut/bash-php-env-string? "hal--lo")))
  (is (= false
         (sut/bash-php-env-string? "hal\\lo")))
  (is (= true
         (sut/bash-php-env-string? "test")))
  (is (= true
         (sut/bash-php-env-string? "test123")))
  )