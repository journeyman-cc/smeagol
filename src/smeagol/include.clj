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
