
(ns ^{:doc "Render a page as HTML."
      :author "Simon Brooke"}
  smeagol.layout
  (:require [clojure.java.io :as cjio]
            [clojure.string :as s]
            [compojure.response :refer [Renderable]]
            [environ.core :refer [env]]
            [hiccup.core :refer [html]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [content-type response]]
            [selmer.parser :as parser]
            [smeagol.configuration :refer [config]]
            [smeagol.sanity :refer :all]
            [smeagol.util :as util]))

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

(def template-path
  "Path to the resource directory in which Selmer templates are stored. These
  should be in a place which is not editable from the Wiki, otherwise
  users may break things which they cannot subsequently fix!"
  "templates/")

(parser/add-tag! :csrf-field (fn [_ _] (anti-forgery-field)))

;; Attempt to do internationalisation more neatly
;; This tag takes two arguments, the first is a key, the (optional) second is a
;; default. The key is looked up in the i18n
(parser/add-tag! :i18n
  (fn [args context-map]
    (let [messages (:i18n context-map)
          default (or (second args) (first args))]
      (if (map? messages) (or (messages (keyword (first args))) default)
        default))))


(deftype RenderableTemplate
;;   Boilerplate from Luminus. Load a template file into an object which may
;;   be rendered.
  [template params]
  Renderable
  (render [this request]
          (try
            (content-type
              (->> (assoc params
                     (keyword (s/replace template #".html" "-selected")) "active"
                     :i18n (util/get-messages request)
                     :dev (env :dev)
                     :servlet-context
                     (if-let [context (:servlet-context request)]
                       ;; If we're not inside a serlvet environment (for
                       ;; example when using mock requests), then
                       ;; .getContextPath might not exist
                       (try (.getContextPath context)
                         (catch IllegalArgumentException _ context))))
                   (parser/render-file (str template-path template))
                   response)
              "text/html; charset=utf-8")
            (catch Exception any
              (show-sanity-check-error any)))))


(defn render
  "Boilerplate from Luminus. Render an HTML page based on this `template` and
  these `params`. Returns HTML source as a string."
  [template & [params]]
  (try
    (RenderableTemplate. template params)
    (catch Exception any
      (show-sanity-check-error any))))

