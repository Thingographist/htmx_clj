(ns user.oz)

(defn oz-temporal [field & [unit]]
  (when unit (assert (#{:week :day :year :month} unit)))
  (cond-> {:field field
           :type  :temporal}
    (some? unit)
    (assoc :timeUnit unit)))

(defn oz-quantitative [field & {:keys [agg scale]}]
  (when agg (assert (#{:sum :mean :max :min} agg)))
  (cond-> {:field field
           :type  :quantitative}
    (some? agg)
    (assoc :aggregate agg)
    (some? scale)
    (assoc :scale agg)))

(defn oz-nominal [field]
  {:field field
   :type  :nominal})

(comment

  (require '[oz.core :as oz])

  (oz/start-server!)
  (oz.server/stop!)
  (oz.server/get-server-port)
  
  ;;
  )