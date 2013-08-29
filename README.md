# Fusebox: a feature flag system for Clojure and ClojureScript.

## What are feature flags?

In short, a way to control which features are enabled or disabled in a production system, without having to deploy code or suffer downtime. The relevant code is wrapped in a conditional which checks whether a feature flag is enabled, and does the appropriate thing accordingly - when enabled, use the new feature code, and when disabled, do nothing, or perhaps use alternate, older code when the new feature replaces an old one.

One major advantage to using feature flags is that you needn't run separate staging and production systems; you stage new features *in production* by wrapping them with flags. You can then enable flags for the whole system or for individual users, user roles, or groups of users - whatever suits your application's own logic - as and when you need.

Martin Fowler [explains it nicely](http://martinfowler.com/bliki/FeatureToggle.html). [Etsy](http://codeascraft.etsy.com/2011/02/04/how-does-etsy-manage-development-and-operations/), [Square](http://www.infoq.com/presentations/Square), [Asana](http://blog.asana.com/2011/04/using-flags-to-ease-new-feature-development/), [Github](https://github.com/blog/677-how-we-deploy-new-features) and [Flickr](http://code.flickr.net/2009/12/02/flipping-out/) all use feature flags in production.

### Some applications for feature flags:

* Marketing can enable flags when the marketing initiatives are all lined up, without a developer having to get involved.
* Quickly yank features from production when critical bugs are discovered, without having to take down the whole system while performing a rollback and deploy cycle.
* Integrate flags with A/B testing and serve new features to a percentage of your user base.
* Run regression tests with all flags on AND all flags off.
* Provide early beta access for new features to a subset of users.

*Fusebox* is a small abstraction for documenting and enabling feature flags, or *fuses*. It provides no durability for fuses; that's up to you.

## Usage

Add `[cognician/fusebox "0.1.1"]` to your Leiningen project's dependencies.

On clojars at <https://clojars.org/cognician/fusebox>.

### Defining fuses

```clojure
(require '[cognician.fusebox :as fusebox])

(fusebox/add-fuse! :namespace/fuse "Description of the fuse")
```

Fuses are always disabled unless specifically enabled.

### Listing fuses

```clojure
(require '[cognician.fusebox :as fusebox])

(fusebox/fuses)
```

`fusebox/fuses` returns a set of all the fuses that have been defined, in the format:

```clojure
{:fuse :namespace/fuse :description "Description of the fuse" :enabled? <true|false>}
```

Pass the namespace portion as a keyword to get only the fuses in that namespace:

```clojure
(fusebox/fuses :namespace)
```

### Activating fuses

```clojure
(require '[cognician.fusebox :as fusebox])

;; enable individual fuses
(fusebox/enable! :namespace/fuse)

;; enable collections of fuses
(fusebox/enable! #{:namespace/fuse1 :namespace/fuse2})
(fusebox/enable! [:namespace/fuse3 :namespace/fuse4])

;; enable multiple collections of fuses
;; this allows higher level abstractions to set all gathered fuses in one go
(fusebox/enable! #{:namespace/fuse1 :namespace/fuse2} [:namespace/fuse3 :namespace/fuse4])
```

If any fuses are not defined when enabling them, they will be defined automatically, with the string representation of the fuse as the description. Activating fuses is idempotent; fuses can be enabled any number of times with no additional effect beyond the first.

Although it shouldn't be necessary to, fuses can be explicitly disabled with `fusebox/disable!`. Use the same argument options as described for `fusebox/enable!`.

### Checking if fuses are enabled

```clojure
(require '[cognician.fusebox :as fusebox])

(if (fusebox/enabled? :namespace/fuse)
  <enabled code>
  <disabled code>)
```

For the purpose of `fusebox/enabled?`, any fuse not already defined will be treated as disabled.

### Scoping fuses

By default, Fusebox stores fuse able state in a global scope. If you want to explicitly scope fuse state within some logic, for example, from within a scheduled worker task, you can use the `fusebox-macros/scope` macro:

```clojure
(require '[cognician.fusebox.macros :as fusebox-macros])

(fusebox/add-fuse! :namespace/fuse "Description")

(fusebox-macros/scope
  (fusebox/enable! :namespace/fuse)
  ... later on ...
  (if (fusebox/enabled? :namespace/fuse) ;; true - runs 'enabled code'
    <enabled code>
    <disabled code>))

(if (fusebox/enabled? :namespace/fuse) ;; false - runs 'disabled code'
  <enabled code>
  <disabled code>)
```

Each use of `fusebox-macros/scope` makes all the fuses disabled within that scope unless the first form in the body of the macro is :chain. In that case the fuses enabled outside the scope at the time the macro is entered will be enabled inside. Calls to `fusebox-macros/scope` can be nested.

We use a separate macros namespace here so that fusebox can be used in ClojureScript with [lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild)'s [crossovers](https://github.com/emezeske/lein-cljsbuild/blob/master/doc/CROSSOVERS.md).

### Using in ClojureScript

Everything above works in ClojureScript. Sharing fuse data and state between server-side Clojure and client-side ClojureScript is outside the scope of this library; however, it's simple enough to use `fusebox/fuses` on the server and `fusebox/enable` on the client, along with your preferred method of transferring the data from server to client.

### Ring middleware

Ring middleware can be used to ensure that fuses are enabled prior to processing any web request logic. This middleware uses the `fusebox-macros/scope` macro to scope fuses to the request.

```clojure
(require '[cognician.fusebox.middleware :as fusebox-middleware])

(defn get-fuses-for-request
  []
  (...))

;; add to your middleware stack
(fusebox-middleware/wrap-fuses get-fuses-for-request)
```

The `get-fuses-for-request` function above should return a collection (or a collection of collections) of fuse keywords to be used with `fusebox/enable!`. Presumably, this data would come from your durable storage of choice.

## License

Copyright © 2012 Cognician Software (Pty) Ltd

Distributed under the Eclipse Public License, the same as Clojure.
