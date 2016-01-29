(ns pyramid.coordinate
  (:require [pyramid.math :as math]))

(defn lower-bound
  [coord low]
  (mapv max coord low))

(defn upper-bound
  [coord high]
  (mapv min coord high))

(defn normalize
  [coord dims]
  (mapv / coord dims))

(defn translate
  [coord distance]
  (mapv + coord distance))

(defn difference
  [a b]
  (mapv - a b))

(defn scale
  [dims factors]
  (mapv * dims factors))

(defn re-map
  [coordinate in-dims out-dims]
  (scale (normalize coordinate in-dims) out-dims))

(defn px->grid
  [px-coord px-dims grid-dims]
  (mapv
   math/floor
   (re-map px-coord px-dims grid-dims)))

(defn grid->px
  [grid-coord grid-dims px-dims]
  (re-map grid-coord grid-dims px-dims))

(defn center
  [coordinate dimensions]
  (mapv 
   + 
   coordinate
   (mapv #(/ % 2) dimensions)))

(defn decenter
  [coordinate dimensions]
  (mapv
   -
   coordinate
   (mapv #(/ % 2) dimensions)))
