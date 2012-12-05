# Fusebox: a feature flag system for Clojure.

## Usage

Add [cognician/fusebox "0.1.0"] to your Leiningen project's dependencies.

On clojars at https://clojars.org/cognician/fusebox.

## Usage

### Defining flags:

```clojure
(require '[cognician.fusebox :as fusebox])

(fusebox/flag :namespace/flag "Description of the flag")
```

Flags are always deactivated unless specifically activated.

### Activating flags:

```clojure
(require '[cognician.fusebox :as fusebox])

;; toggle individual flags
(fusebox/activate! :namespace/flag)

;; toggle collections of flags
(fusebox/activate! #{:namespacqe/flag1 :namespace/flag2})
```

To deactivate flags, simply use `fusebox/deactivate!` with the same argument options described above.

### Using flags:

```clojure
(require '[cognician.fusebox :as fusebox])

(if (fusebox/active? :namespace/flag)
  <enabled code>
  <not enabled code>)
```

## License

Copyright © 2012 Cognician Software (Pty) Ltd

Distributed under the Eclipse Public License, the same as Clojure.
