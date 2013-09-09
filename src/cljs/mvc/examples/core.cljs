(ns mvc.examples.core
  (:require
   [cljs.core.async :as a :refer [>! <! put! take! chan]]
   [mvc.impl.compiler]
   [dommy.core :as dommy]
   [mvc.ui.meny :as meny])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go alts!]]
   [mvc.macros :as m :refer [defcollection defmodel defpartial]]
   [dommy.macros :as dm :refer [sel sel1]]))

(def ads-api "http://192.241.130.213:8080/user/15/ads-api/")

(def rest-api "http://192.241.130.213:8080/user/15/rest-api/")

(defmodel account
  {:url "accounts/:id"})

(defcollection accounts
  {:url "accounts" :model account})

(defmodel campaign
  {:url "accounts/:account-id/campaigns/:id"})

(defcollection campaigns
  {:url "accounts/:id/campaigns" :model campaign})

(defpartial test-node
  [k]
  [:h1#head-id {:data-click (fn [e] (.log js/console ads-api))} "hi"]
  (for [n (range k)]
    [:li#clickable {:data-click (fn [e] (.log js/console e))} n]))

(defn bootstrap
  [& args]
  (println "Hello, examples!")
  (dommy/append! (sel1 :body) (meny/meny)))
