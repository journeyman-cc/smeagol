(ns ^{:doc "Functions related to sanity checks and error reporting in conditions
      where the environment may not be sane."
      :author "Simon Brooke"}
  smeagol.sanity
  (:import (java.util Locale))
  (:require [clojure.java.io :as cjio]
            [clojure.string :as string]
            [hiccup.core :refer [html]]
            [scot.weft.i18n.core :as i18n]
            [smeagol.authenticate :refer [password-file-path]]
            [smeagol.configuration :refer [config-file-path config]]
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


;; The general idea behind the 'check-' functions in this file is that, if the
;; check passes, they return true; if it fails, they return a map of problems found.
;; the map comprises keys bound to 'explanation' lists of keywords and strings. If
;; internationalisation is available, the keywords will then be translated into
;; localised strings for presentation to the user; but if it isn't available,
;; the keywords need to be human readable. Sanity checking ought to work even
;; when the installation is quite badly broken.

(defn check-exists
  "Check this `path` exists. If so, return `true`; if not, return a map
  containing this `problem-key` bound to a list explaining the problem."
  [path problem-key]
  (if-not
    (.exists (cjio/as-file path))
    {problem-key (list :file-or-directory path :does-not-exist)}
    true))


(defn check-is-dir
  "Check this `path` is a directory. If so, return `true`; if not, return a map
  containing this `problem-key` bound to a list explaining the problem."
  [path problem-key]
  (if-not
    (.isDirectory (cjio/as-file path))
    {problem-key (list :file-or-directory path :is-not-directory)}
    true))


(defn check-can-write
  "Check this `path` is writable. If so, return `true`; if not, return a map
  containing this `problem-key` bound to a list explaining the problem."
  [path problem-key]
  (if-not
    (.canWrite (cjio/as-file path))
    {problem-key (list :file-or-directory path :is-not-writable)}
    true))


(defn check-can-read
  "Check this `path` is readable. If so, return `true`; if not, return a map
  containing this `problem-key` bound to a list explaining the problem."
  [path problem-key]
  (if-not
    (.canRead (cjio/as-file path))
    {problem-key (list :file-or-directory path :is-not-readable)}
    true))


(defn check-with-protection
  "Apply this `check` to this `path` and `problem-key`; if no exception is thrown, return
  the result. If an exception is thrown, return a map comprising a problem-key bound to
  an explanation which includes the exception."
  [check problem-key & args]
  (try
    (apply check args)
    (catch Exception ex
      {problem-key (list problem-key args ex)})))


(defn compound-check-results
  [& results]
  (let [problems (remove true? results)]
    (if (empty? problems) true (apply merge problems))))


(defn check-can-read-and-write
  "Check this `path` is both readable and writable. If so, return `true`;
  if not, return a map containing this `problem-key` bound to a list explaining
  the problem."
  [path problem-key]
  (compound-check-results
    (check-with-protection check-exists :file-or-directory path (keyword (str (name problem-key) "-exists")))
    (check-with-protection check-can-read :file-or-directory path (keyword (str (name problem-key) "-can-read")))
    (check-with-protection check-can-write :file-or-directory path (keyword (str (name problem-key) "-can-write")))))


(defn check-content-dir
  "Check that `path` exists and is populated as a valid content directory. Return true
  if so, else a map of all problems found. If `path` is not supplied, default to the
  configured content directory."
  ([path]
  (compound-check-results
    (check-with-protection check-exists :file-or-directory path :content-dir-exists)
    (check-with-protection check-is-dir :file-or-directory path :content-dir-is-dir)
    (check-can-read-and-write path :content-dir)
    (apply compound-check-results
      (map
        #(check-can-read-and-write
           (cjio/file path (str "_" % ".md"))
           %)
        ["side-bar" "edit-side-bar" "header" ]))))
  ([]
   (check-content-dir util/content-dir)))


(defn check-password-member-field
  "Check that this `member` map, expected to be an entry from the passwd
  file whose key was `user-key`, has this `field` and if not return a
  problem explanation with this `problem-key`."
  [member field user-key problem-key]
  (if
    (and (map? member) (member field))
    true
    {problem-key (list :user-lacks-field user-key field)}))


(defn check-password-member
  "Check that this `member` map, expected to be an entry from the passwd
  file whose key was `user-key`, has all the required fields and if not
  return a problem explanation with this `problem-key`."
  [member user-key problem-key]
  (apply
    compound-check-results
    (map
      #(check-password-member-field
         member
         %
         user-key
         (keyword
           (string/join
             "-"
             (list
                 (name problem-key)
                 (name user-key)
                 (name %)))))
      [:email :password])))


