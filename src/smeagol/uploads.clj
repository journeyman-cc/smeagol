(ns ^{:doc "Handle file uploads."
      :author "Simon Brooke"}
  smeagol.uploads
  (:require [clojure.string :as cs]
            [clojure.java.io :as io]
            [image-resizer.core :refer [resize]]
            [image-resizer.util :refer :all]
            [me.raynes.fs :as fs]
            [smeagol.configuration :refer [config]]
            [taoensso.timbre :as log])
  (:import [java.io File]
           [java.awt Image]
           [java.awt.image RenderedImage BufferedImageOp]
           [javax.imageio ImageIO ImageWriter ImageWriteParam IIOImage]
           [javax.imageio.stream FileImageOutputStream]))

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

(def image-file-extns
  "Extensions of file types we will attempt to thumbnail. GIF is excluded
  because by default the javax.imageio package can read GIF, PNG, and JPEG
  images but can only write PNG and JPEG images."
  #{".jpg" ".jpeg" ".png"})

(defn read-image
  "Reads a BufferedImage from source, something that can be turned into
  a file with clojure.java.io/file"
  [source]
  (ImageIO/read (io/file source)))

(defn write-image
  "Writes img, a RenderedImage, to dest, something that can be turned into
  a file with clojure.java.io/file.
  Takes the following keys as options:
      :format  - :gif, :jpg, :png or anything supported by ImageIO
      :quality - for JPEG images, a number between 0 and 100"
  [^RenderedImage img dest & {:keys [format quality] :or {format :jpg}}]
  (if (or (not quality) (not (contains? #{:jpg :jpeg} format)))
    (ImageIO/write img (name format) (io/file dest))
    (let [fmt (rest (fs/extension (cs/lower-case dest)))
          iw (doto ^ImageWriter (first
                                  (iterator-seq
                                    (ImageIO/getImageWritersByFormatName
                                      "jpeg")))
               (.setOutput (FileImageOutputStream. (io/file dest))))
          iw-param (doto ^ImageWriteParam (.getDefaultWriteParam iw)
                     (.setCompressionMode ImageWriteParam/MODE_EXPLICIT)
                     (.setCompressionQuality (float (/ quality 100))))
          iio-img (IIOImage. img nil nil)]
      (.write iw nil iio-img iw-param))))

(defn auto-thumbnail
  "For each of the thumbnail sizes in the configuration, create a thumbnail
  for the file with this `filename` on this `path`, provided that it is a
  scalable image and is larger than the size."
  ([^String path ^String filename]
    (if
      (image-file-extns (fs/extension (cs/lower-case filename)))
      (let [original (buffered-image (.File (str path filename)))] ;; fs/file?
        (map
          #(auto-thumbnail path filename % original)
          (keys (config :thumbnails))))
      (log/info filename " cannot be thumbnailed.")))
  ([^String path ^String filename size ^RenderedImage image]
   (let [s (-> config :thumbnails size)
         d (dimensions image)]
     (if (and (integer? s) (some #(> % s) d))
       (do
         (write-image (resize image s s) (io/file path (name size) filename))
         (log/info "Created a " size " thumbnail of " filename))
       (log/info filename "is smaller than " s "x" s " and was not scaled to " size)))))

(defn store-upload
  "Store an upload both to the file system and to the database.
  The issue with storing an upload is moving it into place.
  If `params` are passed as a map, it is expected that this is a map from
  an HTTP POST operation of a form with type `multipart/form-data`.

  On success, returns the file object uploaded."
  [params path]
  (let [upload (:upload params)
        tmp-file (:tempfile upload)
        filename (:filename upload)]
    (log/info
      (str "Storing upload file: " upload))
    (log/debug
      (str "store-upload mv file: " tmp-file " to: " path filename))
    (if tmp-file
      (try
        (do
          (.renameTo tmp-file
                     (File. (str path filename))) ;; TODO: fs/file
          (auto-thumbnail path filename)
          (File. (str path filename)))
        (catch Exception x
          (log/error (str "Failed to move " tmp-file " to " path filename "; " (type x) ": " (.getMessage x)))
          (throw x)))
      (throw (Exception. "No file found?")))))
