(ns mvc.core.observable)

(defprotocol IObservable
  (-subscribe [_]))

(defprotocol IObserver
  (-handler [_])
  (-set-handler! [_])
  (-error-handler [_])
  (-set-error-handler! [_])
  (-next-handler [_])
  (-set-next-handler! [_]))

(defprotocol IDisposable
  (-dispose [_]))


