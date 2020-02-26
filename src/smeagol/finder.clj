(ns ^{:doc "Find (by doing a 302 redirect to) appropriate files; if no
      appropriate file is found return a 302 redirect to a default file."
      :author "Simon Brooke"}
  smeagol.finder
  (:require [clojure.string :as cs]
            [me.raynes.fs :as fs]
            [noir.io :as io]
            [ring.util.mime-type :refer [ext-mime-type]]
            [ring.util.response :as response]
            [smeagol.configuration :refer [config]]
            [smeagol.util :refer [local-url-base content-dir upload-dir]]
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

;; See:
;; https://github.com/weavejester/compojure/wiki/Routes-In-Detail
;; https://github.com/weavejester/compojure/wiki/Destructuring-Syntax

(defn to-url
  "Given the absolute file path `fqn`, return the relative URL to that path
  within Smeagol, if any, else `nil`."
  [fqn]
  (let [f (when fqn (str fqn))
        l (str local-url-base)
        c (str content-dir)]
    (cond
      (nil? f) nil
      (cs/starts-with? f l) (subs f (count l))
      ;; content-dir may not be within local-url-base
      ;; TODO: potential bad bug: check that when uploads isn't within local-url-base
      ;; the right copies of files are actually getting served!
      (cs/starts-with? f c) (str "content/" (subs f (count c))))))


(defn find-file-on-path
  "Find a file with a name like this `n` on this `path` with
  one of these `extensions`. Question: should we recurse down
  the hierarchy?"
  [n path extensions]
  (let [ext (fs/extension n)
        basename (subs n 0 (- (count n) (count ext)))
        fqn (fs/absolute (fs/file path n))]
    (if (and (fs/exists? fqn) (fs/readable? fqn))
      fqn
      (first
        (remove
          nil?
          (map
            #(let [fqn' (fs/absolute (fs/file path (str basename %)))]
               (when (and (fs/exists? fqn') (fs/readable? fqn'))
                 fqn'))
            extensions))))))


(defn find-file-on-paths
  "Find a file with a name like this `n` on one of these `paths` with
  one of these `extensions`"
  [n paths extensions]
  (first
    (remove
      nil?
      (map
        #(find-file-on-path n % extensions)
        paths))))


(defn with-mime-type-for-file
  [response file]
  (assoc-in
    response
    [:headers "Content-Type"]
    (ext-mime-type (str file))))


(defn find-image
  "Return the first image file found on these `paths` with this
  `requested-name`, if available; this `default-file` otherwise."
  [requested-name default-file paths]
  (let [file (find-file-on-paths requested-name paths
                                 [".gif" ".png" ".jpg" ".jpeg" ".svg"])
        s (if file (str file) default-file)]
    (if file
      (log/info "Found image" requested-name "at" s)
      (log/warn "Failed to find image matching" requested-name))
    (with-mime-type-for-file
      (response/file-response s)
      s)))

(find-image "froboz.jpg" "resources/public/img/Unknown-pin.png"
            [;; TODO: should map over the configured
              ;; thumbnail paths in ascending order
              ;; by size - for map pins, smaller images are
              ;; better.
              (fs/file upload-dir "map-pin")
              (fs/file upload-dir "small")
              (fs/file upload-dir "med")])



;; (response/file-response "resources/public/img/smeagol.png")

;; (def r {:ssl-client-cert nil,
;;         :access-rules [{:redirect "/auth",
;;                         :rule #object[smeagol.handler$user_access 0x7ee9346 "smeagol.handler$user_access@7ee9346"]}],
;;         :protocol "HTTP/1.1",
;;         :cookies {"ring-session" {:value "4e7c059e-2796-44a0-b03a-c712dae43588"}},
;;         :remote-addr "127.0.0.1",
;;         :params {:n "froboz"},
;;         :flash nil,
;;         :route-params {:n "froboz"},
;;         :headers {"cookie" "ring-session=4e7c059e-2796-44a0-b03a-c712dae43588",
;;                   "accept" "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
;;                   "upgrade-insecure-requests" "1", "user-agent" "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:73.0) Gecko/20100101 Firefox/73.0",
;;                   "connection" "keep-alive",
;;                   "host" "localhost:3000",
;;                   "accept-language" "en-GB,en;q=0.7,en-US;q=0.3",
;;                   "accept-encoding" "gzip, deflate",
;;                   "dnt" "1"},
;;         :server-port 3000,
;;         :content-length nil,
;;         :form-params {},
;;         :session/key "4e7c059e-2796-44a0-b03a-c712dae43588",
;;         :query-params {},
;;         :content-type nil,
;;         :character-encoding nil,
;;         :uri "/map-pin/froboz",
;;         :server-name "localhost",
;;         :query-string nil,
;;         :body #object[org.eclipse.jetty.server.HttpInputOverHTTP 0x5abc1216 "HttpInputOverHTTP@5abc1216"],
;;         :multipart-params {},
;;         :scheme :http,
;;         :request-method :get,
;;         :session {:ring.middleware.anti-forgery/anti-forgery-token "2HVXUnBfpuw6kpLTWXTbiSk4zQN5/qPfvJtI/rw5Ju+m/f5I4r5nsOeEr1tuS5YWrXlNRWO6ruX/MHl4",
;;                   :ring.middleware.session-timeout/idle-timeout 1582725564}}
