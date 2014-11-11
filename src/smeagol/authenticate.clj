(ns smeagol.authenticate (:require [noir.io :as io]))

(defn authenticate
  "Return true if this username/password pair match, false otherwise"
  [username password]
  (let [path (str (io/resource-path) "passwd")
        users (read-string (slurp path))
        user (keyword username)]
    (println (str "Checking for user " user " with password " password " in " users " from " path))
    (.equals (user users) password)))