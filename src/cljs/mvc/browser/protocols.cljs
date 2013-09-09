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
  ICounted
  (-count [this] (.-length this))
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

(defn pr-type
  [v]
  (cond (string? v) v
        (array? v) v
        (number? v) v
        (fn? v) "[function Function]"
        (nil? v) "null"
        :else  (.toString v)))

(defn pr-object-writer
  [writer print-one begin sep end opts obj]
  (-write writer begin)
  (let [ks (js-keys obj)]
    (when (seq ks)
      (print-one {(first ks) (pr-type (aget obj (first ks)))} writer opts)
      (doseq [k (next ks)]
        (-write writer sep)
        (print-one {k (pr-type (aget obj k))} writer opts)))    
    (-write writer end)))

(extend-type object
  ILookup
  (-lookup
    ([o k] (aget o (name k)))
    ([o k not-found]
       (let [k (name k)]
         (if (goog.object/containsKey o k)
           (aget o k)
           not-found))))

  ICounted
  (-count [kvs] (goog.object/getCount kvs))  
  
  IPrintWithWriter
  (-pr-writer [obj writer opts]
    (let [pr-pair (fn [kv]
                    (pr-object-writer writer pr-writer "" " " "" opts kv))]
      (pr-object-writer writer pr-writer "{" ", " "}" opts obj))))
