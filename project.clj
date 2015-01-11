(defproject smeagol "0.4.0-SNAPSHOT"
  :description "A simple Git-backed Wiki inspired by Gollum"
  :url "https://github.com/simon-brooke/smeagol"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.memoize "0.5.6"]
                 [lib-noir "0.9.4" :exclusions [org.clojure/tools.reader]]
                 [ring-server "0.3.1"]
                 [selmer "0.7.2"]
                 [com.taoensso/timbre "3.3.1" :exclusions [org.clojure/tools.reader]]
                 [com.taoensso/tower "3.0.2" :exclusions [com.taoensso/encore]]
                 [markdown-clj "0.9.55" :exclusions [com.keminglabs/cljx]]
                 [clj-jgit "0.8.2"]
                 [environ "1.0.0"]
                 [im.chit/cronj "1.4.2"]
                 [noir-exception "0.2.2"]
                 [prone "0.6.0"]]

  :repl-options {:init-ns smeagol.repl}
  :jvm-opts ["-server"]
  :plugins [[lein-ring "0.8.13" :exclusions [org.clojure/clojure]]
            [lein-environ "1.0.0"]
            [lein-ancient "0.5.5" :exclusions [org.clojure/clojure org.clojure/data.xml]]
            [lein-marginalia "0.7.1" :exclusions [org.clojure/clojure]]]
  :ring {:handler smeagol.handler/app
         :init    smeagol.handler/init
         :destroy smeagol.handler/destroy}
  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
             :aot :all}
   :production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.1"]
                        [pjstadig/humane-test-output "0.6.0"]]
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true}}}
  :min-lein-version "2.0.0")
