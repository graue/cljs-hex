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
    (filter
      (fn [[col row]]
        (and (>= row 0)
             (>= col 0)
             (< row (:rows board))
             (< col (:cols board))))
      possible-neighbors)))

(defn connected-cells [board [start-col start-row]]
  "Returns a hashset of all cells connected to start cell in an unbroken
   chain of the same color."
  (let [start-color (@(:state board) [start-col start-row])]
    (loop [known     #{}                      ; Set of known connected cells.
           [col row] [start-col start-row]    ; Current cell.
           n         1                        ; Tgk - num recurrences
           to-visit  [[start-col start-row]]] ; Stack of cells to visit next.

      (let [known
              (conj known [col row])

            ;; Remove this cell from the to-visit stack.
            to-visit
              (subvec to-visit 0 (dec (count to-visit)))

            ;; Get the neighbors that are the same color
            ;; and not yet known.
            nbrs
              (->> (get-neighbors board [col row])
                (filter
                  (fn [[nbrcol nbrrow]]
                    (and
                      (= (@(:state board) [nbrcol nbrrow]) start-color)
                      (not (contains? known [nbrcol nbrrow]))))))

            to-visit
              (vec (concat to-visit nbrs))]

        (if (or (empty? to-visit) (>= n 50))
          known ; Terminated - no more cells to visit.
          (recur known (last to-visit) (inc n) to-visit))))))

(defn win-from? [board [col row]]
  "Check if the stone at [col row] is connected to both the left and right
   sides (if blue), or both the top and bottom (if red), via stones of its
   own color."
  (let [pl         (@(:state board) [col row])
        conn-cells (connected-cells board [col row])]
    (and (some #(= (goal? board % pl) 0) conn-cells)
         (some #(= (goal? board % pl) 1) conn-cells))))
