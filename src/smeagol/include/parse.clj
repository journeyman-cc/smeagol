(ns ^{:doc "Functions related to the include of markdown-paged - parsing of
include links. This namespaces is implementation detail for 
smeagol.include and not inteded for direct usage."
      :author "Michael Jerger"}
  smeagol.include.parse
  (:require
    [schema.core :as s]))

(def IncludeLink
  {:replace s/Str
   :uri s/Str
   :indent-heading s/Num
   :indent-list s/Num})

(s/defn
  convert-indent-to-int :- s/Num
  [indents :- [s/Str]]
  (if (some? indents)
      (Integer/valueOf (nth indents 2))
      0))

(s/defn
  parse-indent-list
  [md-src :- s/Str]
  (re-matches #".*(:indent-list (\d)).*" md-src))

(s/defn
  parse-indent-heading
  [md-src :- s/Str]
  (re-matches #".*(:indent-heading (\d)).*" md-src))

(s/defn
  parse-include-link
  [md-src :- s/Str]
  (re-seq #".*(&\[\w*(.*)\w*\]\((.*)\)).*" md-src))

(s/defn
  parse-include-md :- [IncludeLink]
  [md-src :- s/Str]
  (vec
    (map
      (fn [parse-element]
        (let [replace (nth parse-element 1)
              uri (nth parse-element 3)
              indents-text (nth parse-element 2)]
          {:replace replace
           :uri uri
           :indent-heading (convert-indent-to-int (parse-indent-heading indents-text))
           :indent-list (convert-indent-to-int (parse-indent-list indents-text))}))
      (parse-include-link md-src))))
