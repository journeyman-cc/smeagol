(ns smeagol.test.include
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [com.stuartsierra.component :as component]
            [smeagol.include.resolve :as resolve]
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

(s/defmethod resolve/do-resolve-md :test-mock
  [resolver
   uri :- s/Str]
  (cond
    (= uri "./simple.md") "Simple content."))

(def system-under-test
  (component/start
    (component/system-map
      :resolver (resolve/new-resolver :test-mock)
      :includer (component/using
                  (sut/new-includer)
                  [:resolver]))))

(deftest test-expand-include-md
  (testing "The whole integration of include"
    (is
      (= "# Heading"
         (sut/expand-include-md (:includer system-under-test) "# Heading")))
    (is
      (= "# Heading1
Simple content."
         (sut/expand-include-md
           (:includer system-under-test)
           include-simple)))))
