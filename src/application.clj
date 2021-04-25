(ns application
  (:require [mount.core :refer [start stop]]
            system.db))

(defn -main [& _]
  (start))

(defn try-start []
  (try
    (-main)
    (catch Throwable e (Throwable->map e))))

(defn restart []
  (require '[clojure.tools.namespace.repl :refer [set-refresh-dirs refresh-all]])
  (let [set-refresh-dirs (ns-resolve 'clojure.tools.namespace.repl 'set-refresh-dirs)
        refresh-all (ns-resolve 'clojure.tools.namespace.repl 'refresh-all)]
    (stop)
    (set-refresh-dirs "src" "repl")
    (refresh-all :after 'application/try-start)))
