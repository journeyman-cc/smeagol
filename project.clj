(defproject smeagol "0.5.1-SNAPSHOT"
  :description "A simple Git-backed Wiki inspired by Gollum"
  :url "https://github.com/simon-brooke/smeagol"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.memoize "0.5.9"]
                 [lib-noir "0.9.9" :exclusions [org.clojure/tools.reader]]
                 [com.cemerick/url "0.1.1"]
                 [ring-server "0.4.0"]
                 [selmer "1.0.7"]
                 [com.taoensso/timbre "3.3.1" :exclusions [org.clojure/tools.reader]]
                 [com.taoensso/tower "3.0.2" :exclusions [com.taoensso/encore]]
                 [markdown-clj "0.9.89" :exclusions [com.keminglabs/cljx]]
                 [crypto-password "0.2.0"]
                 [clj-jgit "0.8.9"]
                 [environ "1.1.0"]
                 [im.chit/cronj "1.4.4"]
                 [noir-exception "0.2.5"]
                 [prone "1.1.1"]]

  :repl-options {:init-ns smeagol.repl}
  :jvm-opts ["-server"]
  :plugins [[lein-ring "0.8.13" :exclusions [org.clojure/clojure]]
            [lein-environ "1.0.0"]
            [lein-ancient "0.5.5" :exclusions [org.clojure/clojure org.clojure/data.xml]]
            [lein-marginalia "0.7.1" :exclusions [org.clojure/clojure]]]
  :ring {:handler smeagol.handler/app
         :init    smeagol.handler/init
         :destroy smeagol.handler/destroy}
  :lein-release {:scm :git}
  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
             :aot :all}
   :production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.5.0"]
                        [pjstadig/humane-test-output "0.8.1"]]
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true}}}
  :min-lein-version "2.0.0")
