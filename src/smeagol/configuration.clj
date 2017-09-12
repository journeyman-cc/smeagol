(ns ^{:doc "Read and make available configuration."
      :author "Simon Brooke"}
  smeagol.configuration
  (:require [environ.core :refer [env]]
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
;;;;
;;;; Right, doing the data visualisation thing is tricky. Doing it in the
;;;; pipeline doesn't work, because the md-to-html-string filter messes up
;;;; both YAML and JSON notation. So we need to extract the visualisation
;;;; fragments from the Markdown text and replace them with tokens we will
;;;; recognise afterwards, perform md-to-html-string, and then replace our
;;;; tokens with the transformed visualisation specification.
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def config-file-path
  "The relative path to the config file."
  (or
    (env :smeagol-config)
    (str (io/resource-path) "../config.edn")))


(defn- from-env-vars
  "Read a map from those of these environment variables which have values"
  [& vars]
  (reduce
    #(let [v (env %2)]
       (if v (assoc %1 %2 v) %1))
    {}
    vars))


(defn transform-map
  "transform this map `m` by applying these `transforms`. Each transforms
  is expected to comprise a map with the keys :from and :to, whose values
  are respectively a key to match and a key to replace that match with,
  and optionally a key :transform, whose value is a function of one
  argument to be used to transform the value of that key."
  [m tuples]
  (reduce
    (fn [m tuple]
      (if
        (and (map? tuple) (map? m) (m (:from tuple)))
        (let [old-val (m (:from tuple))
              t (:transform tuple)
              new-val (if t (apply t (list old-val)) old-val)]
          (assoc (dissoc m (:from tuple)) (:to tuple) new-val))
        m))
    m
    tuples))


(def config-env-transforms
  "Transforms to use with `transform-map` to convert environment
  variable names (which need to be specific) into the shorter names
  used internally"
  '( {:from :smeagol-site-title :to :site-title}
     {:from :smeagol-default-locale :to :default-locale}
     {:from :smeagol-formatters :to :formatters :transform read-string}
     {:from :smeagol-content-dir :to :content-dir}
     {:from :smeagol-passwd :to :passwd}
     {:from :smeagol-log-level :to :log-level :transform (fn [s] (keyword (lower-case s)))}))


(def config
  "The actual configuration, as a map. The idea here is that the config
  file is read (if it is specified and present), but that individual
  values "
  (try
    (let [file-contents (try
                          (read-string (slurp config-file-path))
                          (catch Exception _ {}))]
      (merge
        file-contents
        (transform-map
          (from-env-vars :smeagol-site-title :smeagol-default-locale)
          config-env-transforms)))
    (catch Exception any
      (timbre/error any "Could not load configuration")
      {})))
