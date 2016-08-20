(ns ^{:doc "Explore the history of a page."
      :author "Simon Brooke"}
  smeagol.history
  (:require [taoensso.timbre :as timbre]
            [clj-jgit.porcelain :as git]
            [clj-jgit.internal :as i]
            [clj-jgit.querying :as q])
  (:import  [org.eclipse.jgit.api Git]
            [org.eclipse.jgit.lib Repository ObjectId]
            [org.eclipse.jgit.revwalk RevCommit RevTree RevWalk]
            [org.eclipse.jgit.treewalk TreeWalk AbstractTreeIterator CanonicalTreeParser]
            [org.eclipse.jgit.treewalk.filter PathFilter]
            [org.eclipse.jgit.diff DiffEntry DiffFormatter]))

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

(defn entry-contains
  "If this `log-entry` contains a reference to this `file-path`, return the entry;
   else nil."
  [^String log-entry ^String file-path]
  (timbre/info (format "searching '%s' for '%s'" log-entry file-path))
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


(defn prepare-tree-parser
  "As far as possible a straight translation of prepareTreeParser from
   https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/ShowFileDiff.java"
  ^org.eclipse.jgit.treewalk.AbstractTreeIterator
  [^Git repo ^String id]
  (let [walk (i/new-rev-walk repo)
        commit (i/bound-commit repo walk (ObjectId/fromString id))
        tree (.parseTree walk (.getId (.getTree commit)))
        result (CanonicalTreeParser.)
        reader (.newObjectReader (.getRepository repo))]
    (try
      (.reset result reader (.getId tree))
      (finally
        (.release reader)
        (.dispose walk)))
    result))


(defn diff
  "Find the diff in the file at `file-path` within the repository at
   `git-directory-path` between versions `older` and `newer` or between the specified
   `version` and the current version of the file. Returns the diff as a string.

   Based on JGit Cookbook ShowFileDiff."
  ([^String git-directory-path ^String file-path ^String version]
    (diff git-directory-path file-path version
          (:id (first (find-history git-directory-path file-path)))))
  ([^String git-directory-path ^String file-path ^String older ^String newer]
    (let [git-r (git/load-repo git-directory-path)
          old-parse (prepare-tree-parser git-r older)
          new-parse (prepare-tree-parser git-r newer)
          out (java.io.ByteArrayOutputStream.)]
      (map
        #(let [formatter (DiffFormatter. out)]
           (.setRepository formatter (.getRepository git-r))
           (.format formatter %)
           %)
        (.call
          (.setOutputStream
            (.setPathFilter
              (.setNewTree
                (.setOldTree (.diff git-r) old-parse)
                new-parse)
              (PathFilter/create file-path))
            out)))
      (.toString out))))


(defn fetch-version
  "Return (as a String) the text of this `version` of the file at this
   `file-path` in the git directory at this `git-directory-path`.

   Based on JGit Cookbook ReadFileFromCommit."
  [^String git-directory-path ^String file-path ^String version]
  (let [git-r (git/load-repo git-directory-path)
        repo (.getRepository git-r)
        walk (i/new-rev-walk git-r)
        commit (i/bound-commit git-r walk (ObjectId/fromString version))
        tree (.parseTree walk (.getId (.getTree commit)))
        tw (TreeWalk. repo)
        out (java.io.ByteArrayOutputStream.)]
    (.addTree tw tree)
    (.setRecursive tw true)
    (.setFilter tw (PathFilter/create file-path))
    (if (not (.next tw))
      (throw (IllegalStateException.
               (str "Did not find expected file '" file-path "'"))))
    (.copyTo (.open repo (.getObjectId tw 0)) out)
    (.toString out)))
