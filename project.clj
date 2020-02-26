(defproject smeagol "1.0.5-SNAPSHOT"
  :description "A simple Git-backed Wiki inspired by Gollum"
  :url "https://github.com/simon-brooke/smeagol"
  :license {:name "GNU General Public License,version 2.0 or (at your option) any later version"
            :url "https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html"}
  :dependencies [[clj-jgit "0.8.10"]
                 [clj-yaml "0.4.0"]
                 [clojure.java-time "0.3.2"]
                 [com.cemerick/url "0.1.1"]
                 [com.fzakaria/slf4j-timbre "0.3.7"]
                 [com.stuartsierra/component "0.4.0"]
                 [com.taoensso/encore "2.92.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.taoensso/tower "3.0.2" :exclusions [com.taoensso/encore]]
                 [crypto-password "0.2.0"]
                 [environ "1.1.0"]
                 [hiccup "1.0.5"]
                 [im.chit/cronj "1.4.4"]
                 [image-resizer "0.1.10"]
                 [instaparse "1.4.10"]
                 [lib-noir "0.9.9" :exclusions [org.clojure/tools.reader]]
                 [markdown-clj "0.9.99" :exclusions [com.keminglabs/cljx]]
                 [me.raynes/fs "1.4.6"]
                 [noir-exception "0.2.5"]
                 [org.clojars.simon_brooke/internationalisation "1.0.3"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/core.memoize "0.5.9"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.clojure/tools.trace "0.7.10"]
                 [org.slf4j/slf4j-api "1.7.25"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]
                 [org.slf4j/jul-to-slf4j "1.7.25"]
                 [org.slf4j/jcl-over-slf4j "1.7.25"]
                 [prismatic/schema "1.1.9"]
                 [prone "1.1.4"]
                 [ring/ring-anti-forgery "1.1.0"]
                 [ring-server "0.4.0"]
                 [selmer "1.11.0"]]

  :repl-options {:init-ns smeagol.repl}

  :jvm-opts ["-server"]

  :plugins [[lein-ancient "0.5.5" :exclusions [org.clojure/clojure org.clojure/data.xml]]
            [lein-codox "0.10.3"]
            [io.sarnowski/lein-docker "1.0.0"]
            [lein-environ "1.0.0"]
            [lein-kibit "0.1.6"]
            [lein-marginalia "0.7.1" :exclusions [org.clojure/clojure]]
            [lein-npm "0.6.2"]
            [lein-ring "0.12.5" :exclusions [org.clojure/clojure]]]

  :npm {:dependencies [[simplemde "1.11.2"]
                       [vega "5.8.0"]
                       [vega-embed "6.2.2"]
                       [vega-lite "4.1.1"]
                       [mermaid "8.4.6"]
                       [photoswipe "4.1.3"]
                       [showdown "1.9.1"]
                       [tablesort "5.2.0"]
                       [geocsv-js "simon-brooke/geocsv-js#3a34ba7"]]
        :root "resources/public/vendor"}

  :docker {:image-name "simonbrooke/smeagol"
           :dockerfile "Dockerfile"}

  :ring {:handler smeagol.handler/app
         :init    smeagol.handler/init
         :destroy smeagol.handler/destroy}

  ;; for the time being, I'm not sure that I want to formally deploy this anywhere, and I certainly don't feel
  ;; it's fair to clutter clojars.org with it.
  :deploy-repositories [["releases" "file:/tmp"]
                        ["snapshots" "file:/tmp"]]

  :release-tasks [["vcs" "assert-committed"]
                  ["clean"]
                  ["codox"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["bower" "install"]
                  ["ring" "uberjar"]
                  ["deploy"]
                  ["docker" "build"]
                  ["docker" "push"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]]

  :profiles {:uberjar {:omit-source true
                       :env {:production true}
                       :aot :all}
             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}}
             :dev {:dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.6.2"]
                                  [pjstadig/humane-test-output "0.8.2"]]
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]
                   :env {:dev true}}}

  :min-lein-version "2.0.0")
