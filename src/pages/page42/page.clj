(ns pages.page42.page
  (:require [pages.pages :refer [page menu]]
            [pages.page42.state :refer [inject-state]]
            [modules.bootstrap :refer [plotly]]))

(defn- page42 [{ps :params}]
  (with-meta
    [:div
     (menu :page42 {:page42 {:id 42}})
     [:div.container.my-5
      [:div.row.my-5
       [:div.col
        [:div.card
         [:div.card-header (str "Страница " (:id ps))]
         [:div.card-body
          [:h1 "ответ на всякое"]]]]
       [:div.col
        [:div.card
         [:div.card-header "Пример графика"]
         [:div.card-body
          [:div.row.my-1
           [:div.col
            [:button#refresh.btn "refresh"]]]
          [:div.row
           [:div.col {:hx-post     ""
                      :hx-swap    "innerHTML"
                      :hx-trigger "click from:button#refresh"}
            (plotly [{:x [1, 2, 3, 4, 5]
                      :y (repeatedly 5 #(inc (rand-int 10)))}]
                    {:xaxis  {:fixedrange true}
                     :yaxis  {:fixedrange true}
                     :margin {:t 25
                              :b 25
                              :l 25
                              :r 25}})]]]]]]]]
    {:title (str "HTMX KIT PAGE42")
     :js    ["/js/plotly-latest.min.js"]}))

(defmethod page :page42 [req]
  (let [req+ (inject-state req)]
    (page42 req+)))