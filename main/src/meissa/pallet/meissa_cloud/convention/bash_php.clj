(ns meissa.pallet.meissa-cloud.convention.bash-php
  (:require
   [clojure.spec.alpha :as s]
   [meissa.pallet.meissa-cloud.convention.bash :as bash]))

(defn bash-php-env-string? 
  [input]
  (and (bash/bash-env-string? input)
       (not (re-matches #".*[\-\\\\]+.*" input))))

(s/def ::plain bash-php-env-string?)
