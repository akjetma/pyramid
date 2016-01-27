(ns pyramid.util
  (:require [clojure.stacktrace :as trace]))

(defn resource-path
  [path]
  (str "/Users/adamjetmalani/code/pyramid/resources/" path))

(defn tile-path
  [{:keys [zoom row col]}]
  (str "tile/" zoom "/" row "/" col ".jpg"))
