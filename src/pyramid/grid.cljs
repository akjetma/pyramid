(ns pyramid.grid
  (:require [reagent.core :as reagent]
            [pyramid.math :as math]
            [pyramid.coordinate :as coordinate]
            [goog.string :as gstring]
            goog.string.format))

(defn css-translate
  [[x y]]
  (str "translate(" x "px," y "px)"))

(defn tile-src
  [zoom [row col]]
  (str "/tile?zoom=" zoom "&row=" row "&col=" col))

(defn tile-component
  [zoom rank tile-size]
  (let [real-coords (coordinate/scale (reverse rank) tile-size)]
    [:img.tile
     {:src (tile-src zoom rank)
      :style {:transform (css-translate real-coords)
              :transform-origin "0px 0px"
              :position "absolute"
              :z-index zoom}}]))

(defn grid
  [state]
  (let [{:keys [coords tile-size tiles]} @state
        real-coords (mapv - coords)]
    ;; (.log js/console real-coords)
    [:div.grid {:style {:transform (css-translate real-coords)}}
     (for [[zoom & rank] tiles]
       ^{:key (str zoom rank)}
       [tile-component zoom rank tile-size])]))
