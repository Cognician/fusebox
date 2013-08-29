(ns cognician.fusebox.macros
  (:require [cognician.fusebox :as fusebox]))

(defmacro scope
  [& body]
  (let [chain (first body)
        chain? (and chain (keyword? chain) (= chain :chain))
        fuse-init (if chain?
                    '@cognician.fusebox/fuse-states*
                    '{})
        body (if chain?
               (next body)
               body)]
   `(binding [fusebox/fuse-states* (atom ~fuse-init)]
      ~@body)))
