(ns mvc.browser.core
  (:refer-clojure :exclude [set remove some get filter map extend])
  (:require
   [mvc.browser.protocols]
   [goog.userAgent] [goog.format] [goog.spell.SpellCheck] [goog.object]
   [goog.ui.Menu] [mvc.browser.string :as s :refer [clojurify]])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.object goog.json goog.userAgent goog.format
              goog.format.EmailAddress goog.spell.SpellCheck])

(defn immutable?
  [o]
  (is-immutable-view o))

(defn inherits
  [child-class parent-class]
  (goog/inherits child-class parent-class))

(defn extend!
  ([b] (let [a (js-obj)] (extend! a b)))
  ([a b] (extend a b) a))

(defn clone-js
  [o]
  (js->clj (clone o) :keywordize-keys true))

(defn immutable
  [o]
  (let [obj (extend! o)]
    (create-immutable-view obj)))

(defn js-vals [obj]
  (let [vals (array)]
    (goog.object/forEach obj (fn [val key obj] (.push vals val)))
    vals))

(defn js-kvs [o]
  (let [kvs (array)]
    (goog.object/forEach
     o (fn [v k o]
         (.push kvs (goog.string/toSelectorCase k))
         (.push kvs v)))
    kvs))

(def object? goog/isObject)

(def array-like? goog/isArrayLike)

(defn persistent-object
  ([obj] (persistent-object obj {:clojurify-keys true}))
  ([obj {:keys [clojurify-keys]}]
     (let [out (transient cljs.core.PersistentHashMap/EMPTY)]
       (goog.object/forEach obj (fn [v k o] (assoc! out k v)))
       (persistent! out))))

(defn immutable!
  ([] (immutable! (js-obj)))
  ([mutable]
     (let [clone (immutable mutable)]
       (atom clone
             :meta {:mutable mutable}
             :validator (fn [state] true)))))

