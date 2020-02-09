(ns ^{:doc "Format Semagol's extended markdown format."
      :author "Simon Brooke"}
  smeagol.extensions.mermaid
  (:require [smeagol.extensions.utils :refer :all]
            [taoensso.timbre :as log]))

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
;;;;
;;;; Graphs can now be embedded in a page using the
;;;; [Mermaid](https://mermaid-js.github.io/mermaid/#/) graph description
;;;; language. The graph description should start with a line comprising three
;;;; back-ticks and then the word `mermaid`, and end with a line comprising just
;;;; three backticks.
;;;;
;;;; Here's an example culled from the Mermaid documentation.
;;;;
;;;; ### GANTT Chart
;;;;
;;;; ```mermaid
;;;; gantt
;;;;         dateFormat  YYYY-MM-DD
;;;;         title Adding GANTT diagram functionality to mermaid
;;;;         section A section
;;;;         Completed task            :done,    des1, 2014-01-06,2014-01-08
;;;;         Active task               :active,  des2, 2014-01-09, 3d
;;;;         Future task               :         des3, after des2, 5d
;;;;         Future task2               :         des4, after des3, 5d
;;;;         section Critical tasks
;;;;         Completed task in the critical line :crit, done, 2014-01-06,24h
;;;;         Implement parser and jison          :crit, done, after des1, 2d
;;;;         Create tests for parser             :crit, active, 3d
;;;;         Future task in critical line        :crit, 5d
;;;;         Create tests for renderer           :2d
;;;;         Add to mermaid                      :1d
;;;; ```
;;;;
;;;; Mermaid graph specifications can also be loaded from URLs. Here's another
;;;; example.
;;;;
;;;; ### Class Diagram
;;;;
;;;; ```mermaid
;;;; http://localhost:3000/data/classes.mermaid
;;;; ```
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn process-mermaid
  "If this `url-or-graph-spec` is a valid URL, it is assumed to point to a plain
  text file pointing to a valid `graph-spec`; otherwise, it is expected to BE a
  valid `graph-spec`.

  Lightly mung this `graph-spec`, assumed to be a mermaid specification."
  [^String url-or-graph-spec ^Integer index]
  (let [data (resource-url-or-data->data url-or-graph-spec)
        graph-spec (:data data)]
    (log/info "Retrieved graph-spec from " (:from data) " `" ((:from data) data) "`")
    (str "<div class=\"mermaid data-visualisation\" id=\"mermaid" index "\">\n"
         graph-spec
         "\n</div>")))

;; (fs/file? (str (nio/resource-path) "data/classes.mermaid"))
;;  (slurp (str (nio/resource-path) "data/classes.mermaid"))
