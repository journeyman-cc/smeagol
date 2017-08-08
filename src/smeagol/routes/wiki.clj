(ns ^{:doc "Render all the main pages of a very simple Wiki engine."
      :author "Simon Brooke"}
  smeagol.routes.wiki
  (:require [clojure.walk :refer :all]
            [clojure.java.io :as cjio]
            [clojure.string :as cs]
            [cemerick.url :refer (url url-encode url-decode)]
            [compojure.core :refer :all]
            [clj-jgit.porcelain :as git]
            [markdown.core :as md]
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.route :as route]
            [noir.session :as session]
            [taoensso.timbre :as timbre]
            [smeagol.authenticate :as auth]
            [smeagol.diff2html :as d2h]
            [smeagol.formatting :refer [md->html]]
            [smeagol.layout :as layout]
            [smeagol.util :as util]
            [smeagol.uploads :as ul]
            [smeagol.history :as hist]
            [smeagol.routes.admin :as admin]))

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

(defn get-git-repo
  "Get the git repository for my content, creating it if necessary"
  []
  (let [path (str (io/resource-path) "/content/")
        repo (cjio/as-file (str path ".git"))]
    (if (.exists repo) (git/load-repo repo)
      (git/git-init path))))


(defn process-source
  "Process `source-text` and save it to the specified `file-path`, committing it
  to Git and finally redirecting to wiki-page."
  [params suffix request]
  (let [source-text (:src params)
        page (:page params)
        file-name (str page suffix)
        file-path (str (io/resource-path) "/content/" file-name)
        exists? (.exists (clojure.java.io/as-file file-path))
        git-repo (get-git-repo)
        user (session/get :user)
        email (auth/get-email user)
        summary (format "%s: %s" user (or (:summary params) "no summary"))]
    (timbre/info (format "Saving %s's changes ('%s') to %s" user summary page))
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
   (edit-page request (util/get-message :default-page-title request) ".md" "edit.html" "/content/_edit-side-bar.md"))
  ([request default suffix template side-bar]
   (let [params (keywordize-keys (:params request))
         src-text (:src params)
         page (or (:page params) default)
         file-path (str (io/resource-path) "content/" page suffix)
         exists? (.exists (cjio/as-file file-path))
         user (session/get :user)]
     (if (not exists?)
       (timbre/info (format "File '%s' not found; creating a new file" file-path))
       (timbre/info (format "Opening '%s' for editing" file-path)))
     (cond src-text (process-source params suffix request)
           true
           (layout/render template
                          (merge (util/standard-params request)
                                 {:title (str (util/get-message :edit-title-prefix request) " " page)
                                  :page page
                                  :side-bar (md->html (io/slurp-resource side-bar))
                                  :content (if exists? (io/slurp-resource (str "/content/" page suffix)) "")
                                  :exists exists?}))))))


(defn edit-css-page
  "Render a stylesheet in a text-area for editing.."
  [request]
  (edit-page request "stylesheet" ".css" "edit-css.html" "/content/_edit-side-bar.md"))


(defn wiki-page
  "Render the markdown page specified in this `request`, if any. If none found, redirect to edit-page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (or (:page params) (util/get-message :default-page-title request))
        file-name (str "/content/" page ".md")
        file-path (str (io/resource-path) file-name)
        exists? (.exists (clojure.java.io/as-file file-path))]
    (cond exists?
          (do
            (timbre/info (format "Showing page '%s'" page))
            (layout/render "wiki.html"
                           (merge (util/standard-params request)
                                  {:title page
                                   :page page
                                   :content (md->html (io/slurp-resource file-name))
                                   :editable true})))
          true (response/redirect (str "/edit?page=" page)))))


(defn history-page
  "Render the history for the markdown page specified in this `request`,
  if any. If none, error?"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) (util/get-message :default-page-title request)))
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")]
    (timbre/info (format "Showing history of page '%s'" page))
    (layout/render "history.html"
                   (merge (util/standard-params request)
                          {:title (str "History of " page)
                           :page page
                           :history (md->html (hist/find-history repo-path file-name))}))))

