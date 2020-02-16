(ns ^{:doc "Render all the main pages of a very simple Wiki engine."
      :author "Simon Brooke"}
  smeagol.routes.wiki
  (:require [cemerick.url :refer (url url-encode url-decode)]
            [clj-jgit.porcelain :as git]
            [clojure.java.io :as cjio]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as cs]
            [clojure.walk :refer :all]
            [compojure.core :refer :all]
            [java-time :as jt]
            [me.raynes.fs :as fs]
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.route :as route]
            [noir.session :as session]
            [smeagol.authenticate :as auth]
            [smeagol.configuration :refer [config]]
            [smeagol.diff2html :as d2h]
            [smeagol.formatting :refer [md->html]]
            [smeagol.history :as hist]
            [smeagol.layout :as layout]
            [smeagol.routes.admin :as admin]
            [smeagol.sanity :refer [show-sanity-check-error]]
            [smeagol.util :as util]
            [smeagol.uploads :as ul]
            [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]
            [smeagol.configuration :refer [config]]
            [smeagol.include.resolve-local-file :as resolve]
            [smeagol.include :as include]
            [smeagol.util :refer [content-dir local-url]]))

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

(defn process-source
  "Process `source-text` and save it to the specified `file-path`, committing it
  to Git and finally redirecting to wiki-page."
  [params suffix request]
  (log/trace (format "process-source: '%s'" request))
  (let [source-text (:src params)
        page (:page params)
        file-name (str page suffix)
        file-path (cjio/file util/content-dir file-name)
        exists? (.exists (cjio/as-file file-path))
        git-repo (hist/load-or-init-repo util/content-dir)
        user (session/get :user)
        email (auth/get-email user)
        summary (format "%s: %s" user (or (:summary params) "no summary"))]
    (log/info (format "Saving %s's changes ('%s') to %s in file '%s'" user summary page file-path))
    (spit file-path source-text)
    (git/git-add git-repo file-name)
    (git/git-commit git-repo summary {:name user :email email})
    (response/redirect
      (str
        "/wiki?page="
        (if
          (= suffix ".md")
          (url-encode page)
          (util/get-message :default-page-title request))))))


(defn edit-page
  "Render a page in a text-area for editing. This could have been done in the same function as wiki-page,
  and that would have been neat, but I couldn't see how to establish security if that were done."
  ([request]
   (edit-page request (util/get-message :default-page-title request) ".md" "edit.html" "_edit-side-bar.md"))
  ([request default suffix template side-bar]
   (or
     (show-sanity-check-error)
     (let [params (keywordize-keys (:params request))
           src-text (:src params)
           page (or (:page params) default)
           file-name (str page suffix)
           file-path (cjio/file util/content-dir file-name)
           exists? (.exists (cjio/as-file file-path))
           user (session/get :user)]
       (if-not
         exists?
         (log/info
           (format "File '%s' not found; creating a new file" file-path))
         (log/info (format "Opening '%s' for editing" file-path)))
       (cond src-text (process-source params suffix request)
             true
             (layout/render template
                            (merge (util/standard-params request)
                                   {:title (str (util/get-message :edit-title-prefix request) " " page)
                                    :page page
                                    :side-bar (md->html (slurp (cjio/file util/content-dir side-bar)))
                                    :content (if exists? (slurp file-path) "")
                                    :exists exists?})))))))


(defn edit-css-page
  "Render a stylesheet in a text-area for editing.."
  [request]
  (edit-page request "stylesheet" ".css" "edit-css.html" "_edit-side-bar.md"))


(def md-include-system
  "Allowing Markdown includes. Unfortunately the contributor who contributed
  this didn't document it, and I haven't yet worked out how it works. TODO:
  investigate and document."
  (component/start
    (component/system-map
      :resolver (resolve/new-resolver util/content-dir)
      :includer (component/using
                  (include/new-includer)
                  [:resolver]))))


