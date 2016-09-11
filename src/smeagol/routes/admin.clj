(ns ^{:doc "Render all the main pages of a very simple Wiki engine."
      :author "Simon Brooke"}
  smeagol.routes.admin
  (:require [clojure.walk :refer :all]
            [noir.session :as session]
            [taoensso.timbre :as timbre]
            [smeagol.authenticate :as auth]
            [smeagol.layout :as layout]
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
;;;; Copyright (C) 2016 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn edit-users
  "Put a list of users on-screen for editing."
  [request]
  (let [params (keywordize-keys (:params request))
        user (session/get :user)]
    (layout/render "edit-users.html"
                   (merge (util/standard-params request)
                          {:title "Select user to edit"
                           :users (auth/list-users)}))))

(defn delete-user
  "Delete a user."
  [request]
  (let [params (keywordize-keys (:params request))
        target (:target params)
        deleted (auth/delete-user target)
        message (if deleted (str "Successfully deleted user " target))
        error (if (not deleted) (str "Could not delete user " target))]
    (layout/render "edit-users.html"
                   (merge (util/standard-params request)
                          {:title "Select user to edit"
                           :message message
                           :error error
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
                   (merge (util/standard-params request)
                          {:title (str "Edit user " target)
                           :message message
                           :error error
                           :target target
                           :details details}))))
