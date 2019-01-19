(ns smeagol.test.include.parse
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [smeagol.include.parse :as sut]))

(def include-simple
  "# Heading1
&[](./simple.md)")

(def include-surounding-simple
  "# Heading1
Some surounding &[](./simple.md) text")

(def include-heading-0
  "# Heading1
&[:indent-heading 0](./with-heading.md)")

(def include-heading-list-1
  "# Heading1
&[:indent-heading 1 :indent-list 1](./with-heading-and-list.md)")

(def include-heading-list-0
  "# Heading1
&[:indent-list 0 :indent-heading 0](./with-heading-and-list.md)")

(def include-invalid-indent
  "# Heading1
&[ invalid input should default to indent 0 ](./simple.md)")

(def include-spaced-indent
  "# Heading1
&[ :indent-heading 2   :indent-list 33  ](./with-heading-and-list.md)")

(def multi
  "# Heading1
&[ :indent-heading 2   :indent-list 33  ](./with-heading-and-list.md)
some text
&[](./simple.md)
more text.")


(deftest test-parse-include-md
  (testing "parse include links"
    (is
      (= []
         (sut/parse-include-md "# Heading")))
    (is
      (= [{:replace "&[](./simple.md)" :uri "./simple.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-simple)))
    (is
      (= [{:replace "&[](./simple.md)" :uri "./simple.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-surounding-simple)))
    (is
      (= [{:replace "&[:indent-heading 0](./with-heading.md)" :uri "./with-heading.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-heading-0)))
    (is
      (= [{:replace
            "&[:indent-heading 1 :indent-list 1](./with-heading-and-list.md)"
           :uri "./with-heading-and-list.md", :indent-heading 1, :indent-list 1}]
         (sut/parse-include-md
           include-heading-list-1)))
    (is
      (= [{:replace
            "&[:indent-list 0 :indent-heading 0](./with-heading-and-list.md)"
           :uri "./with-heading-and-list.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-heading-list-0)))
    (is
      (= [{:replace
            "&[ invalid input should default to indent 0 ](./simple.md)"
           :uri "./simple.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-invalid-indent)))
    (is
      (= [{:replace
            "&[ :indent-heading 2   :indent-list 33  ](./with-heading-and-list.md)"
           :uri "./with-heading-and-list.md", :indent-heading 2, :indent-list 3}]
         (sut/parse-include-md
           include-spaced-indent)))
    (is
      (= [{:replace
            "&[ :indent-heading 2   :indent-list 33  ](./with-heading-and-list.md)"
           :uri "./with-heading-and-list.md",
           :indent-heading 2,
           :indent-list 3}
          {:replace "&[](./simple.md)" :uri "./simple.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           multi)))))
