(ns smeagol.include
  (:require
    [schema.core :as s]
    [com.stuartsierra.component :as component]
    [smeagol.include.parse :as parse]
    [smeagol.include.resolve :as resolve]))

(s/defrecord Includer
  [resolver])

(defprotocol IncludeMd
  (expand-include-md
    [includer md-src]
    "return a markfown file content for given uri."))

(extend-type Includer
  IncludeMd
  (expand-include-md [includer md-src]
    (let [includes (parse/parse-include-md md-src)]
      ;resolve found includes
      ;indent & integrate
      md-src)))

(s/defn
  new-includer
  []
  (map->Includer {}))
