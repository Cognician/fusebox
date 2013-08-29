(ns cognician.test.fusebox
  (:use midje.sweet)
  (:require [cognician.fusebox :as f]
            [cognician.fusebox.macros :as fm]))

(defn reset-fuses!
  []
  (reset! f/fuses* {})
  (f/reset-fuse-states!))

(fact "Make fuses."
  (f/make-fuse :namespace/fuse "Description")
  => {:fuse :namespace/fuse :description "Description"})

(fact "Add fuses."
  (reset-fuses!)
  (let [fuse (f/make-fuse :namespace/fuse "Description")]
    (f/add-fuse! :namespace/fuse "Description") => fuse
    @f/fuses* => {(:fuse fuse) fuse}))

(fact "Listing fuses."
  (reset-fuses!)
  (f/add-fuse! :namespace/fuse "Description")
  (f/add-fuse! :namespace/fuse2 "Description 2")
  (f/fuses) => (contains (list (f/make-fuse :namespace/fuse "Description")
                               (f/make-fuse :namespace/fuse2 "Description 2")) :in-any-order))

(fact "Listing fuses for a namespace."
  (reset-fuses!)
  (f/add-fuse! :namespace/fuse "Description")
  (f/add-fuse! :namespace2/fuse "Description 2")
  (f/fuses :namespace) => (contains (list (f/make-fuse :namespace/fuse "Description")) :in-any-order)
  (f/fuses :namespace2) => (contains (list (f/make-fuse :namespace2/fuse "Description 2")) :in-any-order))

(fact "Check whether a fuse is enabled."
  (reset-fuses!)
  (f/add-fuse! :namespace/fuse "Description")
  (f/enabled? :namespace/fuse) => false
  (f/enable! :namespace/fuse)
  (f/enabled? :namespace/fuse) => true)

(fact "Activate fuses."
  (reset-fuses!)
  (f/add-fuse! :namespace/fuse "Description")
  (f/enable! :namespace/fuse)
  (f/enabled? :namespace/fuse) => true
  (reset-fuses!)
  (f/add-fuse! :namespace/fuse2 "Description 2")
  (f/enable! [:namespace/fuse :namespace/fuse2])
  (f/enabled? :namespace/fuse) => true
  (f/enabled? :namespace/fuse2) => true
  (reset-fuses!)
  (f/add-fuse! :namespace/fuse2 "Description 2")
  (f/enable! #{:namespace/fuse2} [:namespace/fuse])
  (f/enabled? :namespace/fuse) => true
  (f/enabled? :namespace/fuse2) => true)

(fact "Activating or deenabling undefined fuses automatically defines them."
  (reset-fuses!)
  (f/disable! :namespace/fuse)
  @f/fuses* => {:namespace/fuse {:fuse :namespace/fuse :description ":namespace/fuse"}}
  (reset-fuses!)
  (f/enable! :namespace/fuse)
  @f/fuses* => {:namespace/fuse {:fuse :namespace/fuse :description ":namespace/fuse"}})

(fact "Disable fuses."
  (reset-fuses!)
  (f/add-fuse! :namespace/fuse "Description")
  (f/enable! :namespace/fuse)
  (f/enabled? :namespace/fuse) => true
  (f/disable! :namespace/fuse)
  (f/enabled? :namespace/fuse) => false)

(fact "Scoping fuses."
  (reset-fuses!)
  (let [fuse (f/make-fuse :namespace/fuse "Description")]
    (f/add-fuse! :namespace/fuse "Description")
    (f/enable! :namespace/fuse)
    (f/enabled? :namespace/fuse) => true
    (fm/scope
     (f/enabled? :namespace/fuse) => false)
    (f/enabled? :namespace/fuse) => true
    (fm/scope
     (f/enabled? :namespace/fuse) => false
     (fm/scope
      (f/enable! :namespace/fuse)
      (f/enabled? :namespace/fuse) => true)
     (f/enabled? :namespace/fuse) => false)))

(fact "Scoping fuses with chain."
  (reset-fuses!)
  (let [fuse (f/make-fuse :namespace/fuse "Description")]
    (f/add-fuse! :namespace/fuse "Description")
    (f/enable! :namespace/fuse)
    (f/enabled? :namespace/fuse) => true
    (fm/scope :chain
     (f/enabled? :namespace/fuse) => true)
    (f/enabled? :namespace/fuse) => true
    (fm/scope :chain
              (f/enabled? :namespace/fuse) => true
              (f/disable! :namespace/fuse)
              (f/enabled? :namespace/fuse) => false)
    (f/enabled? :namespace/fuse) => true))
