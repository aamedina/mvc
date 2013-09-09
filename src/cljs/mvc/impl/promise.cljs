(ns mvc.impl.promise)

(defprotocol IDeliver
  (-deliver [promise val])
  (-deliver-exception [promise exception]))

(defprotocol INotify
  (-attend [promise f executor]))

(defprotocol IFail
  (-realized-exception? [promise]))

(deftype Promise [])
