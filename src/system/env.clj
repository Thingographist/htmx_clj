(ns system.env
  (:require [mount.core :refer [defstate]]
            dotenv))

(defn- base-env []
  (into {} [(System/getenv)
            (System/getProperties)
            (#'dotenv/load-env-file ".env")]))

(defn- app-env-specific-env []
  (into {} (map #'dotenv/load-env-file dotenv/app-env-specific-filenames)))

(defn- start []
  (into {} [(base-env) (app-env-specific-env)]))

(defstate extended-env
  :start (start))

(defn env
  ([] extended-env)
  ([k] (get extended-env (name k))))