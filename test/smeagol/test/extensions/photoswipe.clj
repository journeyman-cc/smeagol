(ns smeagol.test.extensions.photoswipe
  (:require [clojure.test :refer :all]
            [clojure.string :as cs]
            [smeagol.extensions.photoswipe :refer :all]))


(deftest simple-syntax-tests
  (testing "Process simple slide"
    (let [expected {:title "Frost on a gate, Laurieston",
                    :src "content/uploads/g1.jpg",
                    :w 2592,
                    :h 1944
                    :msrc "content/uploads/med/g1.jpg"}
          actual (process-simple-slide
                   '([:title "Frost on a gate, Laurieston"]
                     [:src "content/uploads/g1.jpg"]))]
      (is (= actual expected))))
  (testing "Find thumbnail"
    (let [expected "content/uploads/med/g1.jpg"
          actual (find-thumb "content/uploads/g1.jpg" :med)]
      (is (= actual expected) "`resources/content/uploads/med/g1.jpg` is in
          the repository, so should be found"))
    (let [expected nil
          actual (find-thumb "passwd" :med)]
      (is (= actual expected) "`resources/passwd` is in
          the repository, but is not uploaded so should NOT be found")))
  (testing "Merging image dimensions"
    (let [expected {:title "Frost on a gate, Laurieston", :src "content/uploads/g1.jpg", :w 2592, :h 1944}
          actual (slide-merge-dimensions {:title "Frost on a gate, Laurieston",
                                          :src "content/uploads/g1.jpg"})]
      (is (= actual expected))))
  (testing "Simple slide grammar"
    (let [expected '(([:title "Frost on a gate, Laurieston"] [:src "content/uploads/g1.jpg"]))
          actual (simplify
                   (simple-grammar
                     "![Frost on a gate, Laurieston](content/uploads/g1.jpg)"))]
      (is (= actual expected) "Valid syntax, should parse")
      (is (empty? (simplify (simple-grammar "[Fred](fred.jpg)")))
          "Invalid syntax (no leading `!`), should not parse."))
    (let  [expected '(([:title "Frost on a gate, Laurieston"] [:src "content/uploads/g1.jpg"])
                      ([:title "Feathered crystals on snow surface, Taliesin"] [:src "content/uploads/g2.jpg"])
                      ([:title "Feathered snow on log, Taliesin"] [:src "content/uploads/g3.jpg"])
                      ([:title "Crystaline growth on seed head, Taliesin"] [:src "content/uploads/g4.jpg"]))
           actual (simplify
                    (simple-grammar
                      "![Frost on a gate, Laurieston](content/uploads/g1.jpg)
                      ![Feathered crystals on snow surface, Taliesin](content/uploads/g2.jpg)
                      ![Feathered snow on log, Taliesin](content/uploads/g3.jpg)
                      ![Crystaline growth on seed head, Taliesin](content/uploads/g4.jpg)"))]
      (is (= actual expected) "Valid syntax, should parse"))))
