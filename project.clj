(defproject smeagol "0.5.0-rc1"
  :description "A simple Git-backed Wiki inspired by Gollum"
  :url "https://github.com/simon-brooke/smeagol"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.memoize "0.5.9"]
                 [com.taoensso/encore "2.80.0"]
                 [lib-noir "0.9.9" :exclusions [org.clojure/tools.reader]]
                 [com.cemerick/url "0.1.1"]
                 [ring-server "0.4.0"]
                 [selmer "1.0.7"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [com.taoensso/timbre "4.7.4" :exclusions [org.clojure/tools.reader]]
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
            [lein-bower "0.5.1"]
            [lein-ancient "0.5.5" :exclusions [org.clojure/clojure org.clojure/data.xml]]
            [lein-marginalia "0.7.1" :exclusions [org.clojure/clojure]]]
  :bower-dependencies [[simplemde "1.11.2"]]
  :ring {:handler smeagol.handler/app
         :init    smeagol.handler/init
         :destroy smeagol.handler/destroy}
  :lein-release {:scm :git :deploy-via :lein-install}
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
