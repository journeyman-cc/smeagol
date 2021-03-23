(ns ^{:doc "Functions related to the include of markdown-paged - providing
a plugable load-content componet. This namespaces is implementation detail for
smeagol.include and not inteded for direct usage."
      :author "Michael Jerger"}
  smeagol.include.resolve
  (:require
    [schema.core :as s]))

(s/defrecord Resolver
  [type :- s/Keyword
   local-base-dir :- s/Str])

;As schema doesn't support s/defprotocol we use the dispatcher for annotation & validation.
(s/defn dispatch-by-resolver-type :- s/Keyword
  "Dispatcher for different resolver implementations."
  [resolver :- Resolver
   uri :- s/Str]
  (:type resolver))

(defmulti do-resolve-md
  "Multimethod return a markdown file content for given uri."
  dispatch-by-resolver-type)
(s/defmethod do-resolve-md :default
  [resolver :- Resolver
   uri :- s/Str]
  (throw (Exception. (str "No implementation for " resolver))))

(defprotocol ResolveMd
  (resolve-md
    [resolver uri]
    "return a markfown file content for given uri."))

(extend-type Resolver
  ResolveMd
  (resolve-md [resolver uri]
    (s/validate s/Str uri)
    (s/validate s/Str (do-resolve-md resolver uri))))

(s/defn
  new-resolver
  ([type :- s/Keyword]
   (map->Resolver {:type type :local-base-dir nil}))
  ([type :- s/Keyword
    local-base-dir :- s/Str]
   (map->Resolver {:type type :local-base-dir local-base-dir})))
