(ns ^{:doc "Utility functions useful to extension processors."
      :author "Simon Brooke"}
  smeagol.extensions.utils
  (:require [cemerick.url :refer (url)]
            [clj-yaml.core :as yaml]
            [clojure.data.json :as json]
            [clojure.java.io :as cjio]
            [clojure.string :as cs]
            [me.raynes.fs :as fs]
            [noir.io :as io]
            [taoensso.timbre :as log]
            [smeagol.util :refer [upload-dir]]))

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

(def resource-url-or-data->data
  "Interpret this `resource-url-or-data` string as data to be digested by a
  `process-extension` function. It may be a URL or the pathname of a local
  resource, in which case the content should be fetched; or it may just be
  the data itself.

  Returns a map with a key `:from` whose value may be `:url`, `:resource` or
  `:text`, and a key `:data` whose value is the data. There will be an
  additional key being the value of the `:from` key, whose value will be the
  source of the data."
  (memoize
    (fn [^String resource-url-or-data]
      (let [default {:from :text
                     :text resource-url-or-data
                     :data resource-url-or-data}]
        (try
          (try
            ;; is it a URL?
            (let [url (str (url resource-url-or-data))
                  result (slurp url)]
              {:from :url
               :url url
               :data result})
            (catch java.net.MalformedURLException _
              ;; no. So is it a path to a local resource?
              (let [t (cs/trim resource-url-or-data)
                    r (str (io/resource-path) t)]
                (if
                  (fs/file? r)
                  {:from :resource
                   :resource t
                   :data (slurp r)}
                  default))))
          (catch Exception x
            (log/error
              "Could not read mermaid graph specification from `"
              (cs/trim resource-url-or-data)
              "` because "
              (.getName (.getClass x))
              (.getMessage x) )
            default))))))

(defn uploaded?
  "Does this `url` string appear to be one that has been uploaded to our
  `uploads` directory?"
  [url]
  (and
    (cs/starts-with? (str url) "content/uploads")
    (fs/exists? (cjio/file upload-dir (fs/base-name url)))))

;; (uploaded? "content/uploads/g1.jpg")

(defn yaml->json
  "Rewrite this string, assumed to be in YAML format, as JSON."
  [^String yaml-src]
  (json/write-str (yaml/parse-string yaml-src)))

