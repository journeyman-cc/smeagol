(ns ^{:doc "Render all the main pages of a very simple Wiki engine."
      :author "Simon Brooke"}
  smeagol.routes.wiki
  (:require [clojure.walk :refer :all]
            [clojure.java.io :as cjio]
            [clojure.string :as cs]
            [compojure.core :refer :all]
            [clj-jgit.porcelain :as git]
            [cemerick.url :refer (url url-encode url-decode)]
            [markdown.core :as md]
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.route :as route]
            [noir.session :as session]
            [taoensso.timbre :as timbre]
            [smeagol.authenticate :as auth]
            [smeagol.diff2html :as d2h]
            [smeagol.layout :as layout]
            [smeagol.util :as util]
            [smeagol.history :as hist]))

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

(defn local-links
  "Rewrite text in `html-src` surrounded by double square brackets as a local link into this wiki."
  [^String html-src]
  (cs/replace html-src #"\[\[[^\[\]]*\]\]"
                          #(let [text (clojure.string/replace %1 #"[\[\]]" "")
                                 encoded (url-encode text)
                                 ;; I use '\_' to represent '_' in wiki markup, because
                                 ;; '_' is meaningful in Markdown. However, this needs to
                                 ;; be stripped out when interpreting local links.
                                 munged (cs/replace encoded #"%26%2395%3B" "_")]
                             (format "<a href='wiki?page=%s'>%s</a>" munged text))))


(defn get-git-repo
  "Get the git repository for my content, creating it if necessary"
  []
  (let [path (str (io/resource-path) "/content/")
        repo (cjio/as-file (str path ".git"))]
    (if (.exists repo) (git/load-repo repo)
      (git/git-init path))))


(defn standard-params
  "Return a map of standard parameters to pass to the template renderer."
  [request]
  (let [user (session/get :user)]
    {:user user
     :admin (auth/get-admin user)
     :side-bar (local-links (util/md->html "/content/_side-bar.md"))
     :header (local-links (util/md->html "/content/_header.md"))
     :version (System/getProperty "smeagol.version")}))


(defn process-source
  "Process `source-text` and save it to the specified `file-path`, committing it
  to Git and finally redirecting to wiki-page."
  [params suffix]
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
    (if (not exists?) (git/git-add git-repo file-name))
    (git/git-commit git-repo summary {:name user :email email})
    (response/redirect
      (str
        "/wiki?page="
        (if
          (= suffix ".md")
          (url-encode page)
          "Introduction")))))


(defn edit-page
  "Render a page in a text-area for editing. This could have been done in the same function as wiki-page,
  and that would have been neat, but I couldn't see how to establish security if that were done."
  ([request]
   (edit-page request "Introduction" ".md" "edit.html" "/content/_edit-side-bar.md"))
  ([request default suffix template side-bar]
   (let [params (keywordize-keys (:params request))
         src-text (:src params)
         page (or (:page params) default)
         file-path (str (io/resource-path) "content/" page suffix)
         exists? (.exists (cjio/as-file file-path))
         user (session/get :user)]
     (if (not exists?) (timbre/info (format "File '%s' not found; creating a new file" file-path)))
     (cond src-text (process-source params suffix)
           true
           (layout/render template
                          (merge (standard-params request)
                                 {:title (str "Edit " page)
                                  :page page
                                  :side-bar (local-links (util/md->html side-bar))
                                  :content (if exists? (io/slurp-resource (str "/content/" page suffix)) "")
                                  :exists exists?}))))))


(defn edit-css-page
  "Render a stylesheet in a text-area for editing.."
  [request]
   (edit-page request "stylesheet" ".css" "edit-css.html" "/content/_edit-side-bar.md"))


(defn edit-users
  "Put a list of users on-screen for editing."
  [request]
  (let [params (keywordize-keys (:params request))
        user (session/get :user)]
    (layout/render "edit-users.html"
                   (merge (standard-params request)
                          {:title "Select user to edit"
                           :users (auth/list-users)}))))


