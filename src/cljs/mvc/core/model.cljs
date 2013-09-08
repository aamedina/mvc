(ns mvc.core.model
  (:require
   [mvc.core.protocols :refer [broadcasting-port pub! sub!]]
   [cljs.core.async :refer [chan <! >! put! take! close!]]))

(defn model
  [{:keys [url] :as spec} attributes]
  (let [chans (reduce #(into %1 {%2 (broadcasting-port)}) {}
                      [:sync :update :destroy :sort :create])
        mdl (with-meta attributes {:url url :channels chans :type :model})]
    (sub! [mdl :sync] (pub! mdl :update mdl))
    mdl))


