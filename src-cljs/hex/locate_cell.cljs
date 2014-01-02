(ns hex.locate-cell)

;;; This module takes a board geometry and x, y coordinates of a pixel within
;;; a canvas. Using that info, it computes which cell the pixel is in.

(defn- row-in-board [x y board]
  (if (>= (mod y (:row-h board))
          (:diag-y board))
    ;; Easy case: point is in the rectangular area between hex tops and
    ;; bottoms.
    (Math/floor (/ y (:row-h board)))

    ;; Not in rectangular section; this is harder.
    (let [lower-row (Math/floor (/ y (:row-h board)))
          upper-row (dec lower-row)

          ;; Point must be in either upper-row or lower-row.
          ;;
          ;; Consider the diagonal lines that separate these two rows.
          ;; If we draw a bounding box around each such line,
          ;; the point (x,y) is in one such bounding box. Which one?
          ;;
          ;; (Consider 0 the leftmost bounding box for the bottom of
          ;;  upper-row.)

          which-box
            (- (Math/floor (/ x (:diag-x board))) upper-row)

          ;; Does the line rise to the right?
          fwd-line? (odd? which-box)

          slope (* (/ (:diag-y board) (:diag-x board))
                    (if fwd-line? -1 1))

          sy ; Starting y point of the line.
            (+ (* (:row-h board) lower-row)
               (if fwd-line? (:diag-y board) 0))

          sx ; Starting x point of the line.
            (* (:diag-x board)
               (+ which-box upper-row))

          line-y ; Y value on the line we're testing against.
            (+ (* slope (- x sx))
               sy)]

      (if (>= line-y y) ; Above the line.
        upper-row
        lower-row))))

(defn- cell-in-board- [x y board]
  "Which cell is represented by pixel (x,y) in the board: [col row] pair."
  (let [row (row-in-board x y board)
        col (Math/floor (/ (- x (* (:diag-x board) row))
                           (:cell-w board)))]
    [col row]))

(defn cell-in-board [x y board]
  "In which cell ([col row] pair) is pixel (x,y)? Nil if not in board."
  (let [[col row] (cell-in-board- x y board)]
    (when (and (< -1 col (:cols board))
               (< -1 row (:rows board)))
      [col row])))