(defn upload-page
  "Render a form to allow the upload of a file."
  [request]
  (let [params (keywordize-keys (:params request))
        data-path (str (io/resource-path) "/uploads/")
        upload (:upload params)
        uploaded (if upload (ul/store-upload params))]
    (layout/render "upload.html"
                   (merge (util/standard-params request)
                          {:title (util/get-message :file-upload-title request)
                           :uploaded uploaded
                           :is-image (and
                                       uploaded
                                       (or
                                         (cs/ends-with? uploaded ".gif")
                                         (cs/ends-with? uploaded ".jpg")
                                         (cs/ends-with? uploaded ".jpeg")
                                         (cs/ends-with? uploaded ".png")
                                         (cs/ends-with? uploaded ".GIF")
                                         (cs/ends-with? uploaded ".JPG")
                                         (cs/ends-with? uploaded ".PNG")))}))))


(defn version-page
  "Render a specific historical version of a page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) (util/get-message :default-page-title request)))
        version (:version params)
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")
        content (hist/fetch-version repo-path file-name version)]
    (timbre/info (format "Showing version '%s' of page '%s'" version page))
    (layout/render "wiki.html"
                   (merge (util/standard-params request)
                          {:title (str (util/get-message :vers-col-hdr request) " " version " of " page)
                           :page page
                           :content (md->html content)}))))


(defn diff-page
  "Render a diff between two versions of a page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) (util/get-message :default-page-title request)))
        version (:version params)
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")]
    (timbre/info (format "Showing diff between version '%s' of page '%s' and current" version page))
    (layout/render "wiki.html"
                   (merge (util/standard-params request)
                          {:title (str (util/get-message :diff-title-prefix request)" " version " of " page)
                           :page page
                           :content (d2h/diff2html (hist/diff repo-path file-name version))}))))


(defn auth-page
  "Render the auth page"
  [request]
  (let [params (keywordize-keys (:form-params request))
        username (:username params)
        password (:password params)
        action (:action params)
        user (session/get :user)
        redirect-to (or (:redirect-to params) "/wiki")]
    (cond
     (= action (util/get-message :logout-label request))
     (do
       (timbre/info (str "User " user " logging out"))
       (session/remove! :user)
       (response/redirect redirect-to))
     (and username password (auth/authenticate username password))
     (do
       (session/put! :user username)
       (response/redirect redirect-to))
     true
     (layout/render "auth.html"
                   (merge (util/standard-params request)
                    {:title (if user (str (util/get-message :logout-link request) " " user) (util/get-message :login-link request))
                     :redirect-to ((:headers request) "referer")})))))


(defn passwd-page
  "Render a page to change the user password"
  [request]
  (let [params (keywordize-keys (:form-params request))
        oldpass (:oldpass params)
        pass1 (:pass1 params)
        pass2 (:pass2 params)
        user (session/get :user)
        changed? (and
                   (auth/evaluate-password pass1 pass2)
                   (auth/change-pass user oldpass pass2))]
    (layout/render "passwd.html"
                   (merge (util/standard-params request)
                          {:title (str (util/get-message :chpass-title-prefix request) " " user)
                           :message (if changed? (util/get-message :chpass-success request))
                           :error (cond
                                    (nil? oldpass) nil
                                    changed? nil
                                    (< (count pass1) 8) (util/get-message :chpass-too-short request)
                                    (not (= pass1 pass2)) (util/get-message :chpass-bad-match request)
                                    true (util/get-message :chpass-fail request))}))))


(defroutes wiki-routes
  (GET "/wiki" request (wiki-page request))
  (GET "/" request (wiki-page request))
  (GET "/delete-user" request (route/restricted (admin/delete-user request)))
  (GET "/edit" request (route/restricted (edit-page request)))
  (POST "/edit" request (route/restricted (edit-page request)))
  (GET "/edit-css" request (route/restricted (edit-css-page request)))
  (POST "/edit-css" request (route/restricted (edit-css-page request)))
  (GET "/edit-users" request (route/restricted (admin/edit-users request)))
  (GET "/edit-user" request (route/restricted (admin/edit-user request)))
  (POST "/edit-user" request (route/restricted (admin/edit-user request)))
  (GET "/history" request (history-page request))
  (GET "/version" request (version-page request))
  (GET "/changes" request (diff-page request))
  (GET "/auth" request (auth-page request))
  (POST "/auth" request (auth-page request))
  (GET "/passwd" request (passwd-page request))
  (POST "/passwd" request (passwd-page request))
  (GET "/upload" request (route/restricted (upload-page request)))
  (POST "/upload" request (route/restricted (upload-page request))))
