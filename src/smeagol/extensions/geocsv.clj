(ns ^{:doc "GeoCSV extension for Semagol's extendsible markdown format."
      :author "Simon Brooke"}
  smeagol.extensions.geocsv
  (:require [smeagol.configuration :refer [config]]
            [smeagol.extensions.utils :refer :all]
            [taoensso.timbre :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; Smeagol: an extensible Wiki engine.
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
;;;; Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
;;;; USA.
;;;;
;;;; Copyright (C) 2017 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-geocsv
  "If this `url-or-geo-csv` is a valid URL, it is assumed to point to a CSV file
  containing geographical point data; otherwise, it is expected to be CSV formatted
  text with at least `latitude` and `longitude` columns."
  [^String url-or-geo-csv ^Integer index]
  (let [data (resource-url-or-data->data url-or-geo-csv)
        geo-csv (:data data)]
    (log/info "Retrieved geo-csv from " (:from data) " `" ((:from data) data) "`")
    (str "\n<div class=\"geocsv\" style=\"height: 600px;\" id=\"geocsv-" index
         "\">\n<pre>\n"
         geo-csv
         "\n</pre>
         </div>
         <script>
           //<![CDATA[
             document.onreadystatechange = function () {
               if (document.readyState === 'interactive') {
                 GeoCSV.setIconUrlBase( \""
                 (-> config :formatters :geocsv :icon-url-base) "\");
                 GeoCSV.initialiseMapElement(\"geocsv-" index "\",
                 document.getElementById(\"geocsv-" index "\").innerText.trim().replace(/\\[\\[([^\\[\\]]*)\\]\\]/, \"<a href='wiki?page=$1'>$1</a>\"));
                }};
           //]]
         </script>
         ")))
