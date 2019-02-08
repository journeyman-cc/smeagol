(ns smeagol.test
  (:require [clojure.string :as s]
            [clojure.edn :as edn]))

(defn- execute [{:keys [fn in]}]
  (try (let [result (apply fn in)]
         {:result result})
       (catch Exception e
         {:error (.getMessage e)})))

(defn do-test [{:keys [fn in out] :as params}]
  (let [{:keys [error result]} (execute params)]
    (if error
      {:result :error :error error}
      (if (= result out)
        {:result :ok}
        {:result :failure :expected out :actual result}))))

(defn- parse-value [^String line]
  (try
    (let [value (edn/read-string line)]
      {:line line :value value})
    (catch Exception e
      {:line line :error (.getMessage e)})))


(defn- parse-values [texts]
  (reduce (fn [acc text]
            (let [{:keys [value error] :as parsed} (parse-value text)]
              (if error
                (reduced parsed)
                (update-in acc [:values] conj value))))
          {:values []}
          texts))


(defn parse [^String text]
  (let [lines (s/split-lines text)]
    (if (-> lines count (>= 3))
      (let [[sym & rest] lines]
        (if-let [fn (-> sym s/trim symbol resolve)] ;; TODO: require ns
          (let [{:keys [values error line] :as x} (parse-values rest)]
            (if error
              {:error (str "Failed parsing line: " line " due: " error)}
              (let [out (last values)
                    in (butlast values)]
                {:fn fn :out out :in in :text text})))
          {:error (str "No test found with name: " (pr-str sym)) :text text}))
      {:error (str "There shoud be at least 3 lines (test name, input, output), given: " (count lines))
       :text text})))


(defn process [^String text ^Integer index]
  (let [{:keys [error] :as params} (parse text)]
    (if error
      (str "<pre class=\"error\">" (pr-str params) "</pre><pre>" text "</pre>")
      (let [{:keys [result] :as test-result} (do-test params)]
        (str "<pre class=\"" (name result) "\">" (pr-str test-result) "</pre><pre>" text "</pre>")))))
