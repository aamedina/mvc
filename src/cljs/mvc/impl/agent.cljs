(ns mvc.impl.agent
  (:require
   [cljs.core.async :as async :refer [>! <! put! chan timeout alts!]]
   [cljs.core.async.impl.protocols :as impl]
   [cljs.core.async.impl.dispatch :as dispatch]
   [cljs.core.async.impl.buffers :as buffers]
   [mvc.browser.core :as browser :refer [immutable immutable?]]
   [mvc.browser.db :as db]
   [mvc.impl.executor :as executor :refer [PoolExecutor SoloExecutor]])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go]]
   [mvc.macros :as m :refer [future]]))

(def ^:dynamic *agent* nil)

(def ^:dynamic *agent-pool* nil)

(defn queue
  ([] PersistentQueue/EMPTY)
  ([& xs]
     (when (first xs)
       (let [front (first xs) rear (rest xs) num (count xs)]
         (cljs.core/PersistentQueue. nil num front (if rear rear []) 0)))))

(deftype DerefChannel [takes ^:mutable dirty-takes puts ^:mutable dirty-puts
                       ^not-native buf ^:mutable closed state meta action-queue]
  impl/WritePort
  (put! [this val handler])

  impl/ReadPort
  (take! [this handler])

  impl/Channel
  (close! [this])

  IEquiv
  (-equiv [o other] (identical? o other))
  
  IDeref
  (-deref [_] state)
  
  IMeta
  (-meta [_] meta)

  IHash
  (-hash [this] (goog/getUid this)))

(defn deref-chan
  [buf]
  (DerefChannel. (buffers/ring-buffer 32) 0
                 (buffers/ring-buffer 32) 0 buf nil nil {}
                 (queue)))

(defprotocol IAgent
  (-shutdown! [_])
  (-set-error-handler! [_])
  (-set-error-mode! [_])
  (-error-handler [_])
  (-restart! [_])
  (-error [_])
  (-release-pending! [_])
  (-enqueue! [_])
  (-enqueued-count [_])
  (-dispatch! [_]))

(defprotocol IFailed
  (get-error [_]))

(defprotocol IAction
  (-execute [_])
  (-dorun [_]))

(deftype Agent [state meta validator error-handler error-mode watches
                send-counter send-off-counter pooled-executor solo-executor nested]
  IEquiv
  (-equiv [o other] (identical? o other))
  
  IDeref
  (-deref [_] state)
  
  IMeta
  (-meta [_] meta)
  
  IPrintWithWriter
  (-pr-writer [a writer opts]
    (-write writer "#<Agent: ")
    (pr-writer state writer opts)
    (-write writer ">"))
  
  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key)))
  
  IHash
  (-hash [this] (goog/getUid this))

  )

(defn agent
  ([] (agent 0 {} nil nil))
  ([state & {:keys [meta validator error-handler error-mode]
             :or {meta {} validator nil error-handler nil}}]     
     (Agent. state meta validator error-handler
             (if error-handler :continue :fail) nil 0 0
             (PoolExecutor.) (SoloExecutor.) nil)))

(defn send-via
  [executor agent f & args])

(defn send
  [agent f & args])

(defn send-off
  [agent f & args])

(defn release-pending-sends
  [])

(defn agent-error
  [a])

(defn restart-agent
  [a new-state & options])

(defn set-error-handler!
  [a handler-fn])

(defn error-handler
  [a])

(defn set-error-mode!
  [a mode-keyword])

(defn error-mode
  [])

(defn shutdown-agents
  [])

(defn await
  [& agents])

(defn await-for
  [timeout-ms & agents])

(defn commute
  [ref fun & args])

(defn alter
  [ref f & args])

(defprotocol IClojureScript
  (-js->cljs [_]))

(extend-protocol IClojureScript
  array
  (-js->cljs [o] (js->clj o))

  boolean
  (-js->cljs [o] (js->clj o))

  js/Date
  (-js->cljs [o] (js->clj o))

  number
  (-js->cljs [o] (js->clj o))

  string
  (-js->cljs [o] (js->clj o))

  object
  (-js->cljs [o] (js->clj o))

  nil
  (-js->cljs [o] nil)

  default
  (-js->cljs [js-obj]
    (let [cljs-obj (js->clj js-obj)]
      (with-meta cljs-obj {:obj js-obj}))))

(defprotocol IJavaScript
  (-cljs->js [_]))

(extend-protocol IJavaScript
  PersistentVector
  (-cljs->js [v] v)

  PersistentHashMap
  (-cljs->js [v] v)

  PersistentHashSet
  (-cljs->js [v] v)

  PersistentTreeSet
  (-cljs->js [v] v)

  PersistentTreeMap
  (-cljs->js [v] v)

  PersistentArrayMap
  (-cljs->js [v] v)

  PersistentQueue
  (-cljs->js [v] v)

  object
  (-cljs->js [v] v)

  nil
  (-cljs->js [_] nil)
  
  default
  (-cljs->js [v] v))

(defn cljs->js
  [cljs-obj]
  (-cljs->js cljs-obj))

(defn js->cljs
  [js-obj]
  (-js->cljs js-obj))
