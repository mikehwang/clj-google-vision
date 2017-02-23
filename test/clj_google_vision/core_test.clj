(ns clj-google-vision.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-google-vision.core :refer :all]))

(defn load-fixture
  [resource-name]
  (let [res (io/resource resource-name)]
    (read-from-file res)))

(deftest testing-read-from-file
  (testing "Reading labels test"
    (is (= 5 (.getLabelAnnotationsCount (load-fixture "air-label.pb"))))))
