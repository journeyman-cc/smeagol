(ns ^{:doc "Functions related to the include of markdown-paged - handling the 
list & heading indents of includes. This namespaces is implementation detail for
smeagol.include and not inteded for direct usage."
      :author "Michael Jerger"}
  smeagol.include.indent
  (:require
    [clojure.string :as cs]
    [schema.core :as s]))

(s/defn
  parse-list
  [md-resolved :- s/Str]
  (distinct
    (into
      (re-seq #"((^|\R? *)([\*\+-] ))" md-resolved)
      (re-seq  #"((^|\R? *)([0-9]+\. ))" md-resolved))))

(s/defn
  parse-heading
  [md-resolved :- s/Str]
  (distinct
    (re-seq #"((^|\R?)(#+ ))" md-resolved)))

(s/defn
  do-indent :- s/Str
  [indent :- s/Num
   indentor :- s/Str
   elements
   md-resolved :- s/Str]
  (loop [result md-resolved
         elements elements]
    (if (empty? elements)
      result
      (let [element (first elements)
            replace (nth element 1)
            start (nth element 2)
            end (nth element 3)]
        (recur
          (cs/replace
           result
           (re-pattern (cs/escape
                         replace
                         {\* "\\*"
                          \n "\\n"}))
           (str start (apply str (repeat indent indentor)) end))
          (rest elements))))))

(s/defn
  do-indent-heading :- s/Str
  [indent :- s/Num
   md-resolved :- s/Str]
  (do-indent indent "#" (parse-heading md-resolved) md-resolved))

(s/defn
  do-indent-list :- s/Str
  [indent :- s/Num
   md-resolved :- s/Str]
  (do-indent indent " " (parse-list md-resolved) md-resolved))
