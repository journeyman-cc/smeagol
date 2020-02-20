(ns smeagol.test.local-links
  (:require [clojure.test :refer :all]
            [clojure.string :as cs]
            [smeagol.extensions.test :refer :all]
            [smeagol.local-links :refer :all]))

(deftest test-local-links
  (testing "Rewriting of local links"
    (is (= (local-links nil) no-text-error) "Should NOT fail with a no pointer exception!")
    (is (= (local-links "") "") "Empty string should pass through unchanged.")
    (is (= (local-links "[[froboz]]") "<a href='wiki?page=froboz'>froboz</a>") "Local link should be rewritten.")
    (let [text (str "# This is a heading"
                    "[This is a foreign link](http://to.somewhere)")]
      (is (= (local-links text) text) "Foreign links should be unchanged"))
    (let [text (cs/trim (slurp "resources/test/test_local_links.md"))
          actual (local-links text)
          expected "# This is a test\n\n<a href='wiki?page=Local%20link'>Local link</a>\n[Not a local link](http://nowhere.at.al)\n\nThis concludes the test."]
      (is (= actual expected)))))
