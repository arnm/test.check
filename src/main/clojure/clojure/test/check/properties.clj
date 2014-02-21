;   Copyright (c) Rich Hickey, Reid Draper, and contributors.
;   All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns clojure.test.check.properties
  (:require [clojure.test.check.generators :as gen]))

;; NOTES:
;; The fields that should be returned from each test
;; run:
;;
;; pass?: boolean or nil (nil implies test was discarded)
;; expect: boolean or (maybe?) exception
;; values: the realized values during this test
;;
;;
;; These fields could be returned:
;;
;; interrupted?
;; stamp: haskell QC nomenclature for stats/values collected
;; callbacks: maybe this is where printing/clojure.test stuff goes?

(defn- apply-gen
  [function]
  (fn [args-rose]
    (gen/gen-pure
      (gen/rose-fmap
        (fn [args] (let [result (try (apply function args) (catch Throwable t t))]
                     {:result result
                      :function function
                      :args args})) args-rose))))

(defn for-all*
  "Creates a property (properties are also generators). A property
  is a generator that generates the result of applying the function
  under test with the realized arguments. Once realized, the arguments
  will be applied to `function` with `apply`.

  Example:

  (for-all* [gen/int gen/int] (fn [a b] (>= (+ a b) a)))
  "
  [args function]
  (gen/gen-bind (apply gen/tuple args)
                (apply-gen function)))

(defn binding-vars
  [bindings]
  (map first (partition 2 bindings)))

(defn binding-gens
  [bindings]
  (map second (partition 2 bindings)))

(defmacro for-all
  "Macro sugar for `for-all*`. `for-all` lets you name the parameter
  and use them in expression, without wrapping them in a lambda. Like
  `for-all*`, it returns a property.

  Examples

  (for-all [a gen/int
            b gen/int]
    (>= (+ a b) a))
  "
  [bindings & body]
  `(for-all* ~(vec (binding-gens bindings))
             (fn [~@(binding-vars bindings)]
               ~@body)))