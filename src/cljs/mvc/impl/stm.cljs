(ns mvc.impl.stm)

(defprotocol IRef
  (-alter [_ f args])
  (-commute [_ f args])
  (-ensure [_])
  (-dosync [_ exprs])
  (-set-validator! [_ f])
  (-get-validator [_])
  (-set-min-history! [_])
  (-set-max-history! [_])
  (-validate! [_])
  (-watches [_]))

(deftype TVal [val point msecs prior next])

(deftype Ref [state meta validator revision-id watches tvals faults lock tinfo id
              min-history max-history ids]
  IDeref
  (-deref [_] state)
  
  IMeta
  (-meta [_] meta)

  IRef
  (-alter [_ f args])
  (-commute [_ f args])
  (-ensure [_])
  (-dosync [_ exprs])
  (-set-validator! [_ f])
  (-get-validator [_])
  (-validate! [_ new-val] (assert (validator new-val) "Validator rejected state."))
  (-watches [_] watches)
  (-set-min-history! [this mhist] (set! (.-min_history this) mhist))
  (-set-max-history! [this mhist] (set! (.-max_history this) mhist))

  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key))))

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
