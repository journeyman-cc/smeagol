(ns smeagol.test.include.resolve
  (:require [clojure.test :refer :all]
            [smeagol.include.resolve :as sut]))

(deftest test-local-links
  (testing "Rewriting of local links"
    (is (thrown? Exception
          (sut/resolve-md (sut/new-resolver (:default)) "./some-uri.md")))))
