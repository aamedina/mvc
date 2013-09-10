(ns mvc.impl.mutex
  (:require
   [cljs.core.async.impl.protocols :as impl]
   [cljs.core.async :as a :refer [<! >! chan alts!]]
   [mvc.impl.transaction :refer [sync -acquire! -release!]])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go]]))

(defprotocol ILockable
  (-lock [_])
  (-unlock [_]))

(deftype Mutex [sync]
  (-lock [_] (-acquire! sync 1))
  (-unlock [_] (-release! sync 1)))

(defn mutex
  []
  (Mutex. (sync)))
