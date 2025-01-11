(ns dda.c4k-nextcloud.uberjar
  (:gen-class)
  (:require
   [dda.c4k-common.uberjar :as uberjar]
   [dda.c4k-nextcloud.core :as core]))

(defn -main [& cmd-args]
  (uberjar/main-cm
   "c4k-nextcloud"
   core/config?
   core/auth?
   core/config-defaults
   core/config-objects
   core/auth-objects
   cmd-args))
