(ns pyramid.zoom
  (:require [reagent.core :as reagent]
            [devtools.core :as devtools]
            [ajax.core :as http]
            [pyramid.viewport :as viewport]
            [pyramid.grid :as grid]
            [pyramid.mouse :as mouse]
            [pyramid.math :as math]))

;; --- Ajax ---

(defn got-levels
  [state response]
  (swap! state assoc :levels response))

(defn get-levels
  [state]
  (http/GET "/zoom-levels" {:handler (partial got-levels state)
                            :keywords? true
                            :response-format :json}))

;; --- Computations ---

(defn update-tiles!
  [state]
  (let [{:keys [zoom tile-size vport]} @state
        tiles (viewport/current-tiles zoom tile-size vport)]
    (swap! state assoc :tiles tiles)))

(defn decimal-center
  [{:keys [x y]} tile-size grid-size]
  {:x (/ (+ x (/ (:width tile-size) 2)) (:width grid-size))
   :y (/ (+ y (/ (:height tile-size 2)) (:height grid-size)))})

(defn zoom!
  [state new-zoom]
  (let [{:keys [vport levels zoom tile-size]} @state
        {:keys [rows cols]} (get levels (keyword (str zoom)))
        grid-size {:width (* cols (:width tile-size))
                   :height (* rows (:height tile-size))}
        d-center (decimal-center vport tile-size grid-size)
        new-level (get levels (keyword (str new-zoom)))
        new-center {:x (math/floor (* (:cols new-level) (:width tile-size) (:x d-center)))
                    :y (math/floor (* (:rows new-level) (:height tile-size) (:y d-center)))}
        new-vp {:x (- (:x new-center) (/ (:width tile-size) 2))
                :y (- (:y new-center) (/ (:height tile-size) 2))}]
    (swap! state assoc :zoom new-zoom :vport (merge vport new-vp))
    (update-tiles! state)))

;; --- Event handlers ---

(defn mouse-up
  [state]
  (update-tiles! state))

(defn mouse-move
  [state distance]
  (let [anchor (:anchor @state)
        new-coords (merge-with + anchor distance)]
    (swap! state update :vport merge new-coords)))

(defn mouse-down
  [state]
  (let [anchor (select-keys (:vport @state) [:x :y])]
    (.log js/console anchor)
    (swap! state assoc :anchor anchor)))

(defn keypress
  [state code]
  (let [{:keys [zoom levels]} @state
        zmax (dec (count levels))
        new-zoom 
        (case code
          61 (min (inc zoom) zmax)
          45 (max (dec zoom) 0)
          nil)]
    (zoom! state new-zoom)))

;; --- Component ---

(defn base-tile
  [state]
  [grid/tile-component
   {:zoom 0 :row 0 :col 0}
   {}
   {:id "base-tile"
    :on-load
    (fn [_]
      (let [tile (.getElementById js/document "base-tile")
            tile-size {:width (.-width tile) :height (.-height tile)}]
        (swap! state assoc :tile-size tile-size)
        (update-tiles! state)))}])

(defn app
  [state]
  [:div#app
   (let [{:keys [tile-size tiles]} @state]
     (cond (nil? tile-size) [base-tile state]           
           (and (some? tile-size) (some? tiles)) 
           [viewport/viewport state]))])

;; --- Loading/reloading ---

(defn make-reload
  []
  (let [state (reagent/atom 
               {:zoom 0 
                :vport {:width 800 
                        :height 600
                        :x 0 :y 0}})]
    (get-levels state)
    
    (fn []
      (mouse/start-mouse!
       {:keypress (partial keypress state)
        :down (partial mouse-down state)
        :move (partial mouse-move state)
        :up (partial mouse-up state)})
      (reagent/render-component
       [app state]
       (.-body js/document)))))

(defonce load  
  (let [reload (make-reload)]
    (enable-console-print!)
    (devtools/install!)
    (reload)
    reload))

 
