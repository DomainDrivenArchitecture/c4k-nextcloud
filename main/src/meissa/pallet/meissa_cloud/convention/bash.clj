(ns meissa.pallet.meissa-cloud.convention.bash
  (:require
   [clojure.spec.alpha :as s]))

(defn bash-env-string?
  [input]
  (and (string? input)
       (not (re-matches #".*['\"\$]+.*" input))))

(s/def ::plain bash-env-string?)
