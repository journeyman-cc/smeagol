(ns ^{:doc "Handle file uploads."
      :author "Simon Brooke"}
  smeagol.uploads
  (:import [java.io File])
  (:require [clojure.string :as cs]
            [noir.io :as io]
            [taoensso.timbre :as timbre]))

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

;; No longer used as uploaded files now go into Git.
;; (defn avoid-name-collisions
;;   "Find a filename within this `path`, based on this `file-name`, that does not
;;    reference an existing file. It is assumed that `path` ends with a path separator.
;;    Returns a filename hwich does not currently reference a file within the path."
;;   [path file-name]
;;   (if (.exists (File. (str path file-name)))
;;     (let [parts (cs/split file-name #"\.")
;;           prefix (cs/join "." (butlast parts))
;;           suffix (last parts)]
;;       (first
;;        (filter #(not (.exists (File. (str path %))))
;;                (map #(str prefix "." % "." suffix) (range)))))
;;     file-name))


(defn store-upload
  "Store an upload both to the file system and to the database.
  The issue with storing an upload is moving it into place.
  If `params` are passed as a map, it is expected that this is a map from
  an HTTP POST operation of a form with type `multipart/form-data`."
  [params path]
  (let [upload (:upload params)
        tmp-file (:tempfile upload)
        filename (:filename upload)]
    (timbre/info
      (str "Storing upload file: " upload))
      (if tmp-file
        (do
          (.renameTo tmp-file
                     (File. (str path filename)))
          filename)
        (throw (Exception. "No file found?")))))
