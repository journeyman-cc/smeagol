(ns ^{:doc "Format a diff as HTML."
      :author "Simon Brooke"}
  smeagol.diff2html
  (:require [clojure.string :refer [join split split-lines]]))

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


(defn starts-with?
  "True if `s` starts with this `prefix`"
  [^String s ^String prefix]
  (.startsWith s prefix))


(defn mung-line
  "Convert a single line from diff to HTML"
  [^String line]
  (cond
    (starts-with? line "+") (str "<p><ins>" (subs line 1) "</ins></p>")
    (starts-with? line "-") (str "<p><del>" (subs line 1) "</del></p>")
    (starts-with? line "@@") "</div><div class='change'>"
    (starts-with? line "\\") (str "<p class='warn'>" (subs line 1) "</p>")
    :true (str "<p>" line "</p>")))


(defn diff2html
  "Convert this string, assumed to be in diff format, to HTML."
  [^String diff-text]
  ;; TODO doesn't work yet
  (apply str
         (flatten
           (list "<div class='change'>"
                 (join "\n"
                       (remove nil?
                               (map mung-line
                                    ;; The first five lines are boilerplate, and
                                    ;; uninteresting for now
                                    (drop 5
                                          (split-lines diff-text)))))
                 "</div>"))))


