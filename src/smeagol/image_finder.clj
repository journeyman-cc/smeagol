(ns ^{:doc "Find (by doing a 302 redirect to) appropriate images; if no
      appropriate image is found return a 302 redirect to a default image."
      :author "Simon Brooke"}
  smeagol.uploads
  (:require [clojure.string :as cs]
            [me.raynes.fs :as fs]
            [noir.io :as nio]
            [noir.response :as response]
            ))

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
;;;; Copyright (C) 2017 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; See:
;; https://github.com/weavejester/compojure/wiki/Routes-In-Detail
;; https://github.com/weavejester/compojure/wiki/Destructuring-Syntax

(defn find-image
  "Return a 302 redirect to
  1. The requested file, if available;
  2. This default URL otherwise."
  [request requested-name default-url paths-to-explore]
  (let [url (do-something-to-find-appropriate-file request)]
    (response/redirect url :found )))
