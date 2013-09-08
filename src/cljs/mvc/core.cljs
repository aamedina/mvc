(ns mvc.core
  (:require
   [clojure.browser.repl]
   [mvc.browser.protocols]
   [mvc.browser.ajax]
   [mvc.browser.dom]
   [mvc.browser.events]
   [cljs.core.async :as a :refer [>! <! put! take! chan]]
   [dommy.core :as dommy]
   [mvc.examples.core :refer [bootstrap]])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go alts!]]
   [dommy.macros :as m :refer [node sel sel1 deftemplate]]))

(defn js-print [& args]
  (if (js* "typeof console != 'undefined'")
    (.log js/console (apply str args))
    (js/print (apply str args))))

(set! *print-fn* js-print)

(defn connect-repl
  []  
  (go (let [c (chan)
            res (<! (mvc.browser.ajax/GET "http://localhost:3000/repl" c))]
        (dommy/append! (sel1 :body) (node [:script (:body res)])))))

(defn -main
  [& args]
  (bootstrap)
  (connect-repl))

(-main)
