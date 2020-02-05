(ns ^{:doc "Miscellaneous utility functions supporting Smeagol."
      :author "Simon Brooke"}
  smeagol.util
  (:require [clojure.java.io :as cjio]
            [environ.core :refer [env]]
            [noir.io :as io]
            [noir.session :as session]
            [scot.weft.i18n.core :as i18n]
            [smeagol.authenticate :as auth]
            [smeagol.configuration :refer [config]]
            [smeagol.formatting :refer [md->html]]
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
;;;; Copyright (C) 2014 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def start-page
  (:start-page  config))

(def content-dir
  (or
    (:content-dir config)
    (cjio/file (io/resource-path) "content")))


(defn standard-params
  "Return a map of standard parameters to pass to the template renderer."
  [request]
  (let [user (session/get :user)]
    {:user user
     :admin (auth/get-admin user)
     :side-bar (md->html (slurp (cjio/file content-dir "_side-bar.md")))
     :header (md->html (slurp (cjio/file content-dir "_header.md")))
     :version (System/getProperty "smeagol.version")}))


(defn- raw-get-messages
  "Return the most acceptable messages collection we have given the
  `Accept-Language` header in this `request`."
  [request]
  (let [specifier ((:headers request) "accept-language")
        messages (try
                   (i18n/get-messages specifier "i18n" "en-GB")
                   (catch Exception any
                     (timbre/error
                       any
                       (str
                         "Failed to parse accept-language header '"
                         specifier
                         "'"))
                     {}))]
    (merge
      messages
      config)))


(def get-messages (memoize raw-get-messages))


(defn get-message
  "Return the message with this `message-key` from this `request`.
   if not found, return this `default`, if provided; else return the
   `message-key`."
  ([message-key request]
   (get-message message-key message-key request))
  ([message-key default request]
   (let [messages (get-messages request)]
     (if
       (map? messages)
       (or (messages message-key) default)
       default))))
