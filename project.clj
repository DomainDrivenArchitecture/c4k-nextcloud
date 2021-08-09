(defproject meissa/meissa-cloud "1.0.2-SNAPSHOT"
  :description "Crate to install cloud"
  :url "https://meissa-gmbh.de"
  :license {:name "meissa commercial license"
            :url "https://www.meissa-gmbh.de"}
  :dependencies [[dda/dda-pallet "4.0.3"]
                 [dda/dda-k8s-crate "1.0.1"]
                 [orchestra "2021.01.01-1"]]
  :target-path "target/%s/"
  :source-paths ["main/src"]
  :resource-paths ["main/resources"]
  :repositories [["clojars" "https://clojars.org/repo"]
                 ["snapshots"
                  "https://artifact.prod.meissa-gmbh.de/repository/maven-snapshots/"]
                 ["releases"
                  "https://artifact.prod.meissa-gmbh.de/repository/maven-releases/"]]
  :deploy-repositories [["snapshots" "https://artifact.prod.meissa-gmbh.de/repository/maven-snapshots/"]
                        ["releases" "https://artifact.prod.meissa-gmbh.de/repository/maven-releases/"]]
  :profiles {:dev {:source-paths ["integration/src"
                                  "test/src"
                                  "uberjar/src"]
                   :resource-paths ["integration/resources"
                                    "test/resources"]
                   :dependencies
                   [[org.clojure/test.check "1.1.0"]
                    [dda/data-test "0.1.1"]
                    [dda/pallet "0.9.1" :classifier "tests"]
                    [ch.qos.logback/logback-classic "1.3.0-alpha5"]
                    [org.slf4j/jcl-over-slf4j "2.0.0-alpha1"]]
                   :plugins
                   [[lein-sub "0.3.0"]]
                   :leiningen/reply
                   {:dependencies [[org.slf4j/jcl-over-slf4j "1.8.0-beta2"]]
                    :exclusions [commons-logging]}}
             :test {:test-paths ["test/src"]
                    :resource-paths ["test/resources"]
                    :dependencies [[dda/pallet "0.9.1" :classifier "tests"]]}
             :uberjar {:source-paths ["uberjar/src"]
                       :resource-paths ["uberjar/resources"]
                       :aot :all
                       :main meissa.pallet.meissa-cloud.main
                       :uberjar-name "meissa-cloud-standalone.jar"
                       :dependencies [[org.clojure/tools.cli "1.0.194"]
                                      [ch.qos.logback/logback-classic "1.3.0-alpha5"]
                                      [org.slf4j/jcl-over-slf4j "2.0.0-alpha1"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["uberjar"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :local-repo-classpath true)
