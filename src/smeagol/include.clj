(ns smeagol.include
  (:require
    [com.stuartsierra.component :as component]))

(defrecord Resolver [resolver-fn])

(defn new-resolver [resolver-fn]
  (map->Resolver {:resolver-fn resolver-fn}))

(defn resolve-include [resolver uri]
  (apply (:resolver-fn resolver) [uri]))
