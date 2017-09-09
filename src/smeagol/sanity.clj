(ns ^{:doc "Functions related to sanity checks and error reporting in conditions where the environment may not be sane."
      :author "Simon Brooke"}
  smeagol.sanity
  (:require [clojure.java.io :as cjio]
            [clojure.string :as s]
            [compojure.response :refer [Renderable]]
            [environ.core :refer [env]]
            [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [content-type response]]
            [selmer.parser :as parser]
            [smeagol.configuration :refer [config]]
            [smeagol.util :as util]
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

(defn check-content-dir []
  (try
    (let [directory (cjio/as-file (cjio/resource util/content-dir))]
      (if
        (.isDirectory directory)
        true
        (throw (Exception. (str "Content directory '" util/content-dir "' is not a directory"))))
      (if
        (.canWrite directory)
        true
        (throw (Exception. (str "Content directory '" util/content-dir "' is not writable")))))
    (catch Exception any
      (throw (Exception. (str "Content directory '" util/content-dir "' does not exist") any))))
  (try
    (slurp (cjio/resource (str util/content-dir java.io.File/separator "_side-bar.md")))
    (timbre/info "Content directory '" util/content-dir "' check completed.")
    (catch Exception any
      (throw (Exception. (str "Content directory '" util/content-dir "' is not initialised") any)))))


(defn- raw-sanity-check-installation []
  (check-content-dir)
  (config :test))


(defn sanity-check-installation []
  (timbre/info "Running sanity check")
  (check-content-dir)
  (config :test)
  (timbre/info "Sanity check completed"))


;;(def sanity-check-installation (memoize raw-sanity-check-installation))


(defn- get-causes [any]
  (if
    (instance? Exception any)
    (cons any (get-causes (.getCause any)))
    '()))


(defn show-sanity-check-error
  "Generate an error page in a way which should work even when everything else is broken.
  If no argument is passed, run the sanity check and if it fails return page contents;
  if `error` is passed, just return page content describing the error."
  ([error]
   (html
     [:html
      [:head
       [:title "Smeagol is not initialised correctly"]
       [:link {:href "/content/stylesheet.css" :rel "stylesheet"}]]
      [:body
       [:header
        [:h1 "Smeagol is not initialised correctly"]]
       [:div {:id "error"}
        [:p {:class "error"} (.getMessage error)]]
       [:p "There was a problem launching Smeagol probably because of misconfiguration:"]
       (apply
         vector
         (cons :ol
               (map #(vector :li (.getMessage %))
                    (get-causes error))))
       [:p "For more information please see documentation "
        [:a {:href "https://github.com/journeyman-cc/smeagol/blob/develop/resources/public/content/Deploying%20Smeagol.md"} "here"]]]]))
  ([]
   (try
     (sanity-check-installation)
     nil
     (catch Exception any (show-sanity-check-error any)))))
