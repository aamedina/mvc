(ns mvc.browser.dom
  (:refer-clojure :exclude [remove get set])
  (:require
   [goog.dom] [goog.dom.classes])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.dom goog.dom.classes])
