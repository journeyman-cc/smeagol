(ns smeagol.include
  (:require
    [schema.core :as s]
    [com.stuartsierra.component :as component]
    [smeagol.include.resolver :as resolver]))

(s/defrecord Includer
  [resolver])

(defprotocol IncludeMd
  (expand-include-md
    [includer md-src]
    "return a markfown file content for given uri."))

(extend-type Includer
  IncludeMd
  (expand-include-md [includer md-src]
    ;parse md-src
    ;resolve found includes
    ;indent & integrate
    md-src))

(s/defn
  new-includer
  []
  (map->Includer {}))

(def IncludeLink
  {:uri s/Str
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
  (re-seq #".*&\[\w*(.*)\w*\]\((.*)\).*" md-src))

(s/defn
  parse-include-md :- [IncludeLink]
  [md-src :- s/Str]
  (vec
    (map
      (fn [parse-element]
        (let [uri (nth parse-element 2)
              indents-text (nth parse-element 1)]
          {:uri uri
           :indent-heading (convert-indent-to-int (parse-indent-heading indents-text))
           :indent-list (convert-indent-to-int (parse-indent-list indents-text))}))
      (parse-include-link md-src))))
