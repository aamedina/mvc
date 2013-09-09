(ns mvc.impl.future)

(defprotocol IFuture
  (cancelled? [_])
  (completed? [_])
  (cancel! [_]))

(defprotocol IListenableFuture
  (add-listener! [_]))

(deftype Future []
  IFuture
  (cancelled? [_])
  (completed? [_])
  (cancel! [_])
  
  IListenableFuture
  (add-listener! [_]))
