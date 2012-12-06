(ns cognician.fusebox.middleware
  (:require [cognician.fusebox :as fusebox]
            [cognician.fusebox.macros :as fusebox-macros]))

(defn wrap-fuses
  [handler fuses-fn]
  (fn [request]
    (fusebox-macros/scope
     (apply fusebox/enable! (fuses-fn))
     (handler request))))