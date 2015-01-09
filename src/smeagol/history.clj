(ns smeagol.history
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as q]))

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
