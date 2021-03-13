(ns modules.routes
  (:require [clojure.string :as string]
            [htmx]
            [modules.bootstrap :as bt]))

(declare pages)

(defn- menu [curren-page-key & [params]]
  (let [->href (fn [{:keys [href page-key]}]
                 (cond-> href
                   (vector? href)
                   (->> (map #(get-in params [page-key %] %))
                        (string/join ""))))
        ->item (fn [{:keys [page-key title] :as p}]
                 [:a.nav-link
                  (cond-> {:hx-get      (->href p)
                           :hx-push-url "true"
                           :hx-target   "body"
                           :style       {:cursor "default"}}
                    (= curren-page-key page-key)
                    (assoc :class "active"))
                  title])]
    (->> (map ->item pages)
         (apply bt/menu {:center? true}))))

(defmulti page (comp :page-key :page))

(defmethod page :home [_]
  (with-meta
    [:div
     (menu :home {:page42 {:id 42}})
     [:div.container.my-5
      [:div.row.bg-light
       [:div.col
        [:div.card
         [:div.card-header "Интерактивный ввод"]
         [:div.card-body
          [:h1 "hi "
           [:input.inline-input
            {:placeholder "username"
             :name        "username"
             :hx-post     "/api/user"
             :hx-swap     "outerHTML"
             :hx-trigger  "click from:button#send"}]
           "!"
           [:button#send.btn.mx-5 "update"]]]]]]]]
    {:title "HTMX KIT HOME"}))

(defmethod page :info [_]
  (with-meta
    [:div
     (menu :info {:page42 {:id 42}})
     [:div.container.my-5
      [:div.row.my-5
       [:div.col
        [:div.card
         [:div.card-header "Краткая информация"]
         [:div.card-body
          [:h1 "infonify"]]]]]]]
    {:title "HTMX KIT INFO"}))

(defmethod page :page42 [{{id :id} :params}]
  (with-meta
    [:div
     (menu :page42 {:page42 {:id 42}})
     [:div.container.my-5
      [:div.row.my-5
       [:div.col
        [:div.card
         [:div.card-header (str "Страница " id)]
         [:div.card-body
          [:h1 "ответ на всякое"]]]]]]]
    {:title (str "HTMX KIT PAGE42")}))

(def pages
  [{:href     "/"
    :page-key :home
    :title    "home"}
   {:href     "/info"
    :page-key :info
    :title    "info"}
   {:href     ["/page/" :id "/data"]
    :page-key :page42
    :title    "page 42"}])

(def routes
  [["" (for [{:keys [href] :as p} pages]
         [(if (string? href)
            (subs href 1)
            (update href 0 subs 1))
          (htmx/htmx-response 
           (fn [r]  (page (assoc r :page p))))])]
   ["api/user" (fn [r] (htmx/hiccup-response [:b (-> r :params :username)]))]
   ["ws/user" (fn [r] (htmx/hiccup-response [:b (-> r :params :username)]))]])
