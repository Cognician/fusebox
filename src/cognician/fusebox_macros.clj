(ns cognician.fusebox-macros
  (:require [cognician.fusebox :as fusebox]))

(defmacro scope
  [& body]
  `(binding [fusebox/fuse-states* (atom {})]
     ~@body))