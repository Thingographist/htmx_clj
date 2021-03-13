(ns system.http
  (:require [mount.core :refer [defstate]]
            [clj-http.client :as http]
            [clj-http.conn-mgr :as conn]
            [cheshire.core :as json]))


(defn start []
  (conn/make-reusable-conn-manager
   {:timeout           (* 3 60 60)
    :threads           100
    :default-per-route 5}))

(defstate cm
  :start (start)
  :stop nil)

(defn request [query]
  (http/request
   (assoc query :connection-manager cm)))

(defn repeat-request [repeat-pred query]
  (let [call #(try
                {:result (request query)}
                (catch Exception e
                  {:error e}))]
    (loop [tries 10]
      (let [{:keys [error result]} (call)]
        (cond
          (not error)
          result

          (and (pos? tries)
               (repeat-pred error))
          (recur (dec tries))

          :else
          (throw error))))))

(defn pages-iter 
  ([extract]
   (pages-iter nil extract))
  ([opts extract]
   (let [client (if-let [repeat-pred (:repeat-pred opts)]
                  #(repeat-request repeat-pred %)
                  http/request)]
     (fn step [request]
       (let [[page next-request] (extract request (client request))]
         (lazy-seq (cons page (some-> next-request (step)))))))))

(defn extract-json-body [extract]
  (fn [request response]
    (let [data (json/parse-string (:body response) keyword)]
      (extract request data))))