(defn preferred-source
  "Here, `component` is expected to be a map with two keys, `:local` and
  `:remote`. If the value of `:extensions-from` in `config.edn` is remote
  AND the value of `:remote` is not nil, then the value of `:remote` will
  be returned. Otherwise, if the value of `:local` is nil and the value of
  `:remote` is non-nil, the value of `:remote` will be returned. By default,
  the value of `:local` will be returned."
  [component ks]
  (try
    (let [l (:local component)
          l' (if-not (empty? l) (local-url l) l)
          r (:remote component)]
      (cond
        (= (:extensions-from config) :remote)
        (if (empty? r) l' r)
        (empty? l') r
        :else l'))
    (catch Exception any
      (log/error "Failed to find appropriate source for component" ks "because:" any)
      nil)))

;; (preferred-source {:local "vendor/node_modules/photoswipe/dist/photoswipe.min.js",
;;                    :remote "https://cdnjs.cloudflare.com/ajax/libs/photoswipe/4.1.3/photoswipe.min.js"} :core)

(defn collect-preferred
  ([processed-text]
   (concat
     (collect-preferred processed-text :scripts)
     (collect-preferred processed-text :styles)))
  ([processed-text resource-type]
   (reduce concat
   (map
     (fn [extension-key]
       (map
         (fn [requirement]
           (let [r (preferred-source
             (-> processed-text :extensions extension-key resource-type requirement)
             requirement)]
             (if (empty? r)
               (log/warn "Found no valid URL for requirement"
                         requirement "of extension" extension-key))
             r))
          (keys (-> processed-text :extensions extension-key resource-type))))
     (keys (:extensions processed-text))))))

(cjio/file content-dir "vendor/node_modules/photoswipe/dist/photoswipe.min.js")

(def processed-text (md->html (slurp "resources/public/content/Simplified example gallery.md" )))

(preferred-source (-> processed-text :extensions :pswp :scripts :core) :pswp)

(collect-preferred processed-text :scripts)

(defn wiki-page
  "Render the markdown page specified in this `request`, if any. If none found, redirect to edit-page"
  [request]
  (log/trace (format "wiki-page: '%s'" request))
  (or
    (show-sanity-check-error)
    (let [params (keywordize-keys (:params request))
          page (or (:page params) util/start-page (util/get-message :default-page-title "Introduction" request))
          file-name (str page ".md")
          file-path (cjio/file util/content-dir file-name)
          exists? (.exists (clojure.java.io/as-file file-path))]
      (if exists?
        (do
          (log/info (format "Showing page '%s' from file '%s'" page file-path))
          (let [processed-text (md->html
                                 (include/expand-include-md
                                   (:includer md-include-system)
                                   (slurp file-path)))]
            (layout/render "wiki.html"
                           (merge (util/standard-params request)
                                  processed-text
                                  {:title page
                                   :scripts (collect-preferred processed-text :scripts)
                                   :styles (collect-preferred processed-text :styles)
                                   :page page
                                   :editable true}))))
        ;else
        (response/redirect (str "/edit?page=" page))))))


(defn history-page
  "Render the history for the markdown page specified in this `request`,
  if any. If none, error?"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) (util/get-message :default-page-title request)))
        file-name (str page ".md")
        repo-path util/content-dir]
    (log/info (format "Showing history of page '%s'" page))
    (layout/render "history.html"
                   (merge (util/standard-params request)
                          {:title (str (util/get-message :history-title-prefix request)
                                       " " page)
                           :page page
                           :history (hist/find-history repo-path file-name)}))))

;;;; this next section is all stuff supporting the list-uploads page, and maybe
;;;; should be moved to its own file.

(def image-extns #{".gif" ".jpg" ".jpeg" ".png"})

(defn format-instant
  "Format this `unix-time`, expected to be a Long, into something human readable.
  If `template` is supplied, use that as the formatting template as specified for
  java.time.Formatter. Assumes system default timezone. Returns a string."
  ([^Long unix-time]
   (format-instant unix-time "dd MMMM YYYY"))
  ([^Long unix-time ^String template]
   (jt/format
     (java-time/formatter template)
     (java.time.LocalDateTime/ofInstant
       (java-time/instant unix-time)
       (java.time.ZoneOffset/systemDefault)))))

(defn list-uploads-page
  "Render a list of all uploaded files"
  [request]
  (let
    [params (keywordize-keys (:params request))
     files
     (sort-by
       (juxt :name (fn [x] (- 0 (count (:resource x)))))
       (map
         #(zipmap
            [:base-name :is-image :modified :name :resource]
            [(fs/base-name %)
             (if
               (and (fs/extension %)
                    (image-extns (cs/lower-case (fs/extension %))))
               true false)
             (if
               (fs/mod-time %)
               (format-instant (fs/mod-time %)))
             (fs/name %)
             (util/local-url %)])
         (remove
           #(or (cs/starts-with? (fs/name %) ".")
                (fs/directory? %))
           (file-seq (clojure.java.io/file util/upload-dir)))))]
    (log/info (with-out-str (pprint files)))
    (layout/render
      "list-uploads.html"
      (merge (util/standard-params request)
             {:title (str
                       (util/get-message :list-files request)
                       (if
                         (:search params)
                         (str " " (util/get-message :matching request))))
              :search (:search params)
              :files (if
                       (:search params)
                       (try
                         (let [pattern (re-pattern (:search params))]
                           (filter
                             #(re-find pattern (:base-name %))
                             files))
                         (catch Exception _ files))
                       files)
              }))))


;;;; end of list-uploads section ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn upload-page
  "Render a form to allow the upload of a file."
  [request]
  (let [params (keywordize-keys (:params request))
        data-path (str util/content-dir "/uploads/")
        git-repo (hist/load-or-init-repo util/content-dir)
        upload (:upload params)
        uploaded (if upload (ul/store-upload params data-path))
        user (session/get :user)
        summary (format "%s: %s" user (or (:summary params) "no summary"))]
;; TODO: Get this working! it MUST work!
;;     (if-not
;;       (empty? uploaded)
;;       (do
;;         (map
;;           #(git/git-add git-repo (str :resource %))
;;           (remove nil? uploaded))
;;         (git/git-commit git-repo summary {:name user :email (auth/get-email user)})))
    (layout/render "upload.html"
                   (merge (util/standard-params request)
                          {:title (util/get-message :file-upload-title request)
                           :uploaded uploaded}))))

