(ns mvc.browser.result
  (:require
   [goog.result.Result] [goog.result.DependentResult])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.result.result_interface goog.result.DependentResult])
