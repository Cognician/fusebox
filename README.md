# Fusebox: a feature flag system for Clojure and ClojureScript.

## Usage

Add `[cognician/fusebox "0.1.0"]` to your Leiningen project's dependencies.

On clojars at <https://clojars.org/cognician/fusebox>.

## Usage

### Defining flags

```clojure
(require '[cognician.fusebox :as fusebox])

(fusebox/create-flag! :namespace/flag "Description of the flag")
```

Flags are always inactive unless specifically activated.

### Listing flags

```clojure
(require '[cognician.fusebox :as fusebox])

(fusebox/flags)
```

`fusebox/flags` returns a set of all the flags that have been defined, in the format:

```clojure
{:flag :namespace/flag :description "Description of the flag"}
```

Pass the namespace portion as a keyword to get only the flags in that namespace:

```clojure
(fusebox/flags :namespace)
```

### Activating flags

```clojure
(require '[cognician.fusebox :as fusebox])

;; activate individual flags
(fusebox/activate! :namespace/flag)

;; activate collections of flags
(fusebox/activate! #{:namespace/flag1 :namespace/flag2})
(fusebox/activate! [:namespace/flag3 :namespace/flag4])

;; activate multiple collections of flags
;; this allows higher level abstractions to set all gathered flags in one go
(fusebox/activate! #{:namespace/flag1 :namespace/flag2} [:namespace/flag3 :namespace/flag4])
```

If any flags are not defined when activating them, they will be defined automatically, with the string representation of the flag as the description.

Although it shouldn't be necessary to, flags can be explicitly deactivated with `fusebox/deactivate!`. Use the same argument options as described for `fusebox/activate!`.

### Checking if flags are active

```clojure
(require '[cognician.fusebox :as fusebox])

(if (fusebox/active? :namespace/flag)
  <enabled code>
  <not enabled code>)
```

For the purpose of `fusebox/active?`, any flag not already defined will be treated as inactive.

### Ring middleware

Ring middleware can be used to ensure that flags are set prior to processing any web request logic.

```clojure
(require '[cognician.fusebox.middleware :as fusebox-middleware])

(defn get-flags-for-request
  []
  (...))

;; add to your middleware stack
(fusebox-middleware/wrap-flags get-flags-for-request)
```

The `get-flags-for-request` function above should return a collection (or a collection of collections) of flags to be used with `fusebox/activate!`.

### Scoping flags

By default, if you're not using the Ring middleware, Fusebox stores flags in a global scope. If you want to explicitly scope flag state within some logic, for example, from within a scheduled worker task, you can use the `fusebox/scope` macro:

```clojure
(require '[cognician.fusebox.macros :as fusebox-macros])

(fusebox-macros/scope
  (fusebox/activate! :namespace/flag)
  ... later on ...
  (if (fusebox/active? :namespace/flag)
    <enabled code>
    <not enabled code>))
```

Each use of `fusebox-macros/scope` makes all the flags inactive. Calls to `fusebox-macros/scope` can be nested.

We use a separate macros namespace here so that fusebox can be used in ClojureScript with [lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild)'s [crossovers](https://github.com/emezeske/lein-cljsbuild/blob/master/doc/CROSSOVERS.md).

### Using in ClojureScript

Everything above (aside from the Ring middleware) works in ClojureScript. Sharing flags between server-side Clojure and client-side ClojureScript is outside the scope of this library. However, it's simple enough to use `fusebox/flags` on the server and `fusebox/activate` on the client, with your preferred method of transferring the data in between.

## License

Copyright © 2012 Cognician Software (Pty) Ltd

Distributed under the Eclipse Public License, the same as Clojure.
