(ns clj-google-vision.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-google-vision.core :refer :all])
  (:import (com.google.cloud.vision.v1 AnnotateImageResponse)
           )
  )


(defn- load-fixture
  [resource-name]
  (let [res (io/resource resource-name)]
    (read-from-file res)))


(deftest testing-read-from-file
  (testing "Reading labels test"
    (is (= 5 (.getLabelAnnotationsCount (load-fixture "air-label.pb")))))
  )


(deftest testing-parse-response
  (testing "Empty response"
    (is (= {} (parse-response (AnnotateImageResponse/getDefaultInstance)))))
  (testing "Examine label response"
    (let [label-response (load-fixture "air-label.pb")]
      (is (= 5 (count (:label_annotations (parse-response label-response)))))
      ))
  )
