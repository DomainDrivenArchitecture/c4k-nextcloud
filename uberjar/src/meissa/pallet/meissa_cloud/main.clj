(ns meissa.pallet.meissa-cloud.main
  (:gen-class)
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]
   [dda.pallet.core.main-helper :as mh]
   [dda.pallet.core.app :as core-app]
   [meissa.pallet.meissa-cloud.app :as app]))

(def cli-options
  [["-h" "--help"]
   ["-c" "--configure"]
   ["-t" "--targets example-targets.edn" "edn file containing the targets to install on."
    :default "localhost-target.edn"]
   ["-v" "--verbose"]])

(defn usage [options-summary]
  (str/join
   \newline
   ["meissa-cloud installs & configures a single host kubernetes cluster with Cloud installed"
    ""
    "Usage: java -jar meissa-cloud-standalone.jar [options] cloud.edn"
    ""
    "Options:"
    options-summary
    ""
    "cloud.edn"
    "  - follows the edn format."
    "  - has to be a valid CloudConventionConfig"
    ""]))

(defn -main [& args]
  (let [{:keys [options arguments errors summary help]} (cli/parse-opts args cli-options)
        verbose (if (contains? options :verbose) 1 0)]
    (cond
      help (mh/exit 0 (usage summary))
      errors (mh/exit 1 (mh/error-msg errors))
      (not= (count arguments) 1) (mh/exit 1 (usage summary))
      (:serverspec options) (if (core-app/existing-serverspec
                                 app/crate-app
                                 {:convention (first arguments)
                                  :targets (:targets options)
                                  :verbosity verbose})
                              (mh/exit-test-passed)
                              (mh/exit-test-failed))
      (:configure options) (if (core-app/existing-configure
                                app/crate-app
                                {:convention (first arguments)
                                 :targets (:targets options)})
                             (mh/exit-default-success)
                             (mh/exit-default-error))
      :default (if (core-app/existing-install
                    app/crate-app
                    {:convention (first arguments)
                     :targets (:targets options)})
                 (mh/exit-default-success)
                 (mh/exit-default-error)))))
