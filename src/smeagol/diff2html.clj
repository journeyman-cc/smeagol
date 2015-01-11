(ns smeagol.diff2html)

(defn diff2html 
  "Convert this string, assumed to be in diff format, to HTML."
  [^String diff-text]
  ;; TODO doesn't work yet
  (str "<pre>" diff-text "</pre>"))
  