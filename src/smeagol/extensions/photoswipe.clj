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
            [selmer.parser :refer [render]]
            [selmer.util :refer [without-escaping]]
            [smeagol.configuration :refer [config]]
            [smeagol.extensions.utils :refer [resource-url-or-data->data uploaded?]]
            [smeagol.util :refer [content-dir upload-dir]]
            [taoensso.timbre :as log])
  (:gen-class))

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
  (without-escaping
   (render
    (slurp
     (str (io/resource-path) "html-includes/photoswipe-boilerplate.html"))
    {:spec spec :index index})))


(def simple-grammar
  "Parser to transform a sequence of Markdown image links into something we
  can build into JSON. Yes, this could all have been done with regexes, but
  they are very inscrutable."
  (insta/parser "SLIDES := SLIDE | SLIDE SPACE SLIDES;
                SLIDE := START-CAPTION title END-CAPTION src END-SRC;
                START-CAPTION := '![' ;
                END-CAPTION := '](' ;
                END-SRC := ')' ;
                title := #'[^]]*' ;
                src := #'[^)]*' ;
                SPACE := #'[\\s]*'"))


(defn simplify
  "Simplify a parse-`tree` created by `simple-grammar`, q.v."
  [tree]
  (when
   (coll? tree)
    (case (first tree)
      :SLIDES (cons
               (simplify (first (rest tree)))
               (first (simplify (rest (rest tree)))))
      :SLIDE (remove empty? (map simplify (rest tree)))
      :title tree
      :src tree
      :START-CAPTION nil
      :END-CAPTION nil
      :END-SRC nil
      (remove empty? (map simplify tree)))))


(defn slide-merge-dimensions
  "If this `slide` appears to be local, return it decorated with the
  dimensions of the image it references."
  [slide]
  (let [url (:src slide)
        dimensions (try
                     (when (uploaded? url)
                       (dimensions
                        (buffered-image
                         (cio/file upload-dir (fs/base-name url)))))
                     (catch Exception x
                       (log/error
                        "Failed to fetch dimensions of image "
                        url (.getMessage x))
                       nil))]
    (if dimensions
      (assoc slide :w (first dimensions) :h (nth dimensions 1))
      slide)))


(defn find-thumb
  [url thumbsize]
  (when
   (and
    (uploaded? url)
    thumbsize)
    (let [p (str (cio/file "uploads" (name thumbsize) (fs/base-name url)))
          p' (cio/file content-dir p)
          r (str (cio/file "content" p))]
      (if
       (and (fs/exists? p') (fs/readable? p'))
        r
        (do
          (log/warn "Failed to find" thumbsize "thumbnail for" url "at" p')
          nil)))))


(defn process-simple-slide
  "Process a single `slide`, as decoded by `simple-grammar`. At this stage a
  slide is expected to be represented as a sequence of vectors, one for each
  property of the slide (`:title`, `:src`). Each vector contains the name of
  the property as a keyword as its first element, and the value of the
  property as its second element.

  Returns a map of these properties, with, if possible, `:w` (width), `:h`
  (height), and `:msrc` (source URL of a low resolution variant) added."
  [slide]
  (let [s' (zipmap (map first slide) (map #(nth % 1) slide))
        thumbsizes (:thumbnails config)
        thumbsize (first
                   (sort
                    #(> (%1 thumbsizes) (%2 thumbsizes))
                    (keys thumbsizes)))
        url (:src s')
        thumb (find-thumb url thumbsize)]
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
                 (simplify (simple-grammar spec)))
        :options {:timeToIdle 100}
        :openImmediately true}) index))))


(defn process-photoswipe
  "Process a Photoswipe specification which may conform either to the
  `full` or the `simple` syntax."
  [^String url-or-pswp-spec ^Integer index]
  (let [data (resource-url-or-data->data url-or-pswp-spec)
        spec (cs/trim (:data data))]
    (if
     (cs/starts-with? spec "![")
      (process-simple-photoswipe spec index)
      (process-full-photoswipe spec index))))
