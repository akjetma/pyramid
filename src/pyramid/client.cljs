(ns pyramid.client
  (:require [reagent.core :as reagent]
            [devtools.core :as devtools]
            [ajax.core :as http]
            [pyramid.grid :as grid]
            [pyramid.control :as control]
            [pyramid.math :as math]
            [pyramid.coordinate :as coordinate]))

;; Main namespace for the client. Contains a lot of mixed functionality 
;; including application lifecycle, data fetching, high level react
;; components and the behavior for zooming and panning. Would probably
;; split these things up in a larger application.

(defn zoom-level
  [state zoom]
  (let [{:keys [rows cols]} (get-in @state [:levels (keyword (str zoom))])]
    [rows cols]))

(defn visible-tiles
  "Computes the list of visible tiles given the current app state.
  This is the main/god function of the application, though it is completely
  free of side-effects."
  [state]
  (let [{:keys [coords viewport zoom tile-size]} @state
        
        ;; column-major representation of the size of the active zoom-level grid
        grid-dims (reverse (zoom-level state zoom))
        ;; pixel representation of the size of the grid
        px-dims (coordinate/scale grid-dims tile-size)
        ;; bottom-right corner of the viewport in pixels
        px-max (coordinate/translate coords viewport)
        ;; index of the tile encompassing the top-left corner of the viewport
        grid-view-min (coordinate/px->grid coords px-dims grid-dims)
        ;; index of the tile encompassing the bottom-right corner of the viewport, + 1 tile as a buffer.       
        grid-view-max (coordinate/translate
                       (coordinate/px->grid px-max px-dims grid-dims)
                       [1 1])
        ;; make sure top-left tile is at least [0 0]
        [col-min row-min] (coordinate/lower-bound grid-view-min [0 0])
        ;; make sure bottom-right tile is at most [cols rows]
        [col-max row-max] (coordinate/upper-bound grid-view-max grid-dims)]
    ;; compile a vector of vectors describing the tiles within the defined space.
    ;; [[2 1 0]] for example refers to: 
    ;; the first column (0) of the second row (1) of the third zoom-level (2)
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
  ;; Determines the top-left pixel coordinate for the viewport at the new zoom-level,
  ;; maintaining the center of the viewport's fractional position, and updates the state 
  ;; map's zoom-level and coordinate.
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

(defn key-press!
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
      (control/start-control!
       {:key-press (partial key-press! state)
        :mouse-down (partial mouse-down! state)
        :mouse-move (partial mouse-move! state)
        :mouse-up (partial mouse-up! state)})
      (reagent/render-component
       [app state]
       (.-body js/document)))))

(defonce load!
  (let [reload! (make-reload!)]
    (enable-console-print!)
    (devtools/install!)
    (reload!)
    reload!))

 
