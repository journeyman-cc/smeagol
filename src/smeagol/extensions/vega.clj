(ns ^{:doc "Format vega/vis extensions to Semagol's extended markdown format."
      :author "Simon Brooke"}
  smeagol.extensions.vega
  (:require [smeagol.extensions.utils :refer [resource-url-or-data->data yaml->json]]
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
;;;; Inspired by [visdown](https://visdown.com/) and
;;;; [vega-lite](https://vega.github.io/vega-lite/docs/), the Vega formatter
;;;; allows you to embed vega data visualisations into Smeagol pages. The graph
;;;; description should start with a line comprising three back-ticks and then
;;;; the word '`vega`', and end with a line comprising just three backticks.
;;;;
;;;; Here's an example cribbed in its entirety from
;;;; [here](http://visdown.amitkaps.com/london):
;;;;
;;;; ### Flight punctuality at London airports
;;;;
;;;; ```vega
;;;; data:
;;;;   url: "data/london.csv"
;;;; transform:
;;;;  -
;;;;   filter: datum.year == 2016
;;;; mark: rect
;;;; encoding:
;;;;   x:
;;;;     type: nominal
;;;;     field: source
;;;;   y:
;;;;     type: nominal
;;;;     field: dest
;;;;   color:
;;;;     type: quantitative
;;;;     field: flights
;;;;     aggregate: sum
;;;; ```
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-vega
  "If this `src-resource-or-url` is a valid URL, it is assumed to point to a
  plain text file pointing to  valid `vega-src`; otherwise, it is expected to
  BE a valid `vega-src`.

  Process this `vega-src` string, assumed to be in YAML format, into a
  specification of a Vega chart, and add the plumbing to render it."
  [^String src-resource-or-url ^Integer index]
  (let [data (resource-url-or-data->data src-resource-or-url)
        vega-src (:data data)]
    (log/info "Retrieved vega-src from " (:from data) " `" ((:from data) data) "`")
    (str
      "<div class='data-visualisation' id='vis" index "'></div>\n"
      "<script>\n//<![CDATA[\nvar vl"
      index
      " = "
      (yaml->json (str "$schema: https://vega.github.io/schema/vega-lite/v2.json\n" vega-src))
      ";\nvegaEmbed('#vis"
      index
      "', vl"
      index
      ");\n//]]\n</script>")))
