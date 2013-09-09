(ns mvc.browser.worker
  (:refer-clojure :exclude [-count])
  (:require
   [cljs.core.async :as async :refer [<! >! put! take! chan timeout alts!]]
   [mvc.browser.fs :as fs])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go]]
   [mvc.macros :as m :refer [with-worker]]))

(def ^:dynamic *worker* nil)

(def ^:dynamic *worker-pool* nil)

(defprotocol IWorker
  (-handler [_])
  (-add-handler! [_ f]))

(defprotocol IWorkerPool
  (-enqueue [_])
  (-dequeue [_])
  (-count [_])
  (-log [_]))

(defprotocol IWebWorker)

(deftype WebWorker [])

(defn worker
  []
  (WebWorker.))

(defn worker-pool
  []
  #{})
