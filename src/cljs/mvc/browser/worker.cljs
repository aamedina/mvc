(ns mvc.browser.worker
  (:require
   [cljs.core.async :as async :refer [<! >! put! take! chan timeout alts!]]
   [mvc.browser.fs :as fs])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go]]
   [mvc.macros :as m :refer [with-worker]]))

(def ^:dynamic *worker* nil)

(def ^:dynamic *worker-pool* nil)

(defprotocol IWebWorker)

(deftype WebWorker [])

(defn worker
  []
  (WebWorker.))

(defn worker-pool
  []
  #{})
