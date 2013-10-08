(ns hexcanvas.offset)

; From David Nolen - https://gist.github.com/swannodette/5165980
; but I'm not sure I'm convinced of this idea.
; Lots of boilerplate code here, and there's nothing else other than DOM
; elements that would reasonably use these protocols.
; Smells like YAGNI to me.

; Maybe reduce instead of loop/recur is good;
; right now it's a lot less clear to me,
; but that could be my Clojure inexperience talking.
; I do think the parent-seq is cool.

(defprotocol IOffset
  (-offset [x]))

(extend-type js/Element
  IOffset
  (-offset [el]
    [(.-offsetLeft el) (.-offsetTop el)]))

(defprotocol IScroll
  (-scroll [x]))

(extend-type js/Element
  IScroll
  (-scroll [el]
    [(.-scrollLeft el) (.-scrollTop el)]))

(defn addv [v0 v1] (map + v0 v1))
(defn subv [v0 v1] (map - v0 v1))

(defn parent-seq [x]
  (when-let [p (.-offsetParent x)]
    (lazy-seq (cons p (parent-seq p)))))

(defn total-offset [el]
  (let [els (cons el (parent-seq el))
        vs  (map subv (map -offset els) (map -scroll els))]
    (reduce addv [0 0] vs)))
