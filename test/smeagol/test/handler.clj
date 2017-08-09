(ns smeagol.test.handler
  (:require [clojure.test :refer :all]
        [ring.mock.request :refer :all]
        [smeagol.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/" {:accept-language "en-GB"}))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid" {:accept-language "en-GB"}))]
      (is (= 404 (:status response))))))
