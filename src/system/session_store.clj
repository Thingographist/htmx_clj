(ns system.session-store
  (:require [mount.core :refer [defstate]]
            [system.db :as db]
            [jdbc-ring-session.core :refer [jdbc-store]]))

(defn- start []
  (jdbc-store db/db))

(defstate session-store
  :start (start))