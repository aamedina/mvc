(ns mvc.impl.stm)

(defprotocol IRef
  (-alter [_ f args])
  (-commute [_ f args])
  (-ensure [_])
  (-dosync [_ exprs]))

(deftype Ref [state meta revision-id]
  IDeref
  (-deref [_] state)
  
  IMeta
  (-meta [_] meta))

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

(deftype ARef [history min-history max-history])

(deftype AtomicTransaction [status])

(deftype STM []
  IAtomicTransactor
  (-retry [_])
  (-else! [_])
  (-commit! [_])
  (-abort! [_])
  (-reset! [_])
  (-validate! [_]))
