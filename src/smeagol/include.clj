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
  parse-include-md :- [IncludeLink]
  [md-src :- s/Str]
  (vec
    (map
      (fn [parse-element]
        {:uri (nth parse-element 5)
         :indent-heading 0
         :indent-list 0})
      (re-seq #"&\[(:indent-heading (d*))?w*(:indent-list (d*))?\]\((.*)\)" md-src))))
