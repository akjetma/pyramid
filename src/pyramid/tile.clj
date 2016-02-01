(ns pyramid.tile
  (:require [fivetonine.collage.util :as image]
            [fivetonine.collage.core :as collage]
            [clojure.java.io :as io]
            [pyramid.util :as util]
            [pyramid.math :as math]))

(def default-tile-width 1280)

(defn init-message
  [input-path output-path tile-width]
  (println "tiling " input-path 
           " into " tile-width "px tiles at " 
           (util/make-path output-path util/tile-prefix)))

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

(defn make-tile!
  [source output-path output-width {:keys [x y width height] :as tile}]
  (let [path (util/make-path output-path (util/tile-path tile))]
    (io/make-parents path)
    (-> source
        (collage/crop x y width height)
        (collage/resize :width output-width)
        (image/save path :quality 1.0))))

(defn make-tiles!
  ([source-path] (make-tiles! source-path (util/dir source-path)))
  ([source-path output-path] (make-tiles! source-path output-path default-tile-width))
  ([source-path output-path output-width]
   (init-message source-path output-path output-width)
   (let [source (image/load-image source-path)
         source-dimensions {:width (.getWidth source) 
                            :height (.getHeight source)}
         max-zoom (zoom-depth source-dimensions output-width)
         tiles (tileset source-dimensions max-zoom)]
     (doseq [tile tiles]
       (make-tile! source output-path output-width tile)))))
