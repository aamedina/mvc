(ns mvc.browser.structs
  (:refer-clojure :exclude [map filter some])
  (:require
   [goog.structs])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.structs goog.structs.Pool goog.structs.PriorityPool
              goog.structs.PriorityQueue goog.structs.Node
              goog.structs.TreeNode goog.structs.Trie])
