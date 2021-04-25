(ns pages.info.page
  (:require [pages.pages :refer [page menu]]
            [pages.info.state :refer [inject-state]]))

(defn- info [req]
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

(defmethod page :info [req]
  (let [req+ (inject-state req)]
    (info req+)))