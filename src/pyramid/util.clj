(ns pyramid.util
  (:require [clojure.string :as string]))

(def tile-prefix "tile")

(defn make-path
  [& parts]
  (string/join "/" parts))

(defn dir
  [path]
  (let [parts (string/split path #"/")]
    (apply make-path (butlast parts))))

(defn zoom-path
  [zoom]
  (make-path tile-prefix zoom))

(defn row-path
  [zoom row]
  (make-path 
   (zoom-path zoom) 
   row))

(defn tile-path
  [{:keys [zoom row col]}]
  (make-path 
   (row-path zoom row)
   (str col ".jpg")))
