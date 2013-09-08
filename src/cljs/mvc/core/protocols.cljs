(ns mvc.core.protocols
  (:require
   [cljs.core.async :refer [>! <! put! chan take! sliding-buffer]]
   [mvc.util.routes :refer [absolute-url transform-keys]]
   [mvc.browser.ajax :refer [GET]])
  (:require-macros
   [cljs.core.async.macros :as m :refer [go alts!]]
   [mvc.macros :as macros :refer [go! go-loop dochan]]))

(defn broadcasting-port
  ([] (broadcasting-port (chan (sliding-buffer 50))))
  ([c] (with-meta c {:subscribers (atom #{})})))

(defprotocol IObserverable
  (notify! [_])
  (observe! [_])
  (unobserve [_]))

(defprotocol IEvented
  (add-observer [_])
  (remove-observer [_]))

(defprotocol IDisposable
  (dispose! [_])
  (disposed? [_]))

(defprotocol IPublisher
  (pub! [coll port-key val]))

(defprotocol ISubscriber
  (sub! [coll port-key]))

(defprotocol IRemote
  (update [this])
  (sync [this data])
  (fetch [this])
  (parse [this res])
  (save [this]))

(deftype OneToManyChannel [])

(extend-protocol IPublisher
  object
  (pub! [coll port-key val] (println "pub!")))

(extend-protocol ISubscriber
  object
  (sub! [coll port-key & body] (println "sub!")))

(extend-protocol IRemote
  object
  (update [this updated]
    (let [updated
          (condp = (:type (meta this))
            :model
            (with-meta updated (meta this))
            :collection
            (reduce
             (fn [coll model]
               (conj coll model))
             (with-meta #{} (meta this))
             updated))]
      (pub! this :update updated)
      updated))
  
  (sync [this data]
    (let [parsed
          (condp = (:type (meta this))
            :model
            (-> (parse this data)
                (transform-keys)
                (with-meta (meta this)))
            :collection
            (->> (parse this data)
                 (reduce
                  (fn [coll attrs]
                    (conj coll ((:model (meta this)) (transform-keys attrs))))
                  (with-meta #{} (meta this)))))]
      (pub! this :sync parsed)
      parsed))

  (save [this] (clj->js this))

  (parse
    [this res]
    (let [res (if (string? res) (.parse goog.json res) res)]
      (clojure.walk/keywordize-keys (js->clj (if (.-data res)
                                               (.-data res)
                                               res)))))

  (fetch [this]
    (go
     (let [res (<! (GET (absolute-url this) (chan)))]
       (if (or (= (:status res) 200) (= (:status res) 201))
         (sync this (:body res)))))))