(defn version-page
  "Render a specific historical version of a page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) (util/get-message :default-page-title request)))
        version (:version params)
        file-name (str page ".md")
        content (hist/fetch-version util/content-dir file-name version)]
    (log/info (format "Showing version '%s' of page '%s'" version page))
    (layout/render "wiki.html"
                   (merge (util/standard-params request)
                          {:title (str (util/get-message :vers-col-hdr request) " " version " " (util/get-message :of request) " "  page)
                           :page page
                           :content (md->html content)}))))


(defn diff-page
  "Render a diff between two versions of a page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) (util/get-message :default-page-title request)))
        version (:version params)
        file-name (str page ".md")]
    (log/info (format "Showing diff between version '%s' of page '%s' and current" version page))
    (layout/render "wiki.html"
                   (merge (util/standard-params request)
                          {:title
                           (str
                             (util/get-message :diff-title-prefix request)
                             " "
                             version
                             " "
                             (util/get-message :of request)
                             " "
                             page)
                           :page page
                           :content (d2h/diff2html
                                      (hist/diff util/content-dir file-name version))}))))


(defn auth-page
  "Render the auth page"
  [request]
  (or
    (show-sanity-check-error)
    (let [params (keywordize-keys (:params request))
          form-params (keywordize-keys (:form-params request))
          username (:username form-params)
          password (:password form-params)
          action (:action form-params)
          user (session/get :user)
          redirect-to (:redirect-to params)]
      (if redirect-to (log/info (str "After auth, redirect to: " redirect-to)))
      (cond
        (= action (util/get-message :logout-label request))
        (do
          (log/info (str "User " user " logging out"))
          (session/remove! :user)
          (response/redirect redirect-to))
        (and username password (auth/authenticate username password))
        (do
          (session/put! :user username)
          (response/redirect redirect-to))
        true
        (layout/render "auth.html"
                       (merge (util/standard-params request)
                              {:title (if user
                                        (str (util/get-message :logout-link request) " " user)
                                        (util/get-message :login-link request))
                               :redirect-to redirect-to}))))))

(defn wrap-restricted-redirect
  ;; TODO: this is not idiomatic, and it's too late to write something idiomatic just now
  [f request]
  (route/restricted
    (apply
      f
      (if
        (-> request :params :redirect-to) ;; a redirect target has already been set
        request
        ;; else merge a redirect target into the params
        (let
          [redirect-to (if (:uri request)
                         (cs/join "?" [(:uri request) (:query-string request)]))]
          (assoc-in request [:params :redirect-to] redirect-to))))))

(defn passwd-page
  "Render a page to change the user password"
  [request]
  (let [params (keywordize-keys (:form-params request))
        oldpass (:oldpass params)
        pass1 (:pass1 params)
        pass2 (:pass2 params)
        user (session/get :user)
        check-pass (auth/evaluate-password pass1 pass2)
        changed? (and
                   (true? check-pass)
                   (auth/change-pass user oldpass pass2))]
    (layout/render "passwd.html"
                   (merge (util/standard-params request)
                          {:title (str (util/get-message :chpass-title-prefix request) " " user)
                           :message (if changed? (util/get-message :chpass-success request))
                           :error (cond
                                    (nil? oldpass) nil
                                    changed? nil
                                    (keyword? check-pass) (util/get-message check-pass request)
                                    true (util/get-message :chpass-fail request))}))))


(defroutes wiki-routes
  (GET "/" request (wiki-page request))
  (GET "/auth" request (auth-page request))
  (POST "/auth" request (auth-page request))
  (GET "/changes" request (diff-page request))
  (GET "/delete-user" request (route/restricted (admin/delete-user request)))
  (GET "/edit" request (route/restricted (edit-page request)))
  (POST "/edit" request (route/restricted (edit-page request)))
  (GET "/edit-css" request (route/restricted (edit-css-page request)))
  (POST "/edit-css" request (route/restricted (edit-css-page request)))
  (GET "/edit-users" request (route/restricted (admin/edit-users request)))
  (GET "/edit-user" request (route/restricted (admin/edit-user request)))
  (POST "/edit-user" request (route/restricted (admin/edit-user request)))
  (GET "/history" request (history-page request))
  (GET "/list-uploads" request (route/restricted (list-uploads-page request)))
  (POST "/list-uploads" request (route/restricted (list-uploads-page request)))
  (GET "/version" request (version-page request))
  (GET "/passwd" request (passwd-page request))
  (POST "/passwd" request (passwd-page request))
  (GET "/upload" request (route/restricted (upload-page request)))
  (POST "/upload" request (route/restricted (upload-page request)))
  (GET "/wiki" request (wiki-page request))
  )
