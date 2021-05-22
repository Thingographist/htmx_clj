(ns application
  (:gen-class)
  (:require [mount.core :refer [start stop]]
            [system.env]
            [system.db :as db]
            [system.server]))

(defn try-start []
  (try
    (start)
    (catch Throwable e (Throwable->map e))))

(defn restart []
  (require '[clojure.tools.namespace.repl :refer [set-refresh-dirs refresh-all]])
  (let [set-refresh-dirs (ns-resolve 'clojure.tools.namespace.repl 'set-refresh-dirs)
        refresh-all (ns-resolve 'clojure.tools.namespace.repl 'refresh-all)
        clear (ns-resolve 'clojure.tools.namespace.repl 'clear)]
    (clear)
    (stop)
    (set-refresh-dirs "src" "repl")
    (refresh-all :after 'application/try-start)))

(defn -main [& _]
  (start #'system.env/extended-env #'system.db/db)
  (db/migrate)
  (start))