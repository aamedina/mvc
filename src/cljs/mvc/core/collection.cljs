(ns mvc.core.collection
  (:require
   [mvc.core.protocols :as p :refer [broadcasting-port pub! sub!]]
   [cljs.core.async :as a :refer [chan <! >! put! take! close!]]))

(defn collection
  [{:keys [url model] :as spec}
   & {:keys [for models channels] :or {models #{}}}]
  (let [chans (reduce #(into %1 {%2 (broadcasting-port)}) {}
                      [:sync :update :destroy :sort])
        coll (with-meta models {:url url :channels chans :type :collection
                                :model model :for for})]
    (sub! [coll :sync] (pub! coll :update coll))
    coll))
