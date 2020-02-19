(ns ^{:doc "Format Semagol's extended markdown format."
      :author "Simon Brooke"}
  smeagol.formatting
  (:require [clojure.data.json :as json]
            [clojure.string :as cs]
            [cemerick.url :refer (url url-encode url-decode)]
            [clj-yaml.core :as yaml]
            [markdown.core :as md]
            [smeagol.configuration :refer [config]]
            [smeagol.extensions.mermaid :refer [process-mermaid]]
            [smeagol.extensions.photoswipe :refer [process-photoswipe]]
            [smeagol.extensions.vega :refer [process-vega]]
            [smeagol.local-links :refer :all]
            [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; Smeagol: a very simple Wiki engine.
;;;;
;;;; This program is free software; you can redistribute it and/or
;;;; modify it under the terms of the GNU General Public License
;;;; as published by the Free Software Foundation; either version 2
;;;; of the License, or (at your option) any later version.
;;;;
;;;; This program is distributed in the hope that it will be useful,
;;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;; GNU General Public License for more details.
;;;;
;;;; You should have received a copy of the GNU General Public License
;;;; along with this program; if not, write to the Free Software
;;;; Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
;;;; USA.
;;;;
;;;; Copyright (C) 2017 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; Right, doing the data visualisation thing is tricky. Doing it in the
;;;; pipeline doesn't work, because the md-to-html-string filter messes up
;;;; both YAML and JSON notation. So we need to extract the visualisation
;;;; fragments from the Markdown text and replace them with tokens we will
;;;; recognise afterwards, perform md-to-html-string, and then replace our
;;;; tokens with the transformed visualisation specification.
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare process-text)


(defn process-backticks
  "Effectively, escape the backticks surrounding this `text`, by protecting them
  from the `process-text` filter."
  [^String text ^Integer index]
  (str "<pre class=\"backticks\">```" (.trim text) "\n```</pre>"))


(defn get-first-token
  "Return the first space-separated token of this `string`."
  [^String string]
  (try
    (if string (first (cs/split (first (cs/split-lines string)) #"[^a-zA-Z0-9]+")))
    (catch NullPointerException _ nil)))


(defn- process-markdown-fragment
  "Within the context of `process-text`, process a fragment believed to be markdown.

  As with `process-text`, this function returns a map with two top-level keys:
  `:inclusions`, a map of constructed keywords to inclusion specifications,
  and `:text`, an HTML text string with the keywords present where the
  corresponding inclusion should be inserted."
  [^Integer index ^clojure.lang.Associative result ^String fragment fragments processed]
  (process-text
    (inc index)
    result
    fragments
    (cons fragment processed)))


(defn deep-merge [v & vs]
  "Cripped in its entirety from https://clojuredocs.org/clojure.core/merge."
  (letfn [(rec-merge [v1 v2]
                     (if (and (map? v1) (map? v2))
                       (merge-with deep-merge v1 v2)
                       v2))]
    (if (some identity vs)
      (reduce #(rec-merge %1 %2) v vs)
      (last vs))))


(defn apply-formatter
  "Within the context of `process-text`, process a fragment for which an explicit
  `formatter` has been identified.

  As with `process-text`, this function returns a map with two top-level keys:
  `:inclusions`, a map of constructed keywords to inclusion specifications,
  and `:text`, an HTML text string with the keywords present where the
  corresponding inclusion should be inserted."
  [^Integer index
   ^clojure.lang.Associative result
   fragments
   processed
   ^String fragment
   ^String token
   formatter]
  (log/info "index:" index "(type result):" (type result) "(type fragments):" (type fragments) "fragment:" fragment "token:" token ":formatter" formatter)
  (let
    [inky (keyword (str "inclusion-" index))
     fkey (keyword token)]
    (process-text
      (inc index)
      (deep-merge
        result
        {:inclusions {inky (eval (list formatter (subs fragment (count token)) index))}
         :extensions {fkey (-> config :formatters fkey)}})
       (rest fragments)
      (cons inky processed))))


(defn reassemble-text
  "Reassemble these processed strings into a complete text, and process it as
  Markdown."
  [result processed]
  (assoc result :text
    (local-links
      (md/md-to-html-string
        (cs/join "\n\n" (reverse processed))
        :heading-anchors true))))


(defn reintegrate-inclusions
  "Given a map of the form produced by `process-text`, return a string of HTML text
  with the inclusions (if any) reintegrated."
  ([processed-text]
   (assoc
     processed-text
     :content
     (reintegrate-inclusions
       (:inclusions processed-text)
       (:text processed-text))))
  ([inclusions text]
   (let [ks (keys inclusions)]
     (if (empty? (keys inclusions))
       ;; TODO: this is one opportunity to add scripts at the end of the
       ;; constructed text. I've a feeling that that would be a mistake and
       ;; that instead we should hand back a map comprising the text and the
       ;; keys of the extensions
       text
       (let [kw (first ks)]
         (reintegrate-inclusions
           (dissoc inclusions kw)
           (cs/replace
             text
             (str kw)
             (cs/replace (kw inclusions) "\\/" "/"))))))))


(defn process-text
  [^Integer index ^clojure.lang.Associative result fragments processed]
  (let [fragment (first fragments)
        ;; if I didn't find a formatter for a back-tick marked fragment,
        ;; I need to put the backticks back in.
        remarked (if (odd? index) (str "```" fragment "\n```") fragment)
        first-token (get-first-token fragment)
        kw (if-not (empty? first-token) (keyword first-token))
        formatter (if
                    kw
                    (try
                      (read-string (-> config :formatters kw :formatter))
                      (catch Exception _
                        (do
                          (log/info "No formatter found for extension `" kw "`")
                          ;; no extension registered - there sometimes won't be,
                          ;; and it doesn't matter
                          nil))))]
    (cond
      (empty? fragments)
      ;; We've come to the end of the list of fragments. Reassemble them into
      ;; a single HTML text and pass it back.
      (reassemble-text result processed)
      formatter
      (apply-formatter index result fragments processed fragment first-token formatter)
      true
      (process-markdown-fragment index result remarked (rest fragments) processed))))

(defn md->html
  "Process this `text`, assumed to be markdown potentially containing both local links
  and YAML visualisation specifications, and return a map comprising JSON visualisation
  specification, and HTML text with markers for where those should be reinserted.

  The map has two top-level keys: `:inclusions`, a map of constructed keywords to
  inclusion specifications, and `:text`, an HTML text string with the keywords
  present where the corresponding inclusion should be inserted."
  [^clojure.lang.Associative context]
   (reintegrate-inclusions
     (process-text
       0
       (assoc context :extensions #{})
       (cs/split (or (:source context) "") #"```")
       '())))


;; (def first-token "pswp")
;; (def kw (keyword "pswp"))
;; (def fragment "pswp
;;   ![Frost on a gate, Laurieston](content/uploads/g1.jpg)
;;   ![Feathered crystals on snow surface, Taliesin](content/uploads/g2.jpg)
;;   ![Feathered snow on log, Taliesin](content/uploads/g3.jpg)
;;   ![Crystaline growth on seed head, Taliesin](content/uploads/g4.jpg)")
;; (def index 0)
;; (def formatter (read-string (-> config :formatters kw :formatter)))
;; formatter
;; (eval (list formatter (subs fragment (count first-token)) index))
;; (process-photoswipe (subs fragment (count first-token)) index)

;; (process-text
;;   {:source "pswp
;;   ![Frost on a gate, Laurieston](content/uploads/g1.jpg)
;;   ![Feathered crystals on snow surface, Taliesin](content/uploads/g2.jpg)
;;   ![Feathered snow on log, Taliesin](content/uploads/g3.jpg)
;;   ![Crystaline growth on seed head, Taliesin](content/uploads/g4.jpg)"} )

;; (process-text {:source (slurp (clojure.java.io/file smeagol.util/content-dir "Extensible Markup.md"))})





