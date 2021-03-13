(ns application
  (:require [mount.core :refer [start stop]]
            system.db))

(defn -main [& _]
  (start))

(defn try-start []
  (try
    (-main)
    (catch Throwable e (Throwable->map e))))

(comment

  (do
    (require '[clojure.tools.namespace.repl :as tn])
    (stop)
    (tn/set-refresh-dirs "src" "repl")
    (tn/refresh-all :after 'application/try-start))
  
  ;;
  )
