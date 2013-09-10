(ns mvc.impl.transaction
  (:require
   [cljs.core.async :as async :refer [<! >! put! take! alts! chan]]
   [goog.messaging :as messaging :refer [MessageChannel]])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go]]
   [mvc.macros :as m :refer [sync dosync]]))

(def RETRY_LIMIT 10000)
(def LOCK_WAIT_MSECS 100)
(def BARGE_WAIT_NANOS (* 10 1000000))

(def RUNNING 0)
(def COMMITTING 1)
(def RETRY 2)
(def KILLED 3)
(def COMMITTED 4)

(defprotocol ISynchronizer
  (-acquire! [_ acquires])
  (-owns? [_ conditions])
  (-queued? [_])
  (-exclusive? [_])
  (-release! [_ releases])
  (-compare-and-set! [_ old-val new-val]))

(deftype Sync [state]
  IDeref
  (-deref [_] state)

  ISynchronizer
  (-acquire! [_ acquires] (zero? state))
  (-owns? [_ conditions])
  (-queued? [_])
  (-exclusive? [_])
  (-release! [this releases]
    (go (while true
          (let [c state]
            (cond (not (zero? state)) false
                  (-compare-and-set! this c (dec c)) (= 0 (dec c)))))))
  (-compare-and-set! [this old-val new-val]
    (if (= state old-val)
      (do (set! (.-state this) new-val) true)
      false)))

(defn sync
  ([] (sync 0))
  ([state] (Sync. state)))

(defprotocol ICountDownLatch
  (-await [_])
  (-count-down [_])
  (-get-count [_]))

(deftype CountDownLatch [count sync count-chan interrupt-chan]
  ICountDownLatch
  (-await [_]
    (cond (zero? count) count
          (pos? count)
          (go (let [[val port] (alts! [count-chan interrupt-chan])]
                (condp = port
                  count-chan 0
                  interrupt-chan -1)))))
  (-count-down [_] (-release! sync 1))
  (-get-count [_] count))

(defn countdown-latch
  [count]
  (if (pos? count)
    (throw js/Error e "count < 0")
    (CountDownLatch. count (Sync. count) (chan) (chan))))

(defprotocol ILockingTransaction
  (running? [_])
  (-read-point [_])
  (-commit-point [_])
  (-stop [_ status])
  (-lock [_ ]))

(deftype LockingTransaction [transaction status read-point start-point last-point
                             latch start-time
                             ^PersistentQueue actions
                             ^PersistentHashMap vals
                             ^PersistentHashSet sets
                             ^PersistentTreeMap commutes]
  ILockingTransaction
  (running? [_] (or (= status :running) (= status :committing)))
  (-read-point [_] (swap! read-point #(swap! last-point inc)))
  (-commit-point [_] (swap! last-point inc))
  (-stop [_ status]))

(defn locking-transaction
  [f]
  (f))

