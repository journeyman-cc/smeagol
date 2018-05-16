(ns smeagol.test.include.resolver
  (:require [clojure.test :refer :all]
            [smeagol.include.resolver :as sut]))

(deftest test-local-links
  (testing "Rewriting of local links"
    (is (thrown? Exception
          (sut/resolve-md (sut/new-resolver (:default)) "./some-uri.md")))))
