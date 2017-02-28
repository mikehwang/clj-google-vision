(ns clj-google-vision.core
  (:require [taoensso.timbre :as timbre :refer [info error]]
            [clojure.java.io :as io]
            [flatland.protobuf.core :refer [protobuf-load]]
            )
  (:import (com.google.cloud.vision.spi.v1 ImageAnnotatorClient)
           (com.google.protobuf ByteString)
           (com.google.protobuf CodedOutputStream)
           (java.io FileInputStream)
           (com.google.cloud.vision.v1 AnnotateImageRequest)
           (com.google.cloud.vision.v1 AnnotateImageResponse)
           (com.google.cloud.vision.v1 BatchAnnotateImagesResponse)
           (com.google.cloud.vision.v1 Image)
           (com.google.cloud.vision.v1 Feature)
           (flatland.protobuf PersistentProtocolBufferMap$Def)
           )
  )


; https://github.com/GoogleCloudPlatform/java-docs-samples/blob/master/vision/cloud-client/src/main/java/com/example/vision/Detect.java#L178
(defn- detect
  [feature-type photo-path]
  (let [client (ImageAnnotatorClient/create)
        photo-in-bytes (ByteString/readFrom (new FileInputStream photo-path))
        image (.build (.setContent (Image/newBuilder) photo-in-bytes))
        feature (.build (.setType (Feature/newBuilder) feature-type))

        request (.addFeatures (AnnotateImageRequest/newBuilder) feature)
        request (.setImage request image)
        request (.build request)

        response (.batchAnnotateImages client [request])
        responses (.getResponsesList response)
        ]
    responses
    ))

(defn- wrap-detect-safely
  [target-func]
  (fn [feature-type photo-path]
    (try
      (info "Detecting: " photo-path)
      (target-func feature-type photo-path)
      (catch Exception e
        (error "Error detecting: " photo-path ", " (.getMessage e))
        ))))


(def wrapped-detect (wrap-detect-safely detect))

(def detect-face-detection (partial wrapped-detect
                                    com.google.cloud.vision.v1.Feature$Type/FACE_DETECTION))
(def detect-landmark (partial wrapped-detect
                              com.google.cloud.vision.v1.Feature$Type/LANDMARK_DETECTION))
(def detect-labels (partial wrapped-detect com.google.cloud.vision.v1.Feature$Type/LABEL_DETECTION))
(def detect-text (partial wrapped-detect
                          com.google.cloud.vision.v1.Feature$Type/TEXT_DETECTION))
(def detect-image-properties (partial wrapped-detect
                                      com.google.cloud.vision.v1.Feature$Type/IMAGE_PROPERTIES))


(defn parse-response
  "Make AnnotateImageResponse object into Clojure map"
  [response-object]
  (with-open [xout (new java.io.ByteArrayOutputStream)]
    (let [cos (CodedOutputStream/newInstance xout)]
      (.writeTo response-object cos)
      (.flush cos)
      )

    (let [protobuf-size (.size xout)
          protobuf-def (PersistentProtocolBufferMap$Def/create
                         (AnnotateImageResponse/getDescriptor)
                         PersistentProtocolBufferMap$Def/protobufNames
                         protobuf-size)
          raw-bytes (.toByteArray xout)
          ]
      (protobuf-load protobuf-def raw-bytes)
      )
    ))


(defn read-object-from-file
  "Read AnnotateImageResponse from file"
  [path]
  (with-open [xin (io/input-stream path)]
    (AnnotateImageResponse/parseFrom xin)))

(defn write-object-to-file
  "Write AnnotateImageResponse to file"
  [path response-object image-name]
  (let [target-file (new java.io.File path image-name)]
    (with-open [xout (new java.io.FileOutputStream target-file)]
      (let [cos (CodedOutputStream/newInstance xout)]
        (.writeTo response-object cos)
        (.flush cos)
        ))))

