(ns pyramid.coordinate
  (:require [pyramid.math :as math]))


;; -------------------------------------------------------------------
;; Most of the logic on the front-end involves manipulating 
;; coordinates. If I represent coordinates and dimensions as 
;; 2-tuple vectors ([x y] or [col row] for coords, 
;; [width height] or [cols rows] for dimensions/spaces.) 
;; mapping between the various coordinate systems becomes
;; very easy.


;; -------------------------------------------------------------------
;; lower-bound and upper-bound return the components of the 
;; input vector bounded by a limiting vector. 
;; example: 
;; #=> (lower-bound [-12 24] [0 0])
;; [0 24]

(defn lower-bound
  "Returns coord with components >= the components of 'low."
  [coord low]
  (mapv max coord low))

(defn upper-bound
  [coord high]
  (mapv min coord high))


;; -------------------------------------------------------------------
;; Component-wise vector operations. I gave them names that 
;; represent what they are doing in the context of this 
;; project. These functions all take two ordered pairs.

(defn normalize
  "Fractional representation of a coordinate within a space.
  So, in the space [2 3], the coordinate [1 1] is [1/2, 1/3]"
  [coord dims]
  (mapv / coord dims))

(defn translate
  [coord distance]
  (mapv + coord distance))

(defn difference
  [a b]
  (mapv - a b))

(defn scale
  "Grow or shrink a space by some factor in each dimension.
  Example: (scale [800 600] [2 2]) [1600 1200]"
  [dims factors]
  (mapv * dims factors))


;; -------------------------------------------------------------------
;; These functions are used to project a coordinate from one 
;; space into a coordinate in another space.

(defn re-map
  "Maps coordinate between spaces by getting the fractional
  representation of the coordinate in the input space and
  multiplying it by the dimensions of the output space."
  [coordinate in-dims out-dims]
  (scale (normalize coordinate in-dims) out-dims))

(defn px->grid
  "Given a pixel coordinate [x y], and the dimensions of the pixel
  space [width height], return a vector of indices [col row] in
  the grid space [cols rows] that represent the encompassing
  cell in the grid."
  [px-coord px-dims grid-dims]
  (mapv
   math/floor
   (re-map px-coord px-dims grid-dims)))

(defn grid->px
  [grid-coord grid-dims px-dims]
  (re-map grid-coord grid-dims px-dims))


;; -------------------------------------------------------------------
;; Used to manipulate view box coordinates in the context of
;; its encompassing space.

(defn center
  "Given the top-left coordinate and dimensions of a bounding
  box, return the coordinate in the center of the box."
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
