(ns ^{:doc "In truth, boilerplate provided by LuminusWeb."
      :author "Simon Brooke"}
  smeagol.middleware
  (:require [environ.core :refer [env]]
            [noir-exception.core :refer [wrap-internal-error]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [selmer.middleware :refer [wrap-error-page]]
            [smeagol.util :as util]
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
;;;; Copyright (C) 2014 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn log-request [handler]
  (fn [req]
    (log/debug req)
    (handler req)))


(def development-middleware
  [wrap-error-page
   wrap-exceptions])


(defn smeagol-wrap-content-type
  "Ring's `wrap-content-type` infers the content type from the *requested* file
  name. But because we fuzzy-match images, the file we return may not be the
  same type as the file that was requested, in which case we've already set
  a content type from the filename extension on the file actually served. Do not
  overwrite this!"
  [response]
  (if-not
    (and (map? (:headers response))((:headers response) "Content-Type"))
    (wrap-content-type response)
    (do
      (log/info "Content-type already set as"
                ((:headers response) "Content-Type")
                "; not overriding")
      response)))


(def production-middleware
  [#(wrap-internal-error % :log (fn [e] (log/error e)))
   #(wrap-resource % "public")
   smeagol-wrap-content-type
   #(try
      (wrap-file % util/content-dir
                 {:index-files? false :prefer-handler? true})
      (catch Exception error
        (log/fatal "Could not locate content dir" util/content-dir error)
        %))
   wrap-not-modified])


(defn load-middleware []
  (concat (when (env :dev) development-middleware)
          production-middleware))
