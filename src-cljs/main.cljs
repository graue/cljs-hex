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
        {diag-x :diag-x diag-y :diag-y row-h :row-h vert-y :vert-y}
          (hexboard-geometry w h cols rows)
        ctx (canvas/get-context board "2d")]
    (dotimes [row (+ 1 rows)]
      (let [row-top (* row row-h)
            row-indent (+ (* (- row 1) diag-x)
                          (* (.-lineWidth ctx) 0.5))]
        (draw-path ctx
                   (get-zigzag-pts row-indent row-top diag-x diag-y
                                   (* 2 cols)))))))
