(ns hexcanvas.main
  (:require [monet.canvas :as canvas])
  (:use     [hexcanvas.geometry :only (hexboard-geometry)]))

(defn draw-path [ctx pts]
  (canvas/begin-path ctx)
  (apply canvas/move-to ctx (first pts))
  (doseq [pt (rest pts)]
    (apply canvas/line-to ctx pt))
  (canvas/stroke ctx))

(defn ^:export draw [board]
  (let [w 700 h 500 cols 10 rows 10
        geom (hexboard-geometry w h cols rows)
        ctx (canvas/get-context board "2d")]
    (draw-path ctx
               [[150.5 150.5]
                [200.5 150.5]
                [175.5 183.5]
                [150.5 150.5]])))
