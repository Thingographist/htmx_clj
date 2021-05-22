(ns modules.hyperscript
  (:require [clojure.string :as string]
            [cheshire.core :as json]))

(defn hs [& notations]
  (let [->str (fn [x] 
                (cond-> x (keyword? x) (name)))]
    (->> (for [xs notations] (string/join " " (map ->str (flatten xs))))
         (string/join "\n"))))

(defn hs-json [data]
  (-> (json/generate-string data)
      (string/replace "\"" "\\\"")
      (string/replace "'" "\\'")))

(defn post [url data & actions]
  [:async :do
   :call (format "hsPost('%s','%s')" url (hs-json data))
   :set :POSTRESULT :to :it
   actions
   :return :POSTRESULT
   :end])
