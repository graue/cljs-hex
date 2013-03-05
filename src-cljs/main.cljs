(ns hexcanvas.main
  (:require [monet.canvas :as canvas])
  (:use     [hexcanvas.geometry :only (hexboard-geometry)]))

(defn ^:export draw [board]
  (let [w 700 h 500 cols 10 rows 10
        geom (hexboard-geometry w h cols rows)]
    (->
      (canvas/get-context board "2d")
      (canvas/begin-path)
      (canvas/move-to 150.5 150.5)
      (canvas/line-to 200.5 150.5)
      (canvas/line-to 175.5 183.5)
      (canvas/line-to 150.5 150.5)
      (canvas/stroke))))
