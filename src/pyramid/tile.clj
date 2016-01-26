(ns pyramid.tile
  (:require [fivetonine.collage.util :as util]
            [fivetonine.collage.core :as collage]
            [clojure.java.io :as io]))

(def resource-path
  "/Users/adamjetmalani/code/pyramid/resources")

(defn ceil
  [n]
  (if (integer? n)
    n
    (let [cast (if (>= n Integer/MAX_VALUE) bigint int)]
      (cast n))))

(defn abs
  [n]
  (if (pos? n)
    n
    (* -1 n)))

(defn exp
  [base power]
  (let [agg (if (neg? power) 
              (partial / 1)
              identity)]
    (agg
     (reduce
      *
      (repeat (abs power) base)))))

(defn tile-dimension
  [image zoom-level]
  (let [width (image/width image)
        height (image/height image)
        axis-count (exp 2 zoom-level)
        divide #(ceil (/ % axis-count))]
    {:width (divide width)
     :height (divide height)}))

(defn save-image!
  [image zoom-level row col]
  (image/save 
   image 
   (str resource-path "/tile/tile_" zoom-level "_" row "_" col ".jpg")))

(defn get-tile
  [image {:keys [width height]} row col]
  (image/sub-image
   image
   (* row width) (* col height)
   width
   height))

;; (defn image-tiling
;;   [image zoom-level]
;;   (let [dimension (tile-dimension image zoom-level)
;;         axis-count (exp 2 zoom-level)]
;;     (map
;;      (fn [row]
;;        (map
;;         (partial get-tile image dimension row)
;;         (range axis-count)))
;;      (range axis-count))))

(defn split-image!
  [image zoom-level]
  (let [dimension (tile-dimension image zoom-level)
        axis-count (exp 2 zoom-level)]
    (mapv
     (fn [row]
       (mapv
        (fn [col]
          (save-image!
           (image/resize
            (get-tile image dimension row col)
            1280)
           zoom-level 
           row 
           col))
        (range axis-count)))
     (range axis-count))
    nil))

(defn load-image
  [path]
  (image/load-image-resource path))

(defn process
  [path]
  ())
