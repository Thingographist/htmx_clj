(ns pages.user.page
  (:require [modules.hyperscript :as hs]
            [pages.pages :refer [page menu]]
            [pages.user.state :refer [inject-state]]))

(defn- user [req]
  (with-meta
    [:div
     (menu :home {:page42 {:id 42}})
     [:div.container.my-5
      [:div.row.bg-light
       [:div.col
        [:div.card
         [:div.card-header "Интерактивный ввод"]
         [:div.card-body
          [:h1 {:_ (hs/hs [:on "htmx:afterSwap" [:call "hsAlert('Оу. А ошибки то нет.')"]])} "hi "
           [:input.inline-input
            {:placeholder "username"
             :name        "username"
             :hx-post     "/api/user"
             :hx-swap     "outerHTML"
             :hx-trigger  "click from:button#send"}]
           "!"
           [:button#send.btn.mx-5 {:_ (hs/hs [:on :click :hide :me])} "update"]]]]]]]]
    {:title "HTMX KIT HOME"}))

(defmethod page :user [req]
  (let [req+ (inject-state req)]
    (user req+)))