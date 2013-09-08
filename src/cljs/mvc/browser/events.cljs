(ns mvc.browser.events
    (:require
     [goog.events]
     [cljs.core.async :as async :refer [put! chan <! >!]])
    (:require-macros
     [cljs.core.async.macros :as amacros :refer [go]]
     [mvc.macros :as macros :refer [import-goog]]))

(import-goog [goog.events goog.events.BrowserEvent])

(defn cross-browser-event
  [e]
  (BrowserEvent. e))

(defn unlisten!
  ([event] (unlisten! js/document event))
  ([element event]
     (events/removeAll  element (name event))))

(defn listen!
  ([event handler]
     (listen! js/document event handler))
  ([element event handler]
     (.addEventListener element (name event) handler false)))

(defn event-loop
  ([event handler]
     (event-loop js/document event handler))
  ([element event handler]
     (event-loop element event handler (atom nil)))
  ([element event handler out]
     (let [event-chan (chan)]
       (listen! element event #(put! event-chan (cross-browser-event %)))
       (go (while true
             (let [e (<! event-chan)]
               (.log js/console (clj->js (swap! out handler e))))))
       out)))
