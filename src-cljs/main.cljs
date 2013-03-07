(ns hexcanvas.main
  (:require [monet.canvas :as canvas])
  (:use     [hexcanvas.geometry :only (hexboard-geometry)]))

(defn draw-path [ctx pts]
  (canvas/begin-path ctx)
  (apply canvas/move-to ctx (first pts))
  (doseq [pt (rest pts)]
    (apply canvas/line-to ctx pt))
  (canvas/stroke ctx))

(defn vectors->path [x0 y0 vs]
  "Convert a series of relative vectors to a path starting at x0, y0."
  (loop [x x0, y y0, acc [], vs vs]
    (if (empty? vs)
      (conj acc [x y])
      (let [dx ((first vs) 0)
            dy ((first vs) 1)]
        (recur (+ x dx) (+ y dy) (conj acc [x y]) (rest vs))))))

(defn get-zigzag-pts [x0 y0 dx dy n]
  "Generate vector of points for n vertically-zigzagging lines."
  (->>
    (cycle [[dx dy] [dx (- dy)]])
    (take n)
    (vec)
    (vectors->path x0 y0)))

(def board
  (let [w 700 h 500 cols 10 rows 10]
    (conj (hexboard-geometry w h cols rows)
          {:w w :h h :cols cols :rows rows})))

(defn ^:export draw [canvas-elmt]
  (let [{w :w h :h cols :cols rows :rows
         diag-x :diag-x diag-y :diag-y row-h :row-h vert-y :vert-y
         cell-w :cell-w}
          board
        ctx (canvas/get-context canvas-elmt "2d")]
    (dotimes [row (+ 1 rows)]
      (let [row-top (* row row-h)
            row-indent (+ (* row diag-x)
                          (* (.-lineWidth ctx) 0.5))
            first-row? (== row 0)
            last-row? (== row rows)
            diag-pts
              (get-zigzag-pts (- row-indent diag-x) row-top
                               diag-x diag-y
                               (+ (* 2 cols) 1))]
        (draw-path
          ctx
          (cond first-row? (subvec diag-pts 1)
                last-row?  (subvec diag-pts 0 (- (count diag-pts) 1))
                :else      diag-pts))
        (if (not last-row?)
          (dotimes [col (+ 1 cols)]
            (let [col-x (+ row-indent (* col cell-w))
                  col-top (+ row-top diag-y)]
              (draw-path ctx [[col-x col-top] [col-x (+ col-top vert-y)]]))))))))

(defn total-offset [elmt]
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
  (let [[x y] (rel-mouse-coords event)]
    (js/alert (str "Board clicked at (" x ", " y ")"))))
