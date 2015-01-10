(ns smeagol.history
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as q]))

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

(defn entry-contains
  "If this `log-entry` contains a reference to this `file-path`, return the entry;
   else nil."
  [^String log-entry ^String file-path]
  (cond
    (not
      (empty?
        (filter
          #(= (first %) file-path)
      (:changed_files log-entry))))
    log-entry))

(defn find-history [^String git-directory-path ^String file-path]
  "Return the log entries in the repository at this `git-directory-path`
   which refer to changes to the file at this `file-path`."
  (let [repository (git/load-repo git-directory-path)]
    (filter
      #(entry-contains % file-path)
      (map #(q/commit-info repository %)
           (git/git-log repository)))))
