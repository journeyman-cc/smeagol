(ns ^{:doc "Very simple extension for testing the extension processing flow."
      :author "Simon Brooke"}
  smeagol.extensions.test)


(def process-test-return-value "<!-- The test extension has run and this is its output -->")

(defn process-test
    [^String fragment ^Integer index]
    process-test-return-value)
