(ns mvc.impl.agent
  (:require
   [cljs.core.async :as async :refer [>! <! put! chan timeout alts!]]
   [cljs.core.async.impl.protocols :as impl]
   [cljs.core.async.impl.dispatch :as dispatch]
   [cljs.core.async.impl.buffers :as buffers]
   [mvc.browser.core :as browser :refer [immutable!]]
   [mvc.browser.db :as db] [goog.ui.Menu]
   [mvc.impl.executor :as executor
    :refer [PoolExecutor SoloExecutor execute]])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go]]
   [mvc.macros :as m :refer [future with-agent]]))

(def ^:dynamic *agent* nil)

(def ^:dynamic *agent-pool* nil)

(defn queue
  ([] cljs.core.PersistentQueue/EMPTY)
  ([& xs]
     (when (first xs)
       (let [front (first xs) rear (rest xs) num (count xs)]
         (cljs.core/PersistentQueue. nil num front (if rear rear []) 0)))))

(defprotocol IAgent
  (-send [agent f args])
  (-send-off [agent f args])
  ;; (-restart-agent [agent new-state options])
  ;; (-shutdown! [_])
  ;; (-set-error-handler! [_])
  ;; (-set-error-mode! [_])
  ;; (-error-handler [_])
  ;; (-restart! [_])
  ;; (-error [_])
  ;; (-release-pending! [_])
  (-enqueue! [agent f args])
  (-enqueued-count [_])
  ;; (-dispatch! [_])
  )

(deftype Agent [state meta validator error-handler error-mode watches
                send-counter send-off-counter action-queue]
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

  IAgent
  (-send [agent f args]
    (let [args (if (seq args) args (list))]
      (if (and (:mutable (meta agent)) (keyword? f))
        (let [obj (:mutable (meta agent))
              f (cond (get obj f) (get obj f)
                      (get obj (goog.string/toCamelCase (name f)))
                      (get obj (goog.string/toCamelCase (name f))))]
          (put! action-queue #(immutable! (apply f (cons obj args)))))
        (put! action-queue #(apply f (cons @agent args))))))
  (-send-off [agent f args]
    (put! action-queue (partial f (cons @agent args))))
  (-enqueue! [_])
  (-enqueued-count [_])

  )

(defn agent
  ([] (agent 0 {} nil nil))
  ([state & {:keys [meta validator error-handler error-mode]
             :or {meta {} validator nil error-handler nil}}]
     (let [action-queue (chan)
           a (->Agent state meta validator error-handler
                      (if error-handler :continue :fail) nil 0 0 action-queue)]
       (go (while true
             (let [f (<! action-queue)]
               (try (let [new-value (f)]
                      (reset! a new-value))
                    (catch js/Error e "there was an error!")))))
       a)))

(defn send-via
  [executor agent f & args])

(defn send
  [agent f & args]  
  (-send agent f args)
  agent)

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
