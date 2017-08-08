(ns ^{:doc "Miscellaneous utility functions supporting Smeagol."
      :author "Simon Brooke"}
  smeagol.util
  (:require [noir.session :as session]
            [noir.io :as io]
            [scot.weft.i18n.core :as i18n]
            [taoensso.timbre :as timbre]
            [smeagol.authenticate :as auth]
            [smeagol.configuration :refer [config]]
            [smeagol.formatting :refer [md->html]]))

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


(defn standard-params
  "Return a map of standard parameters to pass to the template renderer."
  [request]
  (let [user (session/get :user)]
    {:user user
     :admin (auth/get-admin user)
     :side-bar (md->html (io/slurp-resource "/content/_side-bar.md"))
     :header (md->html (io/slurp-resource "/content/_header.md"))
     :version (System/getProperty "smeagol.version")}))


(defn raw-get-messages
  "Return the most acceptable messages collection we have given the
  `Accept-Language` header in this `request`."
  [request]
  (merge
    (i18n/get-messages
      ((:headers request) "accept-language")
      (str (io/resource-path) "../i18n")
      "en-GB")
    config))


(def get-messages (memoize raw-get-messages))


(defn get-message
  [message-key request]
  (let [messages (get-messages request)]
    (if
      (map? messages)
      (or (messages message-key) message-key)
      message-key)))

