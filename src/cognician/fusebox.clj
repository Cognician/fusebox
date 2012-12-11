(ns cognician.fusebox
  (:require [clojure.walk :as walk]))

(def fuses* (atom {}))
(def ^:dynamic fuse-states* (atom {}))

(defn make-fuse
  "Make a fuse structure ready for storage in memory."
  [fuse description]
  {:fuse fuse :description description})

(defn has-fuse?
  "Check whether a fuse has been defined."
  [fuse]
  (not (nil? (get @fuses* fuse))))

(defn reset-fuse-states!
  []
  (reset! fuse-states* {}))

(defn add-fuse!
  "Add a fuse definition."
  [fuse description]
  (let [f (make-fuse fuse description)]
    (swap! fuses* assoc fuse f)
    f))

(defn fuses
  "Get a list of defined fuses, optionally restricted by namespace."
  ([]
     (vals @fuses*))
  ([namespace]
     (let [pattern (re-pattern (str "^" (str namespace)))]
       (vals (into {} (filter (fn [[k v]]
                                (re-find pattern (str k)))
                              @fuses*))))))

(defn set-enabled?
  "Set the enabled? status for a fuse or collection of fuses or collection of collections of fuses."
  [enabled? & fuses]
  (doseq [fuse (flatten (walk/postwalk (fn [item] (if (set? item) (seq item) item)) fuses))]
    (when (keyword? fuse)
      (if-not (has-fuse? fuse)
        (add-fuse! fuse (str fuse)))
      (swap! fuse-states* assoc fuse enabled?))))

(defn enable!
  "Make fuses enabled."
  [& fuses]
  (apply set-enabled? true fuses))

(defn disable!
  "Make fuses disabled."
  [& fuses]
  (apply set-enabled? false fuses))

(defn enabled?
  "Check whether a fuse is enabled."
  [fuse]
  (true? (get @fuse-states* fuse)))