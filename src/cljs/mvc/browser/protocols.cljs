(ns mvc.browser.protocols
  (:require
   [goog.string :as string]
   [cljs.core.async.impl.channels :refer [ManyToManyChannel]]))

(defn clone
  [obj & {:keys [clojurify-keys]}]
  (if clojurify-keys
    (let [obj (js->clj (.clone goog.object obj))
          ks (->> (keys obj) (map string/toSelectorCase) (map keyword))]
      (zipmap ks (vals obj)))
    (js->clj (.clone goog.object obj) :keywordize-keys true)))

(defprotocol IAnimatable)

(extend-type js/NodeList
  ISeq
  (-first [nodes]
    (aget nodes 0))
  (-rest [nodes]
    (for [n (range 1 (.-length nodes))]
      (aget nodes n)))
  ISeqable
  (-seq [nodes] nodes))

(extend-type js/HTMLCollection
  ISeq
  (-first [nodes]
    (aget nodes 0))
  (-rest [nodes]
    (for [n (range 1 (.-length nodes))]
      (aget nodes n)))
  ISeqable
  (-seq [nodes] nodes))

(extend-type cljs.core/IndexedSeq
  IMeta
  (-meta [o]
    (.-meta o))
  IWithMeta
  (-with-meta [o meta]
    (set! (.-meta o) meta) o))

(extend-type ManyToManyChannel
  IMeta
  (-meta [o]
    (.-meta o))
  IWithMeta
  (-with-meta [o meta]
    (set! (.-meta o) meta) o))

(extend-type object
  ILookup
  (-lookup [coll k]
    (-lookup coll k nil))
  (-lookup [coll k not-found]
    (if (.hasOwnProperty coll (name k))
      (aget coll (name k))
      not-found)))