(defn edit-user
  "Put an individual user's details on screen for editing."
  [request]
  (let [params (keywordize-keys (:params request))
        target (:target params)
        pass1 (:pass1 params)
        password (if (and pass1 (auth/evaluate-password pass1 (:pass2 params))) pass1)
        stored (if (:email params)
                 (auth/add-user target password (:email params) (:admin params)))
        message (if stored (str "User " target " was stored successfully."))
        error (if (and (:email params) (not stored))
                                    (str "User " target " was not stored."))
        details (auth/fetch-user-details target)]
    (if message
      (timbre/info message))
    (if error
      (timbre/warn error))
    (layout/render "edit-user.html"
                   (merge (standard-params request)
                          {:title (str "Edit user " target)
                           :message message
                           :error error
                           :target target
                           :details details}))))


(defn wiki-page
  "Render the markdown page specified in this `request`, if any. If none found, redirect to edit-page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (or (:page params) "Introduction")
        file-name (str "/content/" page ".md")
        file-path (str (io/resource-path) file-name)
        exists? (.exists (clojure.java.io/as-file file-path))]
    (cond exists?
          (layout/render "wiki.html"
                         (merge (standard-params request)
                                {:title page
                                 :page page
                                 :content (local-links (util/md->html file-name))
                                 :editable true}))
          true (response/redirect (str "edit?page=" page)))))


(defn history-page
  "Render the history for the markdown page specified in this `request`,
  if any. If none, error?"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) "Introduction"))
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")]
    (layout/render "history.html"
                   (merge (standard-params request)
                          {:title (str "History of " page)
                           :page page
                           :history (hist/find-history repo-path file-name)}))))


(defn version-page
  "Render a specific historical version of a page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) "Introduction"))
        version (:version params)
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")]
    (layout/render "wiki.html"
                   (merge (standard-params request)
                          {:title (str "Version " version " of " page)
                           :page page
                           :content (local-links
                                      (md/md-to-html-string
                                        (hist/fetch-version
                                          repo-path file-name version)))}))))


(defn diff-page
  "Render a diff between two versions of a page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (url-decode (or (:page params) "Introduction"))
        version (:version params)
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")]
    (layout/render "wiki.html"
                   (merge (standard-params request)
                          {:title (str "Changes since version " version " of " page)
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
     (= action "Logout!")
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
                   (merge (standard-params request)
                    {:title (if user (str "Logout " user) "Log in")
                     :redirect-to ((:headers request) "referer")
                     :side-bar (local-links (util/md->html "/content/_side-bar.md"))
                     :header (local-links (util/md->html "/content/_header.md"))
                     :user user})))))


(defn passwd-page
  "Render a page to change the user password"
  [request]
  (let [params (keywordize-keys (:form-params request))
        oldpass (:oldpass params)
        pass1 (:pass1 params)
        pass2 (:pass2 params)
        user (session/get :user)
        message (cond
                  (nil? oldpass) nil
                  (and (auth/evaluate-password pass1 pass2) (auth/change-pass user oldpass pass2))
                  "Your password was changed"
                  (< (count pass1) 8) "You proposed password wasn't long enough: 8 characters required"
                  (not (= pass1 pass2)) "Your proposed passwords don't match"
                  true "Your password was not changed")] ;; but I don't know why...
    (layout/render "passwd.html"
                   (merge (standard-params request)
                          {:title (str "Change passord for " user)
                           :side-bar (local-links (util/md->html "/content/_side-bar.md"))
                           :header (local-links (util/md->html "/content/_header.md"))
                           :message message}))))


(defroutes wiki-routes
  (GET "/wiki" request (wiki-page request))
  (GET "/" request (wiki-page request))
  (GET "/edit" request (route/restricted (edit-page request)))
  (POST "/edit" request (route/restricted (edit-page request)))
  (GET "/edit-css" request (route/restricted (edit-css-page request)))
  (POST "/edit-css" request (route/restricted (edit-css-page request)))
  (GET "/edit-users" request (route/restricted (edit-users request)))
  (GET "/edit-user" request (route/restricted (edit-user request)))
  (POST "/edit-user" request (route/restricted (edit-user request)))
  (GET "/history" request (history-page request))
  (GET "/version" request (version-page request))
  (GET "/changes" request (diff-page request))
  (GET "/auth" request (auth-page request))
  (POST "/auth" request (auth-page request))
  (GET "/passwd" request (passwd-page request))
  (POST "/passwd" request (passwd-page request))
  )