(defn check-password-members
  "Check that all entries in this `passwd-content` have the required fields."
  [passwd-content]
  (apply
    compound-check-results
    (map
      #(check-password-member (passwd-content %) % :missing-field)
      (keys passwd-content))))


(defn check-at-least-one-admin
  "Check that there is at least one user in this `passwd-content` who has
  `:admin` set to `true`."
  [passwd-content]
  (if
    (empty?
      (remove
        nil?
        (map
          #(:admin (passwd-content %))
          (keys passwd-content))))
    {:no-admin-users '(:no-admin-users)}
    true))


(defn check-password-file
  "Check that the file at this `path` is a valid passwd file."
  [path]
  (let [content (read-string (slurp path))]
    (compound-check-results
      (check-can-read-and-write path :password-file)
      (check-password-members content)
      (check-at-least-one-admin content))))


(defn check-config
  "Check that the file at this `path` is a valid configuration file"
  [path]
  (let [content (try
                  (read-string (slurp path))
                  (catch Exception any {}))]
    (compound-check-results
      (check-with-protection check-exists :file-or-directory path :config-exists)
      (check-with-protection check-can-read :file-or-directory path :config-can-read)
      (if-not
        (:site-title content)
        {:site-title-not-configured :site-title-not-configured}
        true)
      (if-not
        (:default-locale content)
        {:default-locale-not-configured :default-locale-not-configured}
        true))))


(defn check-everything
  ([content-dir config-path passwd-path]
  (compound-check-results
    (check-content-dir content-dir)
    (check-config config-path)
    (check-password-file passwd-path)))
  ([]
   (check-everything util/content-dir config-file-path password-file-path)))


(defn- get-causes
  "Get the causes of this `error`, if it is an Exception."
  [error]
  (if
    (instance? Exception error)
    (cons error (get-causes (.getCause error)))
    '()))


;; ExplanationPart is a protocol for ensuring that everything which may form part of a
;; problem explanation can be formatted into hiccup, so that it can be converted by
;; hiccup into HTML. The reason for using Hiccup rather than Selmer is that in
;; sanity check I don't want to be dependent on the existance of templates.
;; (Also, I personally like Hiccup better, although I know it's too geeky for most
;; people)
(defprotocol ExplanationPart
  "things which may be parts of explanations need mechanisms for reducing
  themselves to natural language where possible"
  (as-hiccup [this dictionary] "Return `this` as a hiccup-formatted structure."))

(extend-protocol ExplanationPart
  nil
  (as-hiccup [this dictionary] "")

  clojure.lang.Keyword
  (as-hiccup [this dictionary]
             (str
               (or
                 (this dictionary)
                 (string/replace (name this) "-" " "))
               " "))

  clojure.lang.PersistentList
  (as-hiccup [this dictionary]
             (apply
               vector
               (cons
                 :div
                 (map #(as-hiccup % dictionary) this))))

  clojure.lang.PersistentVector
  (as-hiccup [this dictionary]
             (apply
               vector
               (cons
                 :div
                 (map #(as-hiccup % dictionary) this))))

  clojure.lang.PersistentArrayMap
  (as-hiccup [this dictionary]
             (apply
               vector
               (cons
                 :dl
                 (map
                   #(list [:dt (as-hiccup % dictionary)]
                          [:dd (as-hiccup (this %) dictionary)])
                   (keys this)))))

  clojure.lang.PersistentHashMap
  (as-hiccup [this dictionary]
             (apply
               vector
               (cons
                 :dl
                 (map
                   #(list [:dt (as-hiccup % dictionary)]
                          [:dd (as-hiccup (this %) dictionary)])
                   (keys this)))))

  java.lang.String
  (as-hiccup [this dictionary] (str this " "))

  java.lang.StackTraceElement
  (as-hiccup [this dictionary]
             [:li this])

  java.lang.Exception
  (as-hiccup [this dictionary]
             ;; OK, this is the interesting one
             (apply
               vector
               (cons
                 :div
                 (cons
                   {:class "sanity-exception"}
                   (map
                     (fn [x]
                       [:div
                        {:class "sanity-cause"}
                        [:h2 (.getMessage x)]
                        [:div {:class "sanity-stacktrace"}
                         (apply
                           vector
                           (cons
                             :ol
                             (map
                               as-hiccup
                               (.getStackTrace x)
                               dictionary)))]])
                     (get-causes this))))))
  java.lang.Object
  (as-hiccup [this dictionary] (str this " ")))


(defn get-locale-messages
  "Get messages for the server-side locale."
  []
  (let [locale (Locale/getDefault)
        locale-specifier (str (.getLanguage locale) "-" (.getCountry locale))]
    (try
      (i18n/get-messages locale-specifier "i18n" "en-GB")
      (catch Exception any {}))))


;; Prepackaged hiccup sub-units
(defn as-hiccup-head
  [messages]
  [:head
   [:title (as-hiccup :smeagol-not-initialised messages)]
   [:link {:href "/content/stylesheet.css" :rel "stylesheet"}]])


(defn as-hiccup-header
  [messages]
  [:header
   [:div {:id "nav"} "&nbsp;"]
   [:h1 (as-hiccup :smeagol-not-initialised messages)]
   [:p "&nbsp;"]])


(defn as-hiccup-see-doc
  [messages]
  [:p (as-hiccup :see-documentation messages)
   [:a
    {:href
     "https://github.com/journeyman-cc/smeagol/wiki/Deploying-Smeagol"}
    (as-hiccup :here messages)] "."])


(defn as-hiccup-footer
  [messages]
  [:footer
   [:div {:id "credits"}
    [:div
     [:img {:height "16" :width "16" :alt "one wiki to rule them all" :src "img/smeagol.png"}]
     " One Wiki to rule them all || Smeagol wiki engine || "
     [:img
      {:height "16" :width "16"
       :alt "The Web Engineering Factory &amp; Toolworks"
       :src "http://www.weft.scot/images/weft.logo.64.png"}]
     " Developed by "
     [:a {:href "http://www.weft.scot/"}"WEFT"]]]])


(defn sanity-check-report
  "Convert this `problem` report into a nicely formatted HTML page"
  [problems]
  (let [messages (get-locale-messages)]
    (html
      [:html
       (as-hiccup-head messages)
       [:body
        (as-hiccup-header messages)
        [:div {:id "error"}
         [:p {:class "error"}
          (rest (as-hiccup [(count (keys problems)) :problems-found] messages))]]
        [:div {:id "main-container" :class "sanity-check-report"}
         [:p (as-hiccup :smeagol-misconfiguration messages)]
         (as-hiccup problems messages)
         (as-hiccup-see-doc messages)]
        (as-hiccup-footer messages)]])))


(defn- raw-sanity-check-installation
  "Actually do the sanity check."
  []
  (timbre/info "Running sanity check")
  (let [result (check-everything)]
    (if
      (map? result)
      (do
        (timbre/warn "Sanity check completed; " (count (keys result)) " problem(s) found")
        (sanity-check-report result))
      (do
        (timbre/info "Sanity check completed; no problems found")
        nil))))


;; We memoise the sanity check so that although it is called for every wiki
;; page, it is only actually evaluated once.
(def sanity-check-installation (memoize raw-sanity-check-installation))


(defn show-sanity-check-error
  "Generate an error page in a way which should work even when everything else is broken.
  If no argument is passed, run the sanity check and if it fails return page contents;
  if `error` is passed, just return page content describing the error."
  ([error]
   (let [messages (get-locale-messages)]
     (html
       [:html
        (as-hiccup-head messages)
        [:body
         (as-hiccup-header messages)
         [:div {:id "error"}
          [:p {:class "error"} (.getMessage error)]]
         [:div {:id "main-container" :class "sanity-check-report"}
          [:p  (as-hiccup :smeagol-misconfiguration messages)]
          (as-hiccup error messages)
          (as-hiccup-see-doc messages)]
         (as-hiccup-footer messages)]])))
  ([]
   (try
     (sanity-check-installation)
     (catch Exception any
       (timbre/error any "Failure during sanity check")
       (show-sanity-check-error any)))))

(show-sanity-check-error (Exception. "That's insane!"))


