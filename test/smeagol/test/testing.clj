(ns smeagol.test.testing
  (:require [clojure.test :refer :all]
            [smeagol.testing :refer [parse do-test]]))

(deftest test-inalid-input
  (are [match input] (re-find match (-> input parse :error))
    #"at least 3 lines" ""
    #"No test found with name" "wtf\r\n1\r\n2"
    #"Failed parsing line.*EOF while reading" "smeagol.sample/pow\r\n(\r\n2"))

(deftest test-executon
  (are [result input] (= result (-> input parse do-test))
    {:result :failure, :expected 15, :actual 16} " smeagol.sample/pow\r\n4\r\n15\r\n"
    {:result :ok} " smeagol.sample/pow\r\n4\r\n16\r\n"))

