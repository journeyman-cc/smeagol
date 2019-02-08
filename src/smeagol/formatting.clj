(ns ^{:doc "Format Semagol's enhanced markdown format."
      :author "Simon Brooke"}
  smeagol.formatting
  (:require [clojure.data.json :as json]
            [clojure.string :as cs]
            [cemerick.url :refer (url url-encode url-decode)]
            [clj-yaml.core :as yaml]
            [markdown.core :as md]
            [smeagol.testing :as testing]
            [smeagol.configuration :refer [config]]))

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

;; Error to show if text to be rendered is nil.
(def no-text-error "No text: does the file exist?")


(defn local-links
  "Rewrite text in `html-src` surrounded by double square brackets as a local link into this wiki."
  [^String html-src]
  (if html-src
    (cs/replace html-src #"\[\[[^\[\]]*\]\]"
                #(let [text (clojure.string/replace %1 #"[\[\]]" "")
                       encoded (url-encode text)
                       ;; I use '\_' to represent '_' in wiki markup, because
                       ;; '_' is meaningful in Markdown. However, this needs to
                       ;; be stripped out when interpreting local links.
                       munged (cs/replace encoded #"%26%2395%3B" "_")]
                   (format "<a href='wiki?page=%s'>%s</a>" munged text)))
    no-text-error))


(defn yaml->json
  "Rewrite this string, assumed to be in YAML format, as JSON."
  [^String yaml-src]
  (json/write-str (yaml/parse-string yaml-src)))


(declare process-text)


(defn process-vega
  "Process this `vega-src` string, assumed to be in YAML format, into a specification
  of a Vega chart, and add the plumbing to render it."
  [^String vega-src ^Integer index]
  (str
    "<div class='data-visualisation' id='vis" index "'></div>\n"
    "<script>\n//<![CDATA[\nvar vl"
    index
    " = "
    (yaml->json (str "$schema: https://vega.github.io/schema/vega-lite/v2.json\n" vega-src))
    ";\nvega.embed('#vis"
    index
    "', vl"
    index
    ");\n//]]\n</script>"))


(defn process-mermaid
  "Lightly mung this `graph-spec`, assumed to be a mermaid specification."
  [^String graph-spec ^Integer index]
  (str "<div class=\"mermaid data-visualisation\">\n"
       graph-spec
       "\n</div>"))


(defn process-backticks
  "Effectively, escape the backticks surrounding this `text`, by protecting them
  from the `md->html` filter."
  [^String text ^Integer index]
  (str "<pre class=\"backticks\">```" (.trim text) "\n```</pre>"))


(defn process-test
  "Takes at least 3 lines assuming first is a fn name, last is output, rest inside are arguments"
  [^String text ^Integer index]
  (testing/process text index))


(defn get-first-token
  "Return the first space-separated token of this `string`."
  [^String string]
  (if string (first (cs/split string #"[^a-zA-Z0-9]+"))))


(defn- process-markdown-fragment
  "Within the context of `process-text`, process a fragment believed to be markdown.

  As with `process-text`, this function returns a map with two top-level keys:
  `:inclusions`, a map of constructed keywords to inclusion specifications,
  and `:text`, an HTML text string with the keywords present where the
  corresponding inclusion should be inserted."
  [index result fragment fragments processed]
  (process-text
    (inc index)
    result
    fragments
    (cons fragment processed)))


(defn- apply-formatter
  "Within the context of `process-text`, process a fragment for which an explicit
  §formatter has been identified.

  As with `process-text`, this function returns a map with two top-level keys:
  `:inclusions`, a map of constructed keywords to inclusion specifications,
  and `:text`, an HTML text string with the keywords present where the
  corresponding inclusion should be inserted."
  [index result fragments processed fragment token formatter]
  (let
    [kw (keyword (str "inclusion-" index))]
    (process-text
      (inc index)
      (assoc-in result [:inclusions kw] (apply formatter (list (subs fragment (count token)) index)))
      (rest fragments)
      (cons kw processed))))


(defn process-text
  "Process this `text`, assumed to be markdown potentially containing both local links
  and YAML visualisation specifications, and return a map comprising JSON visualisation
  specification, and HTML text with markers for where those should be reinserted.

  The map has two top-level keys: `:inclusions`, a map of constructed keywords to
  inclusion specifications, and `:text`, an HTML text string with the keywords
  present where the corresponding inclusion should be inserted."
  ([^String text]
   (process-text 0 {:inclusions {}} (cs/split (or text "") #"```") '()))
  ([index result fragments processed]
   (let [fragment (first fragments)
         ;; if I didn't find a formatter for a back-tick marked fragment,
         ;; I need to put the backticks back in.
         remarked (if (odd? index) (str "```" fragment "\n```") fragment)
         first-token (get-first-token fragment)
         formatter (eval ((:formatters config) first-token))]
     (cond
       (empty? fragments)
       (assoc result :text
         (local-links
           (md/md-to-html-string
             (cs/join "\n\n" (reverse processed))
             :heading-anchors true)))
       formatter
       (apply-formatter index result fragments processed fragment first-token formatter)
       true
       (process-markdown-fragment index result remarked (rest fragments) processed)))))


(defn reintegrate-inclusions
  "Given a map of the form produced by `process-text`, return a string of HTML text
  with the inclusions (if any) reintegrated."
  ([processed-text]
   (reintegrate-inclusions (:inclusions processed-text) (:text processed-text)))
  ([inclusions text]
   (let [ks (keys inclusions)]
     (if (empty? (keys inclusions))
       text
       (let [kw (first ks)]
         (reintegrate-inclusions
           (dissoc inclusions kw)
           (cs/replace
             text
             (str kw)
             (cs/replace (kw inclusions) "\\/" "/"))))))))


(defn md->html
  "Take this markdown source, and return HTML."
  [md-src]
  (reintegrate-inclusions (process-text md-src)))


