(ns pages.pages
  (:require [clojure.string :as string]
            [pages.bootstrap :as bt]))

(def pages
  [{:href     "/"
    :page-key :user
    :title    "home"}
   {:href     "/info"
    :page-key :info
    :title    "info"}
   {:href     ["/page/" :id "/data"]
    :page-key :page42
    :title    "page 42"}])

(defn menu [curren-page-key & [params]]
  (let [->href (fn [{:keys [href page-key]}]
                 (cond-> href
                   (vector? href)
                   (->> (map #(get-in params [page-key %] %))
                        (string/join ""))))
        ->item (fn [{:keys [page-key title] :as p}]
                 [:a.nav-link
                  (cond-> {:hx-get      (->href p)
                           :hx-push-url "true"
                           :hx-target   "main"
                           :style       {:cursor "default"}}
                    (= curren-page-key page-key)
                    (assoc :class "active"))
                  title])]
    (->> (map ->item pages)
         (apply bt/menu {:center? true}))))

(defmulti page (comp :page-key :page))

(defmulti action (comp :action-key :page))
