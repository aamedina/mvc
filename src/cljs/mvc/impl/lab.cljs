(ns mvc.impl.lab
  (:require
   [cljs.core.async :as async]
   [cljs.core.async.impl.protocols :as impl]
   ;; [clojure.core.async.impl.mutex :as mutex]
   [cljs.core.async.impl.dispatch :as dispatch]
   [cljs.core.async.impl.channels :as channels]))

;; (deftype MultiplexingReadPort
;;     [^Lock mutex ^Set read-ports]
;;   impl/ReadPort
;;   (take! [this handler]
;;     (if (empty? read-ports)
;;       (channels/box nil)
;;       (do
;;         (.lock mutex)
;;         (let [^Lock handler handler
;;               commit-handler (fn []
;;                                (.lock handler)
;;                                (let [take-cb (and (impl/active? handler) (impl/commit handler))]
;;                                  (.unlock handler)
;;                                  take-cb))
;;               fret (fn [[val alt-port]]
;;                      (if (nil? val)
;;                        (do (.lock mutex)
;;                            (.remove read-ports alt-port)
;;                            (.unlock mutex)
;;                            (impl/take! this handler))
;;                        (when-let [take-cb (commit-handler)]
;;                          (dispatch/run #(take-cb val)))))
;;               current-ports (seq read-ports)]
;;           (if-let [alt-res (async/do-alts fret current-ports {})]
;;             (let [[val alt-port] @alt-res]
;;               (if (nil? val)
;;                 (do (.remove read-ports alt-port)
;;                     (.unlock mutex)
;;                     (recur handler))
;;                 (do (.unlock mutex)
;;                     (when-let [take-cb (commit-handler)]
;;                       (dispatch/run #(take-cb val))))))
;;             (do
;;               (.unlock mutex)
;;               nil)))))))

;; (defn multiplex
;;   "Returns a multiplexing read port which, when read from, produces a
;;   value from one of ports.

;;   If at read time only one port is available to be read from, the
;;   multiplexing port will return that value. If multiple ports are
;;   available to be read from, the multiplexing port will return one
;;   value from a port chosen non-deterministicly. If no port is
;;   available to be read from, parks execution until a value is
;;   available."
;;   [& ports]
;;   (->MultiplexingReadPort (mutex/mutex) (HashSet. ^Collection ports)))

;; (defn- broadcast-write
;;   [port-set val handler]
;;   (if (= (count port-set) 1)
;;     (impl/put! (first port-set) val handler)
;;     (let [clauses (map (fn [port] [port val]) port-set)
;;           recur-step (fn [[_ port]] (broadcast-write (disj port-set port) val handler))]
;;       (when-let [alt-res (async/do-alts recur-step clauses {})]
;;         (recur (disj port-set (second @alt-res))
;;                val
;;                handler)))))

;; (deftype BroadcastingWritePort
;;     [write-ports]
;;   impl/WritePort
;;   (put! [port val handler]
;;     (broadcast-write write-ports val handler)))

;; (defn broadcast
;;   "Returns a broadcasting write port which, when written to, writes
;;   the value to each of ports.

;;   Writes to the broadcasting port will park until the value is written
;;   to each of the ports used to create it. For this reason, it is
;;   strongly advised that each of the underlying ports support buffered
;;   writes."
;;   [& ports]
;;   (->BroadcastingWritePort (set ports)))

;; ;;;; Tools for creating processes

;; (defn spool
;;   "Take a sequence and puts each value on a channel and returns the channel.
;;    If no channel is provided, an unbuffered channel is created. If the
;;    sequence ends, the channel is closed."
;;   ([s c]
;;      (async/go
;;       (loop [[f & r] s]
;;         (if f
;;           (do
;;             (async/>! c f)
;;             (recur r))
;;           (async/close! c))))
;;      c)
;;   ([s]
;;      (spool s (async/chan))))
