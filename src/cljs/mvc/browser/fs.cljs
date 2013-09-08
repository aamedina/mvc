(ns mvc.browser.fs
  (:require
   [goog.fs])
  (:require-macros
   [mvc.macros :as m :refer [import-goog with-fs]]))

(import-goog [goog.fs])

(def ^:dynamic *fs* nil)
