(ns pyramid.control
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

(defn wrap-mouse-move
  [mouse-move anchor]
  (fn [e]
    (let [loc (mouse-coords e)
          distance (coordinate/difference loc anchor)]
      (mouse-move distance))))

(defn wrap-mouse-up
  [on-move mouse-up]
  (fn [_]
    (mouse-up)
    (ignore! "mousemove")
    (ignore! "mouseup")))

(defn wrap-mouse-down
  [{:keys [mouse-down mouse-move mouse-up]}]
  (fn [e]
    (let [anchor (mouse-coords e)
          on-move (wrap-mouse-move mouse-move anchor)
          on-up (wrap-mouse-up on-move mouse-up)]
      (mouse-down)
      (listen! "mousemove" on-move)
      (listen! "mouseup" on-up))))

(defn wrap-key-press
  [key-press]
  (fn [e]
    (let [code (.-keyCode e)]
      (key-press code))))

(defn stop-control!
  []
  (map ignore! ["mousemove" "mouseup" "mousedown" "keypress"]))

(defn start-control!
  [{:keys [key-press] :as callbacks}]
  (stop-control!)
  (let [on-mouse-down (wrap-mouse-down callbacks)
        on-key-press (wrap-key-press key-press)]
    (listen! "keypress" on-key-press)
    (listen! "mousedown" on-mouse-down)))
