(ns mvc.browser.history
  (:require
   [goog.history.Event] [goog.history.Html5History])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.history.Event goog.history.Html5History])
