(ns ^{:doc "Photoswipe gallery formatter for Semagol's extendsible markdown
      format."
      :author "Simon Brooke"}
  smeagol.extensions.photoswipe
  (:require [clojure.data.json :as json]
            [clojure.java.io :as cio]
            [clojure.string :as cs]
            [image-resizer.util :refer [buffered-image dimensions]]
            [instaparse.core :as insta]
            [me.raynes.fs :as fs]
            [noir.io :as io]
            [smeagol.configuration :refer [config]]
            [smeagol.extensions.utils :refer :all]
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

(defn process-full-photoswipe
  "Process a specification for a photoswipe gallery, using a JSON
  specification based on that documented on the Photoswipe website."
  [^String spec ^Integer index]
  (str
    "<div class=\"pswp\" id=\"pswp-"
    index "\" tabindex=\"-1\" role=\"dialog\" aria-hidden=\"true\">\n"
    (slurp
      (str (io/resource-path) "html-includes/photoswipe-boilerplate.html"))
    "</div>
    <script>
    \n//<![CDATA[\n
    var pswpElement = document.getElementById('pswp-" index "');
    var spec" index " = "
    spec
    ";
    var gallery" index
    " = new PhotoSwipe( pswpElement, PhotoSwipeUI_Default, spec"
    index ".slides, spec" index ".options);
    if (spec" index ".openImmediately) { gallery" index ".init(); }
    \n//]]\n
    </script>
    <p><button onclick=\"gallery" index
    ".init()\">Open the gallery</button></p>
    </div>"))


(def simple-grammar
  "Parser to transform a sequence of Markdown image links into something we
  can build into JSON. Yes, this could all have been done with regexes, but
  they are very inscrutable."
  (insta/parser "SLIDE := START-CAPTION title END-CAPTION src END-SRC;
                START-CAPTION := '![' ;
                END-CAPTION := '](' ;
                END-SRC := ')' ;
                title := #'[^]]*' ;
                src := #'[^)]*' ;
                SPACE := #'[\\r\\n\\W]*'"))

(defn simplify
  [tree]
  (if
    (coll? tree)
    (case (first tree)
      :SLIDE (remove empty? (map simplify (rest tree)))
      :title tree
      :src tree
      :START-CAPTION nil
      :END-CAPTION nil
      :END-SRC nil
      (remove empty? (map simplify tree)))))

(defn uploaded?
  "Does this `url` string appear to be one that has been uploaded to our
  `uploads` directory?"
  [url]
  (and
    (cs/starts-with? (str url) "content/uploads")
    (fs/exists? (cio/file upload-dir (fs/base-name url)))))

;; (uploaded? "content/uploads/g1.jpg")

(defn slide-merge-dimensions
  "If this `slide` appears to be local, return it decorated with the
  dimensions of the image it references."
  [slide]
  (let [url (:src slide)
        dimensions (try
                     (if (uploaded? url)
                       (dimensions
                         (buffered-image (cio/file upload-dir (fs/base-name url)))))
                     (catch Exception x (.getMessage x)))]
    (if dimensions
      (assoc slide :w (first dimensions) :h (nth dimensions 1))
      (do
        (log/warn "Failed to fetch dimensions of image " url)
        slide))))

;; (slide-merge-dimensions
;;   {:title "Frost on a gate, Laurieston",
;;    :src "content/uploads/g1.jpg"})

(defn process-simple-slide
  [slide-spec]
  (let [s (simplify (simple-grammar slide-spec))
        s'(zipmap (map first s) (map #(nth % 1) s))
        thumbsizes (:thumbnails config)
        thumbsize (first
                    (sort
                      #(> (%1 thumbsizes) (%2 thumbsizes))
                      (keys thumbsizes)))
        url (:url s')
        thumb (if
                (and
                  (uploaded? url)
                  thumbsize)
                (let [p (str (cio/file "uploads" (name thumbsize) (fs/base-name url)))
                      p' (cio/file content-dir p)]
                  (if
                    (and (fs/exists? p') (fs/readable? p'))
                    p)))]
    (slide-merge-dimensions
      (if thumb
        (assoc s' :msrc thumb)
        s'))))

(def process-simple-photoswipe
  "Process a simplified specification for a photoswipe gallery, comprising just
  a sequence of MarkDown image links. This is REALLY expensive to do, we don't
  want to do it often. Hence memoised."
  (memoize
    (fn
      [^String spec ^Integer index]
      (process-full-photoswipe
        (json/write-str
          {:slides (map
                     process-simple-slide
                     (re-seq #"!\[[^(]*\([^)]*\)"  spec))
           ;; TODO: better to split slides in instaparse
           :options { :timeToIdle 100 }
           :openImmediately true}) index))))

;; (map
;;   process-simple-slide
;;   (re-seq #"!\[[^(]*\([^)]*\)"
;;           "![Frost on a gate, Laurieston](content/uploads/g1.jpg)
;;           ![Feathered crystals on snow surface, Taliesin](content/uploads/g2.jpg)
;;           ![Feathered snow on log, Taliesin](content/uploads/g3.jpg)
;;           ![Crystaline growth on seed head, Taliesin](content/uploads/g4.jpg)"))

;; (process-simple-photoswipe
;;   "![Frost on a gate, Laurieston](content/uploads/g1.jpg)
;;   ![Feathered crystals on snow surface, Taliesin](content/uploads/g2.jpg)
;;   ![Feathered snow on log, Taliesin](content/uploads/g3.jpg)
;;   ![Crystaline growth on seed head, Taliesin](content/uploads/g4.jpg)"
;;   1)

(defn process-photoswipe
    [^String url-or-pswp-spec ^Integer index]
  (let [data (resource-url-or-data->data url-or-pswp-spec)
        spec (cs/trim (:data data))]
    (if
      (cs/starts-with? spec "![")
      (process-simple-photoswipe spec index)
      (process-full-photoswipe spec index))))
