(ns mvc.impl.executor
  (:require
   [mvc.browser.result :refer [Result DependentResult]])
  (:require-macros
   [mvc.macros :as m :refer [future atomic]]))

(def ^:dynamic *callback-executor* nil)

(def ^:dynamic *future-executor* nil)

(defprotocol IDeliver
  (-deliver [promise val])
  (-deliver-exception [promise exception]))

(defprotocol INotify
  (-attend [promise f executor]))

(defprotocol IFail
  (-realized-exception? [promise]))

(defprotocol IFuture
  (cancelled? [_])
  (completed? [_])
  (cancel! [_]))

(deftype Future [])

(defprotocol IExecutor
  (execute [f] "Executes the function at some time in the future."))

(defprotocol IExecutorService
  (await-termination [_ timeout-ms]
    "Block until all tasks have completed after being shutdown or the timeout.")
  (invoke-all [_ tasks timeout-ms]
    "Executes all given tasks and returns a list of statuses and results.")
  (invoke-any [_ tasks timeout-ms]
    "Executes the given tasks returning the result of one that was successful.")
  (shutdown? [_] "Returns true if this executor has been shut down.")
  (terminated? [_] "Returns true if all tasks have completed following shutdown.")
  (shutdown [_] "Initiates an orderly shutdown where enqueued first complete.")
  (shutdown-now [_] "Attempts to terminate all tasks and halts waiting tasks.")
  (submit [_ task result] "Enqueues a value-returning task and returns a Future."))

(deftype SoloExecutor []
  IExecutorService
  (await-termination [_ timeout-ms])
  (invoke-all [_ tasks timeout-ms])
  (invoke-any [_ tasks timeout-ms])
  (shutdown? [_])
  (terminated? [_])
  (shutdown [_])
  (shutdown-now [_])
  (submit [_ task result]))

(deftype PoolExecutor []
  IExecutorService
  (await-termination [_ timeout-ms])
  (invoke-all [_ tasks timeout-ms])
  (invoke-any [_ tasks timeout-ms])
  (shutdown? [_])
  (terminated? [_])
  (shutdown [_])
  (shutdown-now [_])
  (submit [_ task result]))

(defprotocol IAtomicTransactor
  (-retry [_])
  (-else! [_])
  (-commit! [_])
  (-abort! [_])
  (-reset! [_])
  (-validate! [_]))

(defprotocol IAtomicReference
  (-get [_])
  (-compare-and-set! [_ expect update])
  (-get-and-set! [_ new-val])
  (-lazy-set! [_ new-val])
  (-set! [_ new-val])
  (-weak-compare-and-set! [_ expect update]))

(deftype STM []
  IAtomicTransactor
  (-retry [_])
  (-else! [_])
  (-commit! [_])
  (-abort! [_])
  (-reset! [_])
  (-validate! [_]))
