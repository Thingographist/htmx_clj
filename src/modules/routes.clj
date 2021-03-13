(ns modules.routes
  (:require [htmx]
            [modules.bootstrap :as bt]))

(declare pages)

(defn- menu [page]
  (let [->item (fn [{:keys [href page-key title]}]
                 [:a.nav-link
                  (cond-> {:href href}
                    (= page page-key)
                    (assoc :class "active"))
                  title])]
    (->> (map ->item pages)
         (apply bt/menu {:center? true}))))

(defmulti page (comp :page-key :page))

(defmethod page :home [_]
  (with-meta
    [:div
     (menu :home)
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
     (menu :info)
     [:div.container.my-5
      [:div.row.my-5
       [:div.col
        [:div.card
         [:div.card-header "Краткая информация"]
         [:div.card-body
          [:h1 "infonify"]]]]]]]
    {:title "HTMX KIT INFO"}))

(def pages
  [{:href     "/"
    :page-key :home
    :title    "home"}
   {:href     "/info"
    :page-key :info
    :title    "info"}])

(def routes
  [["" (for [{:keys [href] :as p} pages]
         [(subs href 1)
          (htmx/htmx-response 
           (fn [r]  (page (assoc r :page p))))])]
   ["api/user" (fn [r] (htmx/hiccup-response [:b (-> r :params :username)]))]
   ["ws/user" (fn [r] (htmx/hiccup-response [:b (-> r :params :username)]))]])
