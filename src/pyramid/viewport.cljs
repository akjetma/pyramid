(ns pyramid.viewport
  (:require [pyramid.math :as math]
            [pyramid.grid :as grid]))

(defn pixels->grid
  [tile-size pixels]
  (math/floor (/ pixels tile-size)))

(defn current-tiles
  [zoom
   {t-width :width t-height :height}
   {v-width :width v-height :height :keys [x y]}]
  (let [max-tile (dec (math/pow 2 zoom))
        col-min (max (pixels->grid t-width x) 0)
        row-min (max (pixels->grid t-height y) 0)
        col-max (min (pixels->grid t-width (+ v-width x)) max-tile)
        row-max (min (pixels->grid t-height (+ v-height y)) max-tile)]
    (mapcat
     (fn [row] 
       (mapv
        (partial vector zoom row)
        (range col-min (inc col-max))))
     (range row-min (inc row-max)))))

(defn viewport
  [state]
  (let [{:keys [zoom vport tile-size tiles]} @state]
    [:div#viewport {:style {:width (str (:width vport))
                            :height (str (:height vport))}}
     [grid/grid zoom vport tile-size tiles]]))
