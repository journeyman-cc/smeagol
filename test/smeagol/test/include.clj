(ns smeagol.test.include
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [smeagol.include.resolver :as resolver]
            [smeagol.include :as sut]))

(s/defmethod resolver/do-resolve-md :test-mock
  [resolver
   uri :- s/Str]
  (cond
    (= uri "./simple.md") "Simple content."))

(def system-under-test
  (component/system-map
    :resolver (resolver/new-resolver :test-mock)
    :includer (component/using
                (sut/new-includer)
                {:resolver :resolver})))

(deftest test-expand-include-md
  (testing "Rewriting of local links"
    (is
      (= "# Heading"
         (sut/expand-include-md (:includer system-under-test) "# Heading")))
    (is
      (= "# Heading 1
          Simple content."
         (sut/expand-include-md
           (:includer system-under-test)
           "# Heading1
#[](.simple.md)")))))
