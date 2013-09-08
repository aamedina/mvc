(ns mvc.browser.ajax
  (:require
   [goog.net.XhrIo :as gxhrio]
   [cljs.core.async :as async :refer [put!]]))

(def ^:dynamic *api* "")

(def *timeout* 5000)

(def *headers*
  (clj->js
   {"X-Requested-With" "XMLHttpRequest"
    "Accept" "application/json"}))

(defn xhr
  ([uri sync-chan]
     (xhr uri "GET" nil sync-chan *headers* *timeout*))
  ([uri method sync-chan]
     (xhr uri method nil sync-chan *headers* *timeout*))
  ([uri method content sync-chan]
     (xhr uri method content sync-chan *headers* *timeout*))
  ([uri method content headers timeout sync-chan]
     (xhr uri method content sync-chan headers *timeout*))
  ([uri method content sync-chan headers timeout]
     (goog.net.XhrIo/send
      uri #(put! sync-chan {:status (.getStatus (.-target %))
                            :body (.getResponseText (.-target %))
                            :headers (.getAllResponseHeaders (.-target %))})
      method (first content) headers *timeout* true)
     sync-chan))

(defn GET [uri sync-chan]
  (xhr uri sync-chan))

(defn POST [uri sync-chan payload]
  (xhr uri "POST" payload sync-chan))

(defn PUT [uri sync-chan payload]
  (xhr uri "PUT" payload sync-chan))

(defn DELETE [uri sync-chan]
  (xhr uri "DELETE" sync-chan))
