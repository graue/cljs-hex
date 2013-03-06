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

(defn ^:export draw [board]
  (let [w 700 h 500 cols 10 rows 10
        {diag-x :diag-x diag-y :diag-y row-h :row-h vert-y :vert-y
         cell-w :cell-w}
          (hexboard-geometry w h cols rows)
        ctx (canvas/get-context board "2d")]
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
