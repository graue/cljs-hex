(ns hexcanvas.check-win)

(defn goal? [board [col row] pl]
  "Is the cell at [col row] a goal for player color pl?
   If so, returns 0 or 1 depending on which goal."
  (cond (or (and (= col 0) (= pl :blue))
            (and (= row 0) (= pl :red)))
          0
        (or (and (= col (dec (:cols board))) (= pl :blue))
            (and (= row (dec (:rows board))) (= pl :red)))
          1
        :else
          false))

(defn get-neighbors [board [col row]]
  "Returns a seq of the neighbors of the given cell."
  (let [possible-neighbors
          [[     col  (dec row)]   ; Upper left
           [(inc col) (dec row)]   ; Upper right
           [(dec col)      row ]   ; Left
           [(inc col)      row ]   ; Right
           [(dec col) (inc row)]   ; Lower left
           [     col  (inc row)]]] ; Lower right

    (filter (fn [[col row]]
              (and (< -1 row (:rows board))
                   (< -1 col (:cols board))))
            possible-neighbors)))

(defn connected-cells [board start-cell]
  "Returns a hashset of all cells connected to start cell in an unbroken
   chain of the same color."
  (let [start-color (@(:state board) start-cell)]
    (loop [known     #{}                      ; Set of known connected cells.
           cell start-cell
           to-visit  [start-cell]] ; Stack of cells to visit next.

      (let [known
              (conj known cell)

            ;; Remove this cell from the to-visit stack.
            to-visit
              (subvec to-visit 0 (dec (count to-visit)))

            ;; Get the neighbors that are the same color
            ;; and not yet known.
            nbrs
            (filter (fn [nb] (and (= start-color (get @(:state board) nb))
                                  (not (contains? known nb))))
                    (get-neighbors board cell))

            to-visit
              (vec (concat to-visit nbrs))]

        (if (empty? to-visit)
          known ; Terminated - no more cells to visit.
          (recur known (last to-visit) to-visit))))))

(defn win-from? [board [col row]]
  "Check if the stone at [col row] is connected to both the left and right
   sides (if blue), or both the top and bottom (if red), via stones of its
   own color."
  (let [pl         (@(:state board) [col row])
        conn-cells (connected-cells board [col row])]
    (and (some #(= (goal? board % pl) 0) conn-cells)
         (some #(= (goal? board % pl) 1) conn-cells))))
