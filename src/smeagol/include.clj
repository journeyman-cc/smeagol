(ns smeagol.include
  (:require
    [clojure.string :as cs]
    [schema.core :as s]
    [com.stuartsierra.component :as component]
    [smeagol.include.parse :as parse]
    [smeagol.include.resolve :as resolve]))

(s/defrecord Includer
  [resolver])

(defprotocol IncludeMd
  (expand-include-md
    [includer md-src]
    "return a markdown containing resolved includes"))

(s/defn
  do-expand-one-include :- s/Str
  [includer :- Includer
   include :- parse/IncludeLink
   md-src :- s/Str]
  (let [{:keys [uri replace]} include]
    (cs/replace
      md-src
      (re-pattern (cs/escape
                    replace
                    {\[ "\\["
                     \] "\\]"
                     \( "\\("
                     \) "\\)"}))
      (resolve/resolve-md (:resolver includer) uri))))
      ;indent

(s/defn
  do-expand-includes :- s/Str
  [includer :- Includer
   includes :- [parse/IncludeLink]
   md-src :- s/Str]
  (loop [loop-includes includes
         result md-src]
    (if (empty? loop-includes)
      result
      (recur
        (rest loop-includes)
        (do-expand-one-include includer (first loop-includes) result)))))

(extend-type Includer
  IncludeMd
  (expand-include-md [includer md-src]
    (do-expand-includes includer (parse/parse-include-md md-src) md-src)))

(s/defn
  new-includer
  []
  (map->Includer {}))
