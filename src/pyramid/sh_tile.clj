(ns pyramid.sh-tile
  (:require [pyramid.tile :as tile]
            [pyramid.util :as util]))

(defn -main
  ([input-path] (-main input-path (util/dir input-path)))
  ([input-path output-path]
   (println "tiling " input-path " into " output-path)
   (tile/make-tiles! input-path)
   (println input-path " tiles located at " output-path)))
