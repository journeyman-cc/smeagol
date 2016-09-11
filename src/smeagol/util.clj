(ns ^{:doc "Miscellaneous utility functions supporting Smeagol."
      :author "Simon Brooke"}
  smeagol.util
  (:require [clojure.string :as cs]
            [cemerick.url :refer (url url-encode url-decode)]
            [noir.io :as io]
            [noir.session :as session]
            [markdown.core :as md]
            [smeagol.authenticate :as auth]))

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
;;;; Copyright (C) 2014 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (md/md-to-html-string (io/slurp-resource filename)))


(defn local-links
  "Rewrite text in `html-src` surrounded by double square brackets as a local link into this wiki."
  [^String html-src]
  (cs/replace html-src #"\[\[[^\[\]]*\]\]"
              #(let [text (clojure.string/replace %1 #"[\[\]]" "")
                     encoded (url-encode text)
                     ;; I use '\_' to represent '_' in wiki markup, because
                     ;; '_' is meaningful in Markdown. However, this needs to
                     ;; be stripped out when interpreting local links.
                     munged (cs/replace encoded #"%26%2395%3B" "_")]
                 (format "<a href='wiki?page=%s'>%s</a>" munged text))))


(defn standard-params
  "Return a map of standard parameters to pass to the template renderer."
  [request]
  (let [user (session/get :user)]
    {:user user
     :admin (auth/get-admin user)
     :side-bar (local-links (md->html "/content/_side-bar.md"))
     :header (local-links (md->html "/content/_header.md"))
     :version (System/getProperty "smeagol.version")}))

