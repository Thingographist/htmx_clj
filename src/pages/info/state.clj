(ns pages.info.state
  (:require [system.db :as db]))

(defn inject-state [req]
  (let [new-state {}]
    (assoc req :state new-state)))