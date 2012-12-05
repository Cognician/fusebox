# Fusebox: a feature flag system for Clojure.

## Usage

Add `[cognician/fusebox "0.1.0"]` to your Leiningen project's dependencies.

On clojars at <https://clojars.org/cognician/fusebox>.

## Usage

### Defining flags

```clojure
(require '[cognician.fusebox :as fusebox])

(fusebox/flag :namespace/flag "Description of the flag")
```

Flags are always inactive unless specifically activated.

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

### Using flags

```clojure
(require '[cognician.fusebox :as fusebox])

(if (fusebox/active? :namespace/flag)
  <enabled code>
  <not enabled code>)
```

### Ring middleware

Ring middleware can be used to ensure that flags are set prior to processing any web request logic.

```clojure
(require '[cognician.fusebox.middleware :as fusebox-middleware])

(defn get-flags-for-session
  []
  (...))

;; add to your middleware stack
(fusebox-middleware/wrap-flags get-flags-for-session)
```

The `get-flags-for-session` function above should return a collection (or a collection of collections) of flags to be used with `fusebox/activate!`.

### Scoping flags

By default, if you're not using the Ring middleware, Fusebox stores flags in a global scope. If you want to explicitly scope flag state within some logic, for example, from within a scheduled worker task, you can use the `fusebox/scope` macro:

```clojure
(require '[cognician.fusebox :as fusebox])

(fusebox/scope
  (fusebox/activate! :namespace/flag)
  ... later on ...
  (if (fusebox/active? :namespace/flag)
    <enabled code>
    <not enabled code>))
```

Each use of `fusebox/scope` makes all the flags inactive. Calls to `fusebox/scope` can be nested.

## License

Copyright © 2012 Cognician Software (Pty) Ltd

Distributed under the Eclipse Public License, the same as Clojure.
