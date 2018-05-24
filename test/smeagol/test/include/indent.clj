(ns smeagol.test.include.indent
  (:require [clojure.test :refer :all]
            [smeagol.include.indent :as sut]))

(deftest test-parse-heading
  (testing
    (is (= '(["# " "# " "" "# "])
           (sut/parse-heading "# h1")))
    (is (= '(["\n# " "\n# " "\n" "# "])
           (sut/parse-heading "\n# h1")))))

(deftest test-indent-heading
  (testing
    (is (= "# h1"
          (sut/do-indent-heading 0 "# h1")))
    (is (= "### h1"
          (sut/do-indent-heading 2 "# h1")))
    (is (= "\n### h1"
          (sut/do-indent-heading 2 "\n# h1")))))

(deftest test-parse-list
  (testing
    (is (= '([" * " " * " " " "* "])
           (sut/parse-list " * list")))
    (is (= '(["\n * " "\n * " "\n " "* "])
           (sut/parse-list "\n * list")))))

(deftest test-indent-list
  (testing
    (is (= " * list"
          (sut/do-indent-list 0 " * list")))
    (is (= "   * list"
          (sut/do-indent-list 2 " * list")))
    (is (= "\n   * list"
          (sut/do-indent-list 2 "\n * list")))))
