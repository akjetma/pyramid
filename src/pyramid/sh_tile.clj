(ns pyramid.sh-tile
  (:require [pyramid.tile :as tile]
            [pyramid.util :as util]))

(defn -main
  ([input-path] (-main input-path tile/default-tile-width))
  ([input-path tile-width]
   (let [output-path (util/dir input-path)
         width (Integer/parseInt (str tile-width))]     
     (tile/make-tiles! input-path output-path width))))
