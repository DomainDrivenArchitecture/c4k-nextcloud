(ns dda.c4k-nextcloud.uberjar
  (:gen-class)
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as cs]
   [clojure.tools.reader.edn :as edn]
   [expound.alpha :as expound]
   [dda.c4k-nextcloud.core :as core]
   [dda.c4k-nextcloud.nextcloud :as nextcloud]))

(def usage
  "usage:
  
  c4k-nextcloud {your configuraton file} {your authorization file}")

(s/def ::options (s/* #{"-h"}))
(s/def ::filename (s/and string?
                              #(not (cs/starts-with? % "-"))))
(s/def ::cmd-args (s/cat :options ::options
                         :args (s/?
                                (s/cat :config ::filename
                                       :auth ::filename))))

(defn expound-config
  [config]
  (expound/expound ::nextcloud/config config))

(defn invalid-args-msg 
  [spec args]
  (s/explain spec args)
  (println (str "Bad commandline arguments\n" usage)))

(defn -main [& cmd-args]
  (let [parsed-args-cmd (s/conform ::cmd-args cmd-args)]
    (if (= ::s/invalid parsed-args-cmd)
      (invalid-args-msg ::cmd-args cmd-args)
      (let [{:keys [options args]} parsed-args-cmd
            {:keys [config auth]} args]
          (cond
            (some #(= "-h" %) options)
            (println usage)
            :default
            (let [config-str (slurp config)
                  auth-str (slurp auth)
                  config-edn (edn/read-string config-str)
                  auth-edn (edn/read-string auth-str)
                  config-valid? (s/valid? nextcloud/config? config-edn)
                  auth-valid? (s/valid? core/auth? auth-edn)]
              (if (and config-valid? auth-valid?)
                (println (core/generate config-edn auth-edn))
                (do
                  (when (not config-valid?) 
                    (println 
                     (expound/expound-str nextcloud/config? config-edn {:print-specs? false})))
                  (when (not auth-valid?) 
                    (println 
                     (expound/expound-str core/auth? auth-edn {:print-specs? false})))))))))))
