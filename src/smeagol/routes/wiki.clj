;; Smeagol: a very simple Wiki engine
;; Copyright (C) 2014 Simon Brooke

;; This program is free software; you can redistribute it and/or
;; modify it under the terms of the GNU General Public License
;; as published by the Free Software Foundation; either version 2
;; of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.

;; You should have received a copy of the GNU General Public License
;; along with this program; if not, write to the Free Software
;; Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

(ns smeagol.routes.wiki
  (:use clojure.walk)
  (:require [compojure.core :refer :all]
            [clj-jgit.porcelain :as git]
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

(defn local-links
  "Rewrite text in `html-src` surrounded by double square brackets as a local link into this wiki."
  [html-src]
  (clojure.string/replace html-src #"\[\[[^\[\]]*\]\]"
                          #(let [text (clojure.string/replace %1 #"[\[\]]" "")]
                             (str "<a href='wiki?content=" text "'>" text "</a>"))))

(defn process-source
  "Process `source-text` and save it to the specified `file-path`, committing it
   to Git and finally redirecting to wiki-page."
  [params]
  (let [source-text (:src params)
        content (:content params)
        file-name (str content ".md")
        file-path (str (io/resource-path) "/content/" file-name)
        exists? (.exists (clojure.java.io/as-file file-path))
        git-repo (git/load-repo (str (io/resource-path) "/content/.git"))
        user (session/get :user)
        email (auth/get-email user)
        summary (str user ": " (or (:summary params) "no summary"))]
    (timbre/info (str "Saving " user "'s changes (" summary ") to " file-name))
    (spit file-path source-text)
    (if (not exists?) (git/git-add git-repo file-name))
    (git/git-commit git-repo summary {:name user :email email})
    (response/redirect (str "/wiki?" content))
  ))

(defn edit-page
  "Render a page in a text-area for editing. This could have been done in the same function as wiki-page,
  and that would have been neat, but I couldn't see how to establish security if that were done."
  [request]
  (let [params (keywordize-keys (:params request))
        src-text (:src params)
        content (:content params)
        file-name (str "/content/" content ".md")
        file-path (str (io/resource-path) file-name)
        exists? (.exists (clojure.java.io/as-file file-path))]
    (cond src-text (process-source params)
          true
          (layout/render "edit.html"
                   {:title (str "Edit " content)
                    :left-bar (local-links (util/md->html "/content/_edit-left-bar.md"))
                    :header (local-links (util/md->html "/content/_header.md"))
                    :content (if exists? (io/slurp-resource file-name) "")
                    :user (session/get :user)
                    :exists exists?}))))

(defn wiki-page
  "Render the markdown page specified in this `request`, if any. If none found, redirect to edit-page"
  [request]
  (let [params (keywordize-keys (:params request))
        content (or (:content params) "Introduction")
        file-name (str "/content/" content ".md")
        file-path (str (io/resource-path) file-name)
        exists? (.exists (clojure.java.io/as-file file-path))]
    (cond exists?
      (layout/render "wiki.html"
                   {:title content
                    :page content
                    :left-bar (local-links (util/md->html "/content/_left-bar.md"))
                    :header (local-links (util/md->html "/content/_header.md"))
                    :content (local-links (util/md->html file-name))
                    :user (session/get :user)})
          true (response/redirect (str "/edit?content=" content)))))

(defn history-page
  "Render the history for the markdown page specified in this `request`, 
   if any. If none, error?"
  [request]
  (let [params (keywordize-keys (:params request))
        page (or (:page params) "Introduction")
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")]
    (layout/render "history.html"
                   {:title (str "History of " page)
                    :page page
                    :left-bar (local-links (util/md->html "/content/_left-bar.md"))
                    :header (local-links (util/md->html "/content/_header.md"))
                    :history (hist/find-history repo-path file-name)})))

(defn version-page
  "Render a specific historical version of a page"
  [request]
  (let [params (keywordize-keys (:params request))
        page (or (:page params) "Introduction")
        version (:version params)
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")]
    (layout/render "wiki.html"
                   {:title (str "Version " version " of " page)
                    :page page
                    :left-bar (local-links 
                                (util/md->html "/content/_left-bar.md"))
                    :header (local-links 
                              (util/md->html "/content/_header.md"))
                    :content (local-links 
                               (md/md-to-html-string 
                                 (hist/fetch-version 
                                   repo-path file-name version)))
                    :user (session/get :user)})))

(defn diff-page 
  "Render a diff between two versions of a page" 
  [request]
  (let [params (keywordize-keys (:params request))
        page (or (:page params) "Introduction")
        version (:version params)
        file-name (str page ".md")
        repo-path (str (io/resource-path) "/content/")]
    (layout/render "wiki.html"
                   {:title (str "Changes since version " version " of " page)
                    :page page
                    :left-bar (local-links 
                                (util/md->html "/content/_left-bar.md"))
                    :header (local-links 
                              (util/md->html "/content/_header.md"))
                    :content (d2h/diff2html (hist/diff repo-path file-name version))
                    :user (session/get :user)})))

(defn auth-page
  "Render the auth page"
  [request]
  (let [params (keywordize-keys (:form-params request))
        username (:username params)
        password (:password params)
        action (:action params)
        user (session/get :user)]
    (cond
      (= action "Logout!")
      (do
        (timbre/info (str "User " user " logging out"))
        (session/remove! :user)
        (response/redirect "/wiki"))
      (and username password (auth/authenticate username password))
      (do
        (session/put! :user username)
        (response/redirect "/wiki"))
      true
      (layout/render "auth.html"
                   {:title (if user (str "Logout " user) "Log in")
                    :left-bar (local-links (util/md->html "/content/_left-bar.md"))
                    :header (local-links (util/md->html "/content/_header.md"))
                    :user user}))))

(defn about-page []
  (layout/render "about.html"))

(defroutes wiki-routes
  (GET "/wiki" request (wiki-page request))
  (GET "/" request (wiki-page request))
  (GET "/edit" request (route/restricted (edit-page request)))
  (POST "/edit" request (route/restricted (edit-page request)))
  (GET "/history" request (history-page request))
  (GET "/version" request (version-page request))
  (GET "/changes" request (diff-page request))
  (GET "/auth" request (auth-page request))
  (POST "/auth" request (auth-page request))
  (GET "/about" [] (about-page)))
