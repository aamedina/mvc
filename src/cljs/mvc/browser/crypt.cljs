(ns mvc.browser.crypt
  (:require
   [goog.crypt] [goog.crypt.JpegEncoder])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.crypt])

(def JpegEncoder goog.crypt.JpegEncoder)
