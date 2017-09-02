(ns ^{:doc "Authentication functions."
      :author "Simon Brooke"}
  smeagol.authenticate
  (:require [taoensso.timbre :as timbre]
            [noir.io :as io]
            [crypto.password.scrypt :as password]))

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
;;;;
;;;;  All functions which relate to the passwd file are in this namespace, in order
;;;;  that it can reasonably simply swapped out for a more secure replacement.
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; the relative path to the password file.
(def password-file-path (str (clojure.java.io/resource "passwd")))


(defn- get-users
  "Get the whole content of the password file as a clojure map"
  []
  (read-string (slurp password-file-path)))


(defn authenticate
  "Return `true` if this `username`/`password` pair match, `false` otherwise"
  [username password]
  (let [user ((keyword username) (get-users))]
    (timbre/info (str "Authenticating " username " against " password-file-path))
    (and user
         (:password user)
         (or
          (.equals (:password user) password)
          (password/check password (:password user))))))


(defn get-email
  "Return the email address associated with this `username`."
  [username]
  (if username
    (let [user ((keyword username)  (get-users))]
      (:email user))))


(defn get-admin
  "Return a flag indicating whether the user with this username is an administrator."
  [username]
  (if username
    (let [user ((keyword username)  (get-users))]
      (:admin user))))

(defn evaluate-password
  "Evaluate whether this proposed password is suitable for use."
  ([pass1 pass2]
   (and pass1 (>= (count pass1) 8) (.equals pass1 pass2)))
  ([password]
   (evaluate-password password password)))


(defn change-pass
  "Change the password for the user with this `username` and `oldpass` to this `newpass`.
  Return `true` if password was successfully changed. Subsequent to user change, their
  password will be encrypted."
  [username oldpass newpass]
  (timbre/info (format "Changing password for user %s" username))
  (let [users (get-users)
        keywd (keyword username)
        user (keywd users)
        email (:email user)]
    (try
      (cond
        (and user
             (or
               (.equals (:password user) oldpass)
               (password/check oldpass (:password user))))
        (do
          (locking password-file-path
            (spit password-file-path
                  (merge users
                         {keywd
                          (merge user
                                 {:password (password/encrypt newpass)})})))
        (timbre/info (str "Successfully changed password for user " username))
          true))
      (catch Exception any
        (timbre/error
          (format "Changing password failed for user %s failed: %s (%s)"
                  username (.getName (.getClass any)) (.getMessage any)))
        false))))


(defn list-users
  "Return, as strings, the names of the currently known users."
  []
  (map name (keys (get-users))))


(defn fetch-user-details
  "Return the map of features of this user, if any."
  [username]
  (if
    (and username (> (count (str username)) 0))
    ((keyword username) (get-users))))


(defn add-user
  "Add a user to the passwd file with this username, initial password and email address and admin flag."
  [username newpass email admin]
  (let [users (get-users)
        user ((keyword username) users)
        password (if
                   (and newpass (evaluate-password newpass))
                   (password/encrypt newpass))
        details {:email email
                 :admin (if
                          (and (string? admin) (> (count admin) 0))
                          true
                          false)}
        ;; if we have a valid password we want to include it in the details to update.
        full-details (if password
                       (merge details {:password password})
                       details)]
    (try
      (locking password-file-path
        (spit password-file-path
              (merge users
                     {(keyword username) (merge user full-details)}))
        (timbre/info (str "Successfully added user " username))
        true)
      (catch Exception any
        (timbre/error
          (format "Adding user %s failed: %s (%s)"
                  username (.getName (.getClass any)) (.getMessage any)))
        false))))


(defn delete-user
  "Delete the user with this `username` from the password file."
  [username]
  (let [users (get-users)]
    (try
      (locking password-file-path
        (spit password-file-path
              (dissoc users (keyword username)))
        (timbre/info (str "Successfully deleted user " username))
        true)
      (catch Exception any
        (timbre/error
          (format "Deleting user %s failed: %s (%s)"
                  username (.getName (.getClass any)) (.getMessage any)))
        false))))
