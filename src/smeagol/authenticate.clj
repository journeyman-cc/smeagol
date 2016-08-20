(ns ^{:doc "Authentication functions."
      :author "Simon Brooke"}
  smeagol.authenticate
  (:use clojure.walk)
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

(defn authenticate
  "Return `true` if this `username`/`password` pair match, `false` otherwise"
  [username password]
  (let [path (str (io/resource-path) "../passwd")
        users (read-string (slurp path))
        user ((keyword username) users)]
    (timbre/info (str "Authenticating " username " against " path))
    (and user
         (or
          (.equals (:password user) password)
          (password/check password (:password user))))))

(defn get-email
  "Return the email address associated with this `username`."
  [username]
  (let [path (str (io/resource-path) "../passwd")
        users (read-string (slurp path))
        user ((keyword username) users)]
    (if user (:email user))))

;;; TODO: worth locking the passwd file to prevent corruption if two simultaneous threads
;;; try to write it. See http://stackoverflow.com/questions/6404717/idiomatic-file-locking-in-clojure

(defn change-pass
  "Change the password for the user with this `username` and `oldpass` to this `newpass`.
  Return `true` if password was successfully changed. Subsequent to user change, their
  password will be encrypted."
  [username oldpass newpass]
  (timbre/info (format "Changing password for user %s" username))
  (let [path (str (io/resource-path) "../passwd")
        users (read-string (slurp path))
        keywd (keyword username)
        user (if users (keywd users))
        email (:email user)]
    (try
      (cond
       (and user
            (or
             (.equals (:password user) oldpass)
             (password/check oldpass (:password user))))
       (do
         (spit path
               (assoc (dissoc users keywd) keywd
                 {:password (password/encrypt newpass) :email email}))
         true))
      (catch Exception any
        (timbre/error
         (format "Changing password failed for user %s failed: %s (%s)"
                 username (.getName (.getClass any)) (.getMessage any)))
        false))))
