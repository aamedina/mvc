(ns mvc.browser.db
  (:require
   [goog.db])
  (:require-macros
   [mvc.macros :as m :refer [import-goog with-db]]))

(import-goog [goog.db goog.db.Cursor goog.db.Error goog.db.Index goog.db.IndexedDb
              goog.db.KeyRange goog.db.ObjectStore goog.db.Transaction])

(def ^:dynamic *db* nil)

(def ^:dynamic *version* nil)

(defn open!
  [uid on-upgrade-needed on-blocked]
  (let [db (open-database uid *version* on-upgrade-needed on-blocked)]
    db))

(defn close!
  [])

(defn transaction
  [])
