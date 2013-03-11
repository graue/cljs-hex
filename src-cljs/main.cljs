(ns hexcanvas.main
  (:require [monet.canvas :as canvas]
            [hexcanvas.geometry :refer (hexboard-geometry)]
            [hexcanvas.draw :refer (draw-board)]
            [hexcanvas.locate-cell :refer (cell-in-board)]))

;; Board state is a hashmap with vector keys like [2 3], etc.
(def board-state (atom {}))
(def current-player (atom :red))

(def board
  (let [w 700 h 500 cols 10 rows 10]
    (conj (hexboard-geometry w h cols rows)
          {:w w :h h :cols cols :rows rows
           :state board-state})))

(defn next-player [pl]
  (if (= pl :red)
    :blue
    :red))

(defn ^:export draw [canvas-elmt]
  (draw-board board canvas-elmt))

(defn total-offset [elmt]
  "Find offset of an element's top-left corner within page. Returns a pair."
  (loop [offset-x 0, offset-y 0, current elmt]
    (let [offset-x (+ offset-x (.-offsetLeft current)
                                (- (.-scrollLeft current)))
          offset-y (+ offset-y (.-offsetTop current)
                                (- (.-scrollTop current)))
          parent (.-offsetParent current)]
      (if parent
        (recur offset-x offset-y parent)
        [offset-x offset-y]))))

(defn rel-mouse-coords [event]
  (let [target (.-target event)
        [offset-x offset-y] (total-offset target)]
    [(- (.-pageX event) offset-x)
     (- (.-pageY event) offset-y)]))

(defn ^:export click [event]
  (let [[x y] (rel-mouse-coords event)
        [col row] (cell-in-board x y board)]
    (if (and col row ; In a valid cell?
             (nil? (get @board-state [col row]))) ; Cell not already played in?
      (do
        ;; Place the stone
        (swap! board-state conj {[col row] @current-player})
        (.log js/console (str "Placed stone " @current-player " at " col
                       " , " row))
        (.log js/console (str "Board state is " @board-state))
        ;; Switch to the next player
        (swap! current-player next-player)
        (.log js/console (str "Next player will be: " @current-player))
        ;; Redraw the board
        (draw-board board (.-target event))))))
