(ns user.db.migrations
  (:require [migratus.core :as migratus]

            [system.db :as db]))

(defn create-migration [name]
  (-> (db/migration-config)
      (migratus/create name)))

(defn rollback []
  (migratus/rollback
   (db/migration-config)))

(defn reset []
  (migratus/reset
   (db/migration-config)))

(comment

  (create-migration "")

  (db/migrate)

  (rollback)

  (db/query {:select [:*]
             :from   [:migration_vers]})

  (db/query {:select [:*]
             :from   [:streaming_projects]})
  
  (db/delete! :migration_vers [:= :id -1])
  
  ;; mysql introspections
  (clojure.java.jdbc/query db/db  "show tables")
  
  (let [table ""]
    (clojure.java.jdbc/query db/db  (str "show create table " table)))
  
  (reset)

  ;; 
  )