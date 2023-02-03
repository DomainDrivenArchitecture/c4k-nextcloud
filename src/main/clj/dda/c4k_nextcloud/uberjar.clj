(ns dda.c4k-nextcloud.uberjar
  (:gen-class)
  (:require
   [dda.c4k-common.uberjar :as uberjar]
   [dda.c4k-nextcloud.nextcloud :as nextcloud]
   [dda.c4k-nextcloud.core :as core]))

(defn -main [& cmd-args]
  (uberjar/main-common
   "c4k-nextcloud"
   nextcloud/config?
   nextcloud/auth?
   core/config-defaults
   core/k8s-objects
   cmd-args))
