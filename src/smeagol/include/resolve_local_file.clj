(ns ^{:doc "Functions related to the include of markdown-paged - providing
a plugable load-local-include-links componet. This namespaces is implementation detail for
smeagol.include and not inteded for direct usage."
      :author "Michael Jerger"}
  smeagol.include.resolve-local-file
  (:require
    [schema.core :as s]
    [smeagol.include.resolve :as resolve]
    [clojure.java.io :as cjio]
    [taoensso.timbre :as timbre]))

(s/defmethod resolve/do-resolve-md :local-file
  [resolver
   uri :- s/Str]
  (let [file-name uri
        file-path (cjio/file (:local-base-dir resolver) file-name)
        exists? (.exists (clojure.java.io/as-file file-path))]
    (cond exists?
          (do
            (timbre/info (format "Including page '%s' from file '%s'" uri file-path))
            (slurp file-path))
          :else
            (do
              (timbre/info (format "Page '%s' not found at '%s'" uri file-path))
              (str "include not found at " file-path)))))

(s/defn
  new-resolver
  [local-base-dir :- s/Str]
  (resolve/new-resolver :local-file local-base-dir))
