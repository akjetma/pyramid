(ns pyramid.grid
  (:require [reagent.core :as reagent]
            [pyramid.math :as math]
            [goog.string :as gstring]
            goog.string.format))

(defn grid-start
  [zoom {:keys [width height]}]
  (let [len (/ (math/pow 2 zoom) 2)]
    {:x (- (* len width))
     :y (- (* len height))}))

(defn translate
  [x y]
  (str "translate(" x "px," y "px)"))

(defn grid-translate
  [{:keys [x y]}]
  (translate (- x) (- y)))

(defn tile-translate
  [{:keys [row col]} {:keys [width height]}]
  (translate (* col width) (* row height)))

(defn tile-src
  [{:keys [zoom row col]}]
  (str "/tile?zoom=" zoom "&row=" row "&col=" col))

(defn tile-component
  [{:keys [zoom row col] :as tile} tile-size & [opts]]
  (let [attrs {:src (tile-src tile)
               :style {:transform (tile-translate tile tile-size)
                       :transform-origin "0px 0px"
                       :position "absolute"
                       :z-index zoom}}]
    [:img.tile (merge attrs opts)]))

(defn grid
  [vzoom vport tile-size tiles]
  [:div.grid {:style {:transform (grid-translate vport)}}
   (for [[zoom row col] tiles]
     ^{:key (str zoom row col)}
     [tile-component {:zoom zoom :row row :col col} tile-size])])
