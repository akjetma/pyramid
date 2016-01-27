(ns pyramid.util
  (:require [clojure.string :as string]))

(def resource-root
  "/Users/adamjetmalani/code/pyramid/resources")

(def tile-root 
  "tile")

(defn make-path
  [& parts]
  (string/join "/" parts))

(defn resource-path
  [path]
  (make-path resource-root path))

(defn zoom-path
  [zoom]
  (make-path tile-root zoom))

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
