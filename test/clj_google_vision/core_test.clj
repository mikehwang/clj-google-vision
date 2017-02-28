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
    (read-object-from-file res)))


(deftest testing-read-object-from-file
  (testing "Reading labels test"
    (is (= 5 (.getLabelAnnotationsCount (load-fixture "air-label.pb")))))
  )

(deftest testing-write-object-to-file
  (testing "Writing labels test"
    (let [file-parent "/tmp"
          file-child "test.pb"]
      (write-object-to-file file-parent (load-fixture "air-label.pb") file-child)
      (is (< 0 (.getTotalSpace (new java.io.File file-parent file-child))))
      ))
  )


(deftest testing-parse-response
  (testing "Empty response"
    (is (= {} (parse-response (AnnotateImageResponse/getDefaultInstance)))))
  (testing "Examine label response"
    (let [label-response (load-fixture "air-label.pb")]
      (is (= 5 (count (:label_annotations (parse-response label-response)))))
      ))
  )
