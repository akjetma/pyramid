(ns pyramid.tile
  (:require [fivetonine.collage.util :as image]
            [fivetonine.collage.core :as collage]
            [clojure.java.io :as io]
            [pyramid.util :as util]))



;; --- Math helpers ---

(defn log2 
  [n]
  (/ (Math/log n) (Math/log 2)))

(defn ceil
  [n]
  (if (> n Integer/MAX_VALUE)
    (bigint (Math/ceil n))
    (int (Math/ceil n))))

(defn floor
  [n]
  (dec (ceil n)))



;; --- Data structure/algorithm ---

(defn zoom-depth
  [{source-width :width} tile-width]
  (ceil (log2 (/ source-width tile-width))))

(defn side-length
  [zoom]
  (int (Math/pow 2 zoom)))

(defn tile
  [zoom width height row col]
  {:zoom zoom
   :row row
   :col col
   :x (* col width)
   :y (* row height)
   :width width
   :height height})

(defn tiles
  [{:keys [width height]} zoom]
  (let [per-side (side-length zoom)
        tile-width (floor (/ width per-side))
        tile-height (floor (/ height per-side))]
    (mapcat
     #(map
       (partial tile zoom tile-width tile-height %)
       (range per-side))
     (range per-side))))

(defn tileset
  [dimension max-zoom]
  (mapcat
   (partial tiles dimension)
   (range (inc max-zoom))))



;; --- File system/generators/io ---

(defn make-tile!
  [source output-width {:keys [x y width height] :as tile}]
  (let [path (-> tile util/tile-path util/resource-path)]
    (io/make-parents path)
    (-> source
        (collage/crop x y width height)
        (collage/resize :width output-width)
        (image/save path))))

(defn make-tiles!
  ([source-path] (make-tiles! source-path 1280))
  ([source-path output-width]
   (let [source (-> source-path util/resource-path image/load-image)
         source-dimensions {:width (.getWidth source) 
                            :height (.getHeight source)}
         max-zoom (zoom-depth source-dimensions output-width)
         tiles (tileset source-dimensions max-zoom)]
     (doseq [tile tiles]
       (make-tile! source output-width tile)))))
