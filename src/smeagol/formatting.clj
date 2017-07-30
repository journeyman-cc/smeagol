(ns ^{:doc "Format Semagol's enhanced markdown format."
      :author "Simon Brooke"}
  smeagol.formatting
  (:require [clojure.string :as cs]
            [cemerick.url :refer (url url-encode url-decode)]
            [noir.io :as io]
            [noir.session :as session]
            [markdown.core :as md]
            [smeagol.authenticate :as auth]
            [clj-yaml.core :as yaml]
            [clojure.data.json :as json]))

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
;;;; both YAML and JSON notation. So we need to extract the visualisation YAML
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


(defn yaml->vis
  "Transcode this YAML fragment into the source for a Vega visualisation with this index."
  [^String yaml-src ^Integer index]
  (str
    "<div class='data-visualisation' id='vis" index "'></div>\n"
    "<script>\n//<![CDATA[\nvar vl"
    index
    " = "
    (json/write-str
      (assoc (yaml/parse-string yaml-src) (keyword "$schema") "https://vega.github.io/schema/vega-lite/v2.json"))
    ";\nvega.embed('#vis" index "', vl" index ");\n//]]\n</script>"))


(defn process-text
  "Process this `text`, assumed to be markdown potentially containing both local links
  and YAML visualisation specifications, and return a map comprising JSON visualisation
  specification, and HTML text with markers for where those should be reinserted.

  The map has two top-level keys: `:visualisations`, a map of constructed keywords to
  visualisation specifications, and `:text`, an HTML text string with the keywords
  present where the corresponding visualisation should be inserted."
  ([text]
   (process-text 0 {:visualisations {}} (cs/split text #"```") '()))
  ([index result fragments processed]
   (cond
     (empty? fragments)
     (assoc result :text
       (local-links
         (md/md-to-html-string
           (cs/join "\n\n" (reverse processed))
           :heading-anchors true)))
     (clojure.string/starts-with? (first fragments) "vis")
     (let [kw (keyword (str "visualisation-" index))]
       (process-text
         (+ index 1)
         (assoc
           result
           :visualisations
           (assoc
             (:visualisations result)
             kw
             (yaml->vis
               (subs (first fragments) 3)
               index)))
         (rest fragments)
         (cons kw processed)))
     true
     (process-text (+ index 1) result (rest fragments) (cons (first fragments) processed)))))


(defn reintegrate-visualisations
  "Given a map of the form produced by `process-text`, return a string of HTML text
  with the visualisations (if any) reintegrated."
  ([processed-text]
   (reintegrate-visualisations (:visualisations processed-text) (:text processed-text)))
  ([visualisations text]
   (let [ks (keys visualisations)]
     (if (empty? (keys visualisations))
       text
       (let [kw (first ks)]
         (reintegrate-visualisations
           (dissoc visualisations kw)
           (cs/replace
             text
             (str kw)
             (cs/replace (kw visualisations) "\\/" "/"))))))))


(defn md->html
  "Take this markdown source, and return HTML."
  [md-src]
  (reintegrate-visualisations (process-text md-src)))


