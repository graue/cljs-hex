(ns hex.draw
  (:require [monet.canvas :as canvas]))

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

(defn draw-board [board canvas-elmt]
  (let [{w :w h :h cols :cols rows :rows
         diag-x :diag-x diag-y :diag-y row-h :row-h vert-y :vert-y
         cell-w :cell-w}
          board
        ctx (canvas/get-context canvas-elmt "2d")]
    (canvas/clear-rect ctx {:x 0 :y 0 :w w :h h})
    (dotimes [row (inc rows)]
      (let [row-top (* row row-h)
            row-indent (+ (* row diag-x)
                          (* (.-lineWidth ctx) 0.5))
            first-row? (zero? row)
            last-row? (== row rows)
            diag-pts
              (get-zigzag-pts (- row-indent diag-x) row-top
                              diag-x diag-y
                              (inc (* 2 cols)))]
        (draw-path
          ctx
          (cond first-row? (subvec diag-pts 1)
                last-row?  (subvec diag-pts 0 (dec (count diag-pts)))
                :else      diag-pts))
        (if (not last-row?)
          (dotimes [col (inc cols)]
            (let [col-x (+ row-indent (* col cell-w))
                  col-top (+ row-top diag-y)]
              (draw-path ctx [[col-x col-top]
                              [col-x (+ col-top vert-y)]]))))))

    ;;; Draw the red and blue stones atop the grid.
    (doseq [[[col row] color] @(:state board)]
      (let [x (+ (* row diag-x)
                 (* (.-lineWidth ctx) 0.5)
                 (* (+ col 0.5) cell-w))
            y (+ (* row row-h)
                 (/ (+ row-h diag-y) 2))]

        (canvas/fill-style ctx color)
        (canvas/circle ctx {:x x :y y :r (dec (/ cell-w 2.75))})))))
