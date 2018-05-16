(ns smeagol.include.resolver
  (:require
    [schema.core :as s]
    [com.stuartsierra.component :as component]))

(s/defrecord Resolver
  [type :- s/Keyword])

;As schema does'nt support s/defprotocol we use the dispatcher for annotation & validation.
(s/defn dispatch-by-resolver-type :- s/Keyword
  "Dispatcher for different resolver implementations."
  [resolver :- Resolver
   uri :- s/Str]
  (:type resolver))

(defmulti do-resolve-md
  "Multimethod return a markfown file content for given uri."
  dispatch-by-resolver-type)
(s/defmethod do-resolve-md :default
  [resolver :- Resolver
   uri :- s/Str]
  (throw (Exception. (str "No implementation for " resolver))))

(defprotocol ResolveMd
  (resolve-md
    [resolver uri]
    "return a markfown file content for given uru."))

(extend-type Resolver
  ResolveMd
  (resolve-md [resolver uri]
    (s/validate s/Str uri)
    (s/validate s/Str (do-resolve-md resolver uri))))

(s/defn
  new-resolver
  [type :- s/Keyword]
  (map->Resolver {:type type}))
