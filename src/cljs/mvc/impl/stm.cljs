(ns mvc.impl.stm)

(defprotocol IAtomicTransaction)

(defprotocol IAtomicTransactor
  (-retry [_])
  (-else! [_])
  (-commit! [_])
  (-abort! [_])
  (-reset! [_])
  (-validate! [_]))

(defprotocol IAtomicReference
  (-get [_])
  (-compare-and-set! [_ expect update])
  (-get-and-set! [_ new-val])
  (-lazy-set! [_ new-val])
  (-set! [_ new-val])
  (-weak-compare-and-set! [_ expect update]))

(defprotocol IAtomicInteger
  (-add-and-get! [_ delta])
  (-get-and-add! [_ delta])
  (-dec-and-get! [_])
  (-get-and-dec! [_])
  (-inc-and-get! [_])
  (-get-and-inc! [_]))

(defprotocol ICountDownLatch
  (await [_]))

(deftype CountDownLatch [count]
  ICountDownLatch
  (await [_]))

(deftype Ref [history min-history max-history])

(deftype AtomicTransaction [status])

(deftype STM []
  IAtomicTransactor
  (-retry [_])
  (-else! [_])
  (-commit! [_])
  (-abort! [_])
  (-reset! [_])
  (-validate! [_]))
