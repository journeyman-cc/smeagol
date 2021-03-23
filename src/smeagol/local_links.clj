(ns ^{:doc "Format Semagol's local links."
      :author "Simon Brooke"}
  smeagol.local-links
  (:require [clojure.string :as cs]
            [cemerick.url :refer (url-encode)]))

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

;; Error to show if text to be rendered is nil.
;; TODO: this should go through i18n, but for that to happen we need the
;; request passed through to here.
(def no-text-error "No text: does the file exist?")


(defn local-links
  "Rewrite text in `html-src` surrounded by double square brackets as a local link into this wiki."
  [^String html-src]
  (if html-src
    (cs/replace html-src #"\[\[[^\[\]]*\]\]"
                #(let [text (cs/replace %1 #"[\[\]]" "")
                       encoded (url-encode text)
                       ;; I use '\_' to represent '_' in wiki markup, because
                       ;; '_' is meaningful in Markdown. However, this needs to
                       ;; be stripped out when interpreting local links.
                       munged (cs/replace encoded #"%26%2395%3B" "_")]
                   (format "<a href='wiki?page=%s'>%s</a>" munged text)))
    no-text-error))


