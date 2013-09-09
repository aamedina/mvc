(ns mvc.browser.history
  (:require
   [goog.History] [goog.history.Event] [goog.history.Html5History])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.History goog.Uri goog.history.Event goog.history.Html5History])
