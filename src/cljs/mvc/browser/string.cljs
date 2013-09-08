(ns mvc.browser.string
  (:refer-clojure :exclude [remove repeat subs])
  (:require
   [goog.string])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.string])
