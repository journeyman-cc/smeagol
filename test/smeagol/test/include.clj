(ns smeagol.test.include
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [smeagol.include :as sut]))

(defn test-include-resolver [uri]
  (cond
    (= uri "./simple.md") "Simple content."))

(def system-under-test
  (component/system-map
    :resolver (sut/new-resolver test-include-resolver)))

(deftest test-local-links
  (testing "Rewriting of local links"
    (is (= "Simple content." (sut/resolve-include (:resolver system-under-test) "./simple.md")))))
