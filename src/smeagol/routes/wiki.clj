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
            [noir.io :as io]
            [noir.response :as response]
            [noir.util.route :as route]
            [noir.session :as session]
            [smeagol.authenticate :as auth]
            [smeagol.layout :as layout]
            [smeagol.util :as util]))

(defn process-source
  "Process `source-text` and save it to the specified `file-path`, finally redirecting to wiki-page"
  [file-path source-text request]
  (let [params (keywordize-keys (:params request))
        content (or (:content params) "Introduction")]
    (spit file-path source-text)
    (response/redirect (str "wiki?" content))
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
    (cond src-text (process-source file-path src-text request)
          true
          (layout/render "edit.html"
                   {:title content
                    :left-bar (util/md->html "/content/_edit-left-bar.md")
                    :header (util/md->html "/content/_header.md")
                    :content (if exists? (io/slurp-resource file-name) "")
                    :user (session/get :user)}))))

(defn local-links
  "Rewrite text in `html-src` surrounded by double square brackets as a local link into this wiki."
  [html-src]
  (clojure.string/replace html-src #"\[\[[^\[\]]*\]\]"
                          #(let [text (clojure.string/replace %1 #"[\[\]]" "")]
                             (str "<a href='wiki?content=" text "'>" text "</a>"))))

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
                    :left-bar (util/md->html "/content/_left-bar.md")
                    :header (util/md->html "/content/_header.md")
                    :content (local-links (util/md->html file-name))
                    :user (session/get :user)})
          true (response/redirect (str "edit?content=" content)))))

(defn auth-page
  "Render the auth page"
  [request]
  (let [params (keywordize-keys (:params request))
        username (:username params)
        password (:password params)
        action (:action params)
        user (session/get :user)]
    (println (str "Action = " action))
    (cond
      (= action "Logout!") 
      (do 
        (session/remove! :user)
        (response/redirect "wiki"))
      (and username password (auth/authenticate username password))
      (do
        (session/put! :user username)
        (response/redirect "wiki"))
      true
      (layout/render "auth.html"
                   {:title (if user (str "Logout " user) "Log in")
                    :left-bar (util/md->html "/content/_left-bar.md")
                    :header (util/md->html "/content/_header.md")
                    :user user}))))

(defn about-page []
  (layout/render "about.html"))

(defroutes wiki-routes
  (GET "/wiki" request (wiki-page request))
  (GET "/" request (wiki-page request))
  (GET "/edit" request (route/restricted (edit-page request)))
  (POST "/edit" request (route/restricted (edit-page request)))
  (GET "/auth" request (auth-page request))
  (POST "/auth" request (auth-page request))
  (GET "/about" [] (about-page)))
