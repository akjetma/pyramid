(ns pyramid.zoom
  (:require [reagent.core :as reagent]
            [devtools.core :as devtools]
            [ajax.core :as http]
            [pyramid.grid :as grid]
            [pyramid.mouse :as mouse]
            [pyramid.math :as math]
            [pyramid.coordinate :as coordinate]))

(defn zoom-level
  [state zoom]
  (let [{:keys [rows cols]} (get-in @state [:levels (keyword (str zoom))])]
    [rows cols]))

(defn visible-tiles
  [state]
  (let [{:keys [coords viewport zoom tile-size]} @state
        grid-dims (reverse (zoom-level state zoom))
        px-dims (coordinate/scale grid-dims tile-size)
        px-max (coordinate/translate coords viewport) 
        grid-view-min (coordinate/px->grid coords px-dims grid-dims)
        grid-view-max (coordinate/translate
                       (coordinate/px->grid px-max px-dims grid-dims)
                       [1 1])
        [col-min row-min] (coordinate/lower-bound grid-view-min [0 0])
        [col-max row-max] (coordinate/upper-bound grid-view-max grid-dims)]
    (mapcat
     (fn [row] 
       (mapv
        (partial vector zoom row)
        (range col-min col-max)))
     (range row-min row-max))))

(defn get-levels!
  [state]
  (http/GET "/zoom-levels" 
            {:handler #(swap! state assoc :levels %)
             :keywords? true
             :response-format :json}))

(defn update-tiles!
  [state]
  (let [tiles (visible-tiles state)]
    (swap! state assoc :tiles tiles)))

(defn zoom!
  [state new-zoom]
  (let [{:keys [viewport coords zoom tile-size]} @state
        current-px-dims (coordinate/scale 
                         (reverse (zoom-level state zoom)) 
                         tile-size)
        new-px-dims (coordinate/scale 
                     (reverse (zoom-level state new-zoom))
                     tile-size)
        new-center (coordinate/re-map
                    (coordinate/center coords viewport)
                    current-px-dims
                    new-px-dims)
        new-coords (coordinate/decenter new-center viewport)]
    (swap! state assoc :zoom new-zoom :coords new-coords)
    (update-tiles! state)))

(defn mouse-up!
  [state]
  (update-tiles! state))

(defn mouse-move!
  [state distance]
  (let [anchor (:anchor @state)
        new-coords (coordinate/difference anchor distance)]
    (swap! state assoc :coords new-coords)))

(defn mouse-down!
  [state]
  (let [coords (:coords @state)]
    (swap! state assoc :anchor coords)))

(defn keypress!
  [state code]
  (let [{:keys [zoom levels]} @state
        zmax (dec (count levels))
        new-zoom 
        (case code
          61 (min (inc zoom) zmax)
          45 (max (dec zoom) 0)
          nil)]
    (when (not= new-zoom zoom)
      (zoom! state new-zoom))))

(defn viewport
  [state]
  [:div#viewport
   [grid/grid state]])

(defn on-pre-load!
  [state]
  (let [tile (.getElementById js/document "base-tile")
        tile-size [(.-width tile) (.-height tile)]]
    (swap! state assoc :tile-size tile-size)
    (update-tiles! state)))

(defn pre-tile!
  [state]
  [:img
   {:id "base-tile"
    :src (grid/tile-src 0 [0 0])
    :on-load #(on-pre-load! state)}])

(defn app
  [state]
  [:div#app
   (let [{:keys [tile-size tiles]} @state]
     (cond 
       (nil? tile-size) 
       [pre-tile! state]           
       
       (and (some? tile-size) (some? tiles)) 
       [viewport state]))])

(defn make-reload!
  []
  (let [state (reagent/atom 
               {:zoom 0 
                :viewport [(.-innerWidth js/window) (.-innerHeight js/window)]
                :coords [0 0]})]
    (get-levels! state)    
    (fn []
      (mouse/start-mouse!
       {:keypress (partial keypress! state)
        :down (partial mouse-down! state)
        :move (partial mouse-move! state)
        :up (partial mouse-up! state)})
      (reagent/render-component
       [app state]
       (.-body js/document)))))

(defonce load!
  (let [reload! (make-reload!)]
    (enable-console-print!)
    (devtools/install!)
    (reload!)
    reload!))

 
