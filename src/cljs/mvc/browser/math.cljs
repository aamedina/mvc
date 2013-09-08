(ns mvc.browser.math
  (:require
   [goog.math])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.math goog.math.Matrix goog.math.ExponentialBackoff])
