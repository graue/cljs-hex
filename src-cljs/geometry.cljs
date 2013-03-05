(ns hexcanvas.geometry)

; Save some typing...
(def sin Math/sin)
(def cos Math/cos)
(def floor Math/floor)
(def round Math/round)
(def pi Math/PI)


; How many cells do you REALLY need space for (horizontally)
; to draw a rows×cols hex grid?
; The last row is indented by (rows-1)/2 cell widths, so...
(defn- virtual-cell-cols [rows cols]
  (+ cols (/ (- rows 1) 2)))

; From this we can compute the maximum cell width.
; We floor the result to guarantee that
; virtual-cell-cols * max-cell-width <= w - 1,
; with max-cell-width an integer.
(defn- max-cell-width-for-box [w rows cols]
  (floor (/ (- w 1) (virtual-cell-cols rows cols))))


; Cell sizes are also limited by height. These definitions will come in
; handy:
(def sin-30 (sin (/ pi 6)))
(def cos-30 (sin (/ pi 6)))

; There's a little vertical overlap between hexes on one row and the next.
; The top of a row is cellHeight * (1-sin(pi/6)) below the top of the
; previous row. When the overlapping bottom part is subtracted, we call that
; "row height".
(def row-bottom-to-cell-height-ratio
  (/ sin-30 (+ 1 (* 2 sin-30))))
(def row-height-to-cell-height-ratio
  (/ (+ 1 sin-30) (+ 1 (* 2 sin-30))))
(def row-bottom-to-row-height-ratio
  (/ row-bottom-to-cell-height-ratio row-height-to-cell-height-ratio))

; Vertically, we need to fit (rows + rowBottomToRowHeightRatio) rows.
(defn- max-row-height-for-box [h rows]
  (floor (/ (- h 1)
            (+ rows row-bottom-to-row-height-ratio))))


; Combine the two constraints on side length (row height and cell width).
(defn- max-side-len-for-box [w h cols rows]
  (Math/min (* (max-row-height-for-box h rows)
               (- 1 row-bottom-to-row-height-ratio))
            (/ (max-cell-width-for-box w rows cols)
               (* 2 cos-30))))


(defn- diag-side-y [side-len]
  "The amount you move up or down drawing a diagonal side. Integer."
  (int (round (* side-len sin-30))))

(defn- diag-side-x [side-len]
  "The amount you move left/right drawing a diagonal side. Integer."
  (int (round (* side-len cos-30))))

(defn- cell-width [side-len]
  "The width of a hex cell with given side-length. Even integer."
  (* diag-side-x 2))

(defn- row-height [side-len]
  "The height of a row (not cell!) with given side length. Integer."
  (->>
    (+ 1 sin-30)
    (* side-len)
    (round)
    (int)))

(defn- vert-side-y [side-len]
  "The length of a vertical side. Basically same as side-len, but may be
   off by one due to rounding. Integer."
  (- (row-height side-len) (diag-side-y side-len)))

(defn hexboard-geometry [w h cols rows]
  (let [side-len (max-side-len-for-box w h cols rows)]
    {:side-len side-len
     :diag-y (diag-side-y side-len)
     :diag-x (diag-side-x side-len)
     :cell-w (cell-width  side-len)
     :row-h  (row-height  side-len)
     :vert-y (vert-side-y side-len)}))
