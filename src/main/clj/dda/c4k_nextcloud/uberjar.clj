(ns dda.c4k-nextcloud.uberjar
  (:gen-class)
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as cs]
   [clojure.tools.reader.edn :as edn]
   [expound.alpha :as expound]
   [dda.c4k-common.yaml :as yaml]
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
                  config-parse-fn (if (yaml/is-yaml? config) yaml/from-string edn/read-string)
                  auth-parse-fn (if (yaml/is-yaml? auth) yaml/from-string edn/read-string)
                  parsed-config (config-parse-fn config-str)
                  parsed-auth (auth-parse-fn auth-str)
                  config-valid? (s/valid? nextcloud/config? parsed-config)
                  auth-valid? (s/valid? core/auth? parsed-auth)]
              (if (and config-valid? auth-valid?)
                (println (core/generate parsed-config parsed-auth))
                (do
                  (when (not config-valid?) 
                    (println 
                     (expound/expound-str nextcloud/config? parsed-config {:print-specs? false})))
                  (when (not auth-valid?) 
                    (println 
                     (expound/expound-str core/auth? parsed-auth {:print-specs? false})))))))))))
