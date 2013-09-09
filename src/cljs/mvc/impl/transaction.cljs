(ns mvc.impl.transaction
  (:require
   [cljs.core.async :as async :refer [<! >! put! take! alts!]])
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
  (-release! [_])
  (-compare-and-set! [_ old-val new-val]))

(deftype Sync [state]
  IDeref
  (-deref [_] state)

  ISynchronizer
  (-acquire! [_ acquires] (zero? state))
  (-owns? [_ conditions])
  (-queued? [_])
  (-exclusive? [_])
  (-release! [this]
    (go (while true
          (let [c state]
            (cond (not (zero? state)) false
                  (-compare-and-set! this c (dec c)) (= 0 (dec c)))))))
  (-compare-and-set! [_ old-val new-val]
    (if (= state old-val)
      (do (reset! state new-val) true)
      false)))

(defprotocol ICountDownLatch
  (-await [_])
  (-count-down [_])
  (-get-count [_]))

(deftype CountDownLatch [count]
  ICountDownLatch
  (-await [_])
  (-count-down [_])
  (-get-count [_]))

(defprotocol ILockingTransaction
  (running [_]))

(deftype LockingTransaction [transaction status start-point latch])

(defn locking-transaction
  [f]
  (f))
