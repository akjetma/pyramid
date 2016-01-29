(ns pyramid.mouse
  (:require [pyramid.coordinate :as coordinate]))

(defonce *listeners* (atom {}))

(defn ignore!
  [event-type]
  (let [listener (get @*listeners* event-type (constantly nil))]
    (swap! *listeners* dissoc event-type)
    (.removeEventListener js/window event-type listener)))

(defn listen!
  [event-type listener]
  (ignore! event-type)
  (swap! *listeners* assoc event-type listener)
  (.addEventListener js/window event-type listener))

(defn mouse-coords
  [e]
  [(.-pageX e)
   (.-pageY e)])

(defn wrap-move
  [move anchor]
  (fn [e]
    (let [loc (mouse-coords e)
          distance (coordinate/difference loc anchor)]
      (move distance))))

(defn wrap-up
  [on-move up]
  (fn [_]
    (up)
    (ignore! "mousemove")
    (ignore! "mouseup")))

(defn wrap-down
  [{:keys [down move up]}]
  (fn [e]
    (let [anchor (mouse-coords e)
          on-move (wrap-move move anchor)
          on-up (wrap-up on-move up)]
      (down)
      (listen! "mousemove" on-move)
      (listen! "mouseup" on-up))))

(defn wrap-keypress
  [keypress]
  (fn [e]
    (let [code (.-keyCode e)]
      (keypress code))))

(defn stop-mouse!
  []
  (map ignore! ["mousemove" "mouseup" "mousedown" "keypress"]))

(defn start-mouse!
  [{:keys [keypress] :as callbacks}]
  (stop-mouse!)
  (let [on-down (wrap-down callbacks)
        on-keypress (wrap-keypress keypress)]
    (listen! "keypress" on-keypress)
    (listen! "mousedown" on-down)))
