(ns mvc.browser.structs
  (:refer-clojure :exclude [map filter some])
  (:require
   [goog.structs] [goog.structs.Trie] [goog.structs.PriorityQueue]
   [goog.structs.Node] [goog.structs.TreeNode] [goog.structs.Map]
   [goog.structs.Pool] [goog.structs.PriorityPool])
  (:require-macros
   [mvc.macros :as m :refer [import-goog]]))

(import-goog [goog.structs.PriorityPool goog.structs.Pool 
              goog.structs.PriorityQueue goog.structs.Node 
              goog.structs.TreeNode goog.structs.Trie goog.structs.Map
              goog.structs])
