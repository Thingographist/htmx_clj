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

(defn table+fields []
  (into {} (map (fn [[t {fs :fields}]] [t (map :column fs)])) db-info))

(defn table+refs []
  (let [->table-refs (fn [tab-refs-definition]
                       (->> (for [[cn col-refs] (group-by :column_name tab-refs-definition)]
                              [cn (map (juxt :referenced_table_name :referenced_column_name) col-refs)])
                            (into {})))]
    (->> (mapcat :references (vals db-info))
         (group-by :table_name)
         (into {} (map (fn [[t trd]] [t (->table-refs trd)]))))))

(defn mquery [q]
  (let [refs (mapcat :references (vals db-info))
        date->str #(-> % (str) (subs 0 10))
        opts {:references refs
              :query-fn   query
              :coercions  {:migration_vers {:applied date->str}
                           :tasks          {:created_at date->str}}}]
    (mquery/exec q opts)))

(comment

  (table+refs)

  (table+fields)

  (let [mq {:contexts {:$fields [:id :context]
                       :tasks   {:$fields [:task :created_at]
                                 :!first? true}}}]
    (mquery mq))

  (let [mq {:migration_vers {:$fields [:id :applied]}}]
    (mquery mq))


  ;;
  )