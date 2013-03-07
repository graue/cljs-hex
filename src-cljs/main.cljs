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
          board  ; refers to the global "board" defined above
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

(defn row-in-board [x y board]
  (.log js/console (str "==> Testing point (" x "," y ")"))
  (if (>= (mod y (:row-h board))
          (:diag-y board))
    ; Easy case: point is in the rectangular area between hex tops and
    ; bottoms.
    (do
      (.log js/console "Easy case")
      (Math/floor (/ y (:row-h board))))

    ; Not in rectangular section; this is harder.
    (let [lower-row (Math/floor (/ y (:row-h board)))
          upper-row (- lower-row 1)

          ; Point must be in either upper-row or lower-row.
          ;
          ; Consider the diagonal lines that separate these two rows.
          ; If we draw a bounding box around each such line,
          ; the point (x,y) is in one such bounding box. Which one?
          ;
          ; (Consider 0 the leftmost bounding box for the bottom of
          ;  upper-row.)

          which-box
            (- (Math/floor (/ x (:diag-x board))) upper-row)

          ; Does the line rise to the right?
          fwd-line? (odd? which-box)

          slope (* (/ (:diag-y board) (:diag-x board))
                    (if fwd-line? -1 1))

          sy ; Starting y point of the line.
            (+ (* (:row-h board) lower-row)
               (if fwd-line? (:diag-y board) 0))

          sx ; Starting x point of the line.
            (* (:diag-x board)
               (+ which-box upper-row))

          f ; Function for the line we're testing against
            (fn [x] (+ (* slope (- x sx))
                       sy))]

      (.log js/console "Hard case")
      (.log js/console
        (str "f(x) = " slope "*(" x "-" sx ") + " sy))
      (.log js/console (str "x=" x ",y=" y ",lineY=" (f x)))
      (.log js/console
        (str "Line runs from (" sx "," (f sx) ") to ("
             (+ sx (:diag-x board)) ","
             (f (+ sx (:diag-x board))) ") supposedly"))
      (if (>= (f x) y) ; above the line
        upper-row
        lower-row))))

(defn cell-in-board [x y board]
  "Which cell is represented by pixel (x,y) in the board: [col row] pair."
  (let [row (row-in-board x y board)
        col (Math/floor (/ (- x (* (:diag-x board) row))
                        (:cell-w board)))]
    [row col]))

(defn ^:export click [event]
  (let [[x y] (rel-mouse-coords event)
        [row col] (cell-in-board x y board)]
    (js/alert (str "Clicked on cell " col " from left, " row " from top"))))
