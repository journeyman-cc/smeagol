(ns smeagol.test.include
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [smeagol.include.resolver :as resolver]
            [smeagol.include :as sut]))

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
      (= [{:uri "./simple.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-simple)))
    (is
      (= [{:uri "./simple.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-surounding-simple)))
    (is
      (= [{:uri "./with-heading.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-heading-0)))
    (is
      (= [{:uri "./with-heading-and-list.md", :indent-heading 1, :indent-list 1}]
         (sut/parse-include-md
           include-heading-list-1)))
    (is
      (= [{:uri "./with-heading-and-list.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-heading-list-0)))
    (is
      (= [{:uri "./simple.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           include-invalid-indent)))
    (is
      (= [{:uri "./with-heading-and-list.md", :indent-heading 2, :indent-list 3}]
         (sut/parse-include-md
           include-spaced-indent)))
    (is
      (= [{:uri "./with-heading-and-list.md",
           :indent-heading 2,
           :indent-list 3}
          {:uri "./simple.md", :indent-heading 0, :indent-list 0}]
         (sut/parse-include-md
           multi)))))

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
  (testing "The whole integration of include"
    (is
      (= "# Heading"
         (sut/expand-include-md (:includer system-under-test) "# Heading")))
    (is
      (= "# Heading 1
Simple content."
         (sut/expand-include-md
           (:includer system-under-test)
           include-simple)))))
