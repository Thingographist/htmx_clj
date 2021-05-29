(ns system.mongo
  (:require [mount.core :refer [defstate]]
            [monger.core :as mg]

            [system.env :refer [env]])
  (:import (com.mongodb MongoClientURI MongoClient)))

(defn- init-db! []
  (-> (env :MONGO_DB)
      (MongoClientURI. )
      (MongoClient.)))

(defstate mongo :start (init-db!))

(defn get-db [db-key]
  (mg/get-db mongo (name db-key)))

(defn list-dbs []
  (map keyword (mg/get-db-names mongo)))

(defn list-colls [db-key]
  (-> (mg/command (get-db db-key) {:listCollections ""})
      (get-in ["cursor" "firstBatch"])
      (->> (map #(keyword (get % "name"))))))
