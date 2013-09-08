(ns mvc.browser.core
  (:refer-clojure :exclude [set remove some get filter map])
  (:require
   [goog.userAgent] [goog.format] [goog.spell.SpellCheck])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.object goog.json goog.userAgent goog.format
              goog.format.EmailAddress goog.spell.SpellCheck])


(defn immutable
  [o]
  (create-immutable-view o))

(defn immutable?
  [o]
  (is-immutable-view o))

(defn inherits
  [child-class parent-class]
  (goog/inherits child-class parent-class))


