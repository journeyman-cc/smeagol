(ns smeagol.test.formatting
  (:require [clojure.test :refer :all]
            [clojure.string :as cs]
            [smeagol.formatting :refer :all]
            [smeagol.extensions.test :refer :all]
            [smeagol.local-links :refer :all]))

(deftest test-apply-formatter
  (testing "apply-formatter"
    (let [actual (-> (apply-formatter
                       3
                       {:inclusions {}}
                       '()
                       '()
                       "test
                       ![Frost on a gate, Laurieston](content/uploads/g1.jpg)
                       ![Feathered crystals on snow surface, Taliesin](content/uploads/g2.jpg)
                       ![Feathered snow on log, Taliesin](content/uploads/g3.jpg)
                       ![Crystaline growth on seed head, Taliesin](content/uploads/g4.jpg)"
                       "test"
                       smeagol.extensions.test/process-test)
                     :inclusions
                     :inclusion-3)
          expected "<!-- The test extension has run and this is its output -->"]
      (is (= actual expected)))))

(deftest test-md->html
  (let [actual (:content (md->html
                           {:source
                            (cs/join
                              "\n"
                              ["# This is a test"
                               ""
                               "```test"
                               "![Frost on a gate, Laurieston](content/uploads/g1.jpg)"
                               "```"
                               ""
                               "This concludes the test"])} ))
        expected (str
                    "<h1 id=\"this&#95;is&#95;a&#95;test\">This is a test</h1>"
                    "<p><!-- The test extension has run and this is its output --></p>"
                    "<p>This concludes the test</p>")]
    (is (= expected actual))))
