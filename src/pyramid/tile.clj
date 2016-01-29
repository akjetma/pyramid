(ns pyramid.tile
  (:require [fivetonine.collage.util :as image]
            [fivetonine.collage.core :as collage]
            [clojure.java.io :as io]
            [pyramid.util :as util]
            [pyramid.math :as math]))

(defn side-length
  [zoom]
  (int (math/pow 2 zoom)))

(defn zoom-depth
  [{source-width :width} tile-width]
  (math/ceil (math/log2 (/ source-width tile-width))))

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
        tile-width (math/floor (/ width per-side))
        tile-height (math/floor (/ height per-side))]
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

(defn tiletree
  [tiles]
  (reduce
   (fn 
     [tree {:keys [zoom row col] :as tile}]
     (assoc-in tree [zoom row col] tile))
   {}
   tiles))

(defn make-tile!
  [source output-width {:keys [x y width height] :as tile}]
  (let [path (-> tile util/tile-path util/resource-path)]
    (io/make-parents path)
    (-> source
        (collage/crop x y width height)
        (collage/resize :width output-width)
        (image/save path :quality 1.0))))

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
