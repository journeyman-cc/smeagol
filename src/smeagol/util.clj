(ns ^{:doc "Miscellaneous utility functions supporting Smeagol."
      :author "Simon Brooke"}
  smeagol.util
  (:require [clojure.java.io :as cjio]
            [clojure.string :as cs]
            [markdown.core :as md]
            [me.raynes.fs :as fs]
            [noir.io :as io]
            [noir.session :as session]
            [scot.weft.i18n.core :as i18n]
            [smeagol.authenticate :as auth]
            [smeagol.configuration :refer [config]]
            [smeagol.local-links :refer [local-links]]
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


(def start-page
  "The page to load on startup, taken from configuration."
  (:start-page  config))

(def content-dir
  "The absolute path to the directory in which Wiki content (i.e., Markdown
  files) are stored."
  (str
    (fs/absolute
      (or
        (:content-dir config)
        (cjio/file (io/resource-path) "content")))))

(def upload-dir
  "The absolute path to the directory in which uploaded files are stored."
  (str (cjio/file content-dir "uploads")))

(def local-url-base
  "Essentially, the slash-terminated absolute path of the `public` resource
  directory."
  (let [a (str (fs/absolute content-dir))]
    (subs a 0 (- (count a) (count "content")))))

;; (def local-url-base
;;   "Essentially, the slash-terminated absolute path of the `public` resource
;;   directory. **NOTE** that this MAY NOT contain `content-dir`."
;;   (cjio/file (io/resource-path)))


(defn not-servable-reason
  "As a string, the reason this `file-path` cannot safely be served, or `nil`
  if it is safe to serve. This reason may be logged, but should *not* be
  shown to remote users, as it would allow file system probing."
  [file-path]
  (try
  (let [path (if
               (cs/starts-with? (str file-path) "/")
               file-path
               (cjio/file local-url-base file-path))]
    (cond
      (cs/includes? file-path "..")
      (cs/join " " [file-path
               "Attempts to ascend the file hierarchy are disallowed."])
      (not (cs/starts-with? path local-url-base))
      (cs/join " " [path "is not servable"])
      (not (fs/exists? path))
      (cs/join " " [path "does not exist"])
      (not (fs/readable? path))
      (cs/join " " [path "is not readable"])))
    (catch Exception any (cs/join " " [file-path "is not servable because" (.getMessage any)]))))


;; (not-servable-reason "/home/simon/workspace/smeagol/resources/public/content/vendor/node_modules/photoswipe/dist/photoswipe.min.js")
;; (not-servable-reason "/root/froboz")

(defn local-url?
  "True if this `file-path` can be served as a local URL, else false."
  [file-path]
  (try
    (if
      (empty? (not-servable-reason file-path))
      true
      (do
        (log/error
          "In `smeagol.util/local-url? `" file-path "` is not a servable resource.")
        false))
    (catch Exception any
      (log/error
        "In `smeagol.util/local-url `" file-path "` is not a servable resource:" any)
      false)))

(defn local-url
  "Return a local URL for this `file-path`, or a deliberate 404 if none
  can be safely served."
  ;; TODO: this actually returns a relative URL relative to local-url-base.
  ;; That's not quite what we want because in Tomcat contexts the absolute
  ;; URL may be different. We *ought* to be able to extract the offset from the
  ;; servlet context, but it may be simpler to jam it in the config.
  [file-path]
  (try
    (let [path (if
                 (cs/starts-with? file-path local-url-base)
                 (subs file-path (count local-url-base))
                 file-path)
          problem (not-servable-reason path)]
      (if
        (empty? problem)
        path
        (do
          (log/error
            "In `smeagol.util/local-url `" file-path "` is not a servable resource.")
          file-path)))
    (catch Exception any
      (log/error
        "In `smeagol.util/local-url `" file-path "` is not a servable resource:" any)
      file-path)))

;; (local-url? "vendor/node_modules/photoswipe/dist/photoswipe.min.js")
;; (local-url? "/home/simon/workspace/smeagol/resources/public/vendor/node_modules/photoswipe/dist/photoswipe.min.js")

(defn standard-params
  "Return a map of standard parameters to pass to the template renderer."
  [request]
  (let [user (session/get :user)]
    {:user user
     :admin (auth/get-admin user)
     :js-from (:js-from config)
     :side-bar (md/md-to-html-string
                 (local-links
                   (slurp (cjio/file content-dir "_side-bar.md")))
                 :heading-anchors true)
     :header (md/md-to-html-string
               (local-links
                 (slurp (cjio/file content-dir "_header.md")))
                 :heading-anchors true)
     :version (System/getProperty "smeagol.version")}))


(defn get-servlet-context-path
  [request]
  (if-let [context (:servlet-context request)]
    ;; If we're not inside a serlvet environment (for
    ;; example when using mock requests), then
    ;; .getContextPath might not exist
    (try
      (let [path (.getContextPath context)
            path' (if-not (cs/ends-with? path "/")
                    (str path "/")
                    path)]
        path')
      (catch IllegalArgumentException _ context))))


(def get-messages
  "Return the most acceptable messages collection we have given the
  `Accept-Language` header in this `request`."
  (memoize
    (fn [request]
      (let [specifier ((:headers request) "accept-language")
            messages (try
                       (i18n/get-messages specifier "i18n" "en-GB")
                       (catch Exception any
                         (log/error
                           any
                           (str
                             "Failed to parse accept-language header '"
                             specifier
                             "'"))
                         {}))]
    (merge
      messages
      config)))))


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
