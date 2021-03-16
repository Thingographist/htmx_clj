(ns system.db
  (:require [mount.core :refer [defstate]]
            [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [migratus.core :as migratus]

            [system.env :refer [env]]
            [modules.mquery :as mquery])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defn connection-pool
  "Create a c3p0 connection pool for the given database SPEC."
  [{:keys [connection-uri classname]}]
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass classname)
                 (.setJdbcUrl connection-uri)
                 (.setMaxIdleTimeExcessConnections (* 30))
                 (.setMaxIdleTime (* 3 60))
                 (.setInitialPoolSize 3)
                 (.setMinPoolSize 3)
                 (.setMaxPoolSize 15)
                 (.setIdleConnectionTestPeriod 0)
                 (.setTestConnectionOnCheckin false)
                 (.setTestConnectionOnCheckout false)
                 (.setPreferredTestQuery nil))})

(defn init-db! []
  (-> {:classname "com.mysql.cj.jdbc.Driver" :connection-uri (env :MYSQL)}
      (connection-pool)))

(defstate db :start (init-db!))

(defn query [honey-sql]
  (jdbc/query db (sql/format honey-sql)))

(defn- ->jdbc-where [honey-where]
  (let [[q & args] (sql/format {:where honey-where})]
    (apply vector (subs q 6) args)))

(defn update! [table set-map where]
  (jdbc/update! db table set-map (->jdbc-where where)))

(defn insert-multi! [table rows]
  (jdbc/insert-multi! db table rows))

(defn insert! [table obj]
  (first (insert-multi! table [obj])))

(defn delete! [table where]
  (jdbc/delete! db table (->jdbc-where where)))

(defn migration-config []
  {:store                :database
   :migration-dir        "migrations/"
   :init-in-transaction? false
   :migration-table-name "migration_vers"
   :db                   db})

(defn migrate []
  (migratus/migrate (migration-config)))

(defstate db-info :start (mquery/db-info db))

(defn mquery [q]
  (let [refs (mapcat :references db-info)
        opts {:references refs
              :query-fn   query
              :coercions  {:migration_vers {:applied #(-> % (str) (subs 0 10))}}}]
    (mquery/exec q opts)))

(comment

  (let [mq {:migration_vers {:$fields [:id :applied]}}]
    (mquery mq))


  ;;
  )