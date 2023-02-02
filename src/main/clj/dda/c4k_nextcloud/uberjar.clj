(ns dda.c4k-nextcloud.uberjar
  (:gen-class)
  (:require
   [dda.c4k-common.uberjar :as uberjar]
   [dda.c4k-nextcloud.jitsi :as jitsi]
   [dda.c4k-nextcloud.core :as core]))

(defn -main [& cmd-args]
  (uberjar/main-common
   "c4k-nextcloud"
   jitsi/config?
   jitsi/auth?
   core/config-defaults
   core/generate
   cmd-args))
