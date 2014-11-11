(ns smeagol.authenticate (:require [noir.io :as io]))

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

(defn authenticate
  "Return `true` if this `username`/`password` pair match, `false` otherwise"
  [username password]
  (let [path (str (io/resource-path) "passwd")
        users (read-string (slurp path))
        user (keyword username)]
    (.equals (:password (user users)) password)))

(defn get-email
  "Return the email address associated with this `username`."
  [username]
  (let [path (str (io/resource-path) "passwd")
        users (read-string (slurp path))
        user (keyword username)]
    (:email (user users))))