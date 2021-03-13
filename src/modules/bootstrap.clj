(ns modules.bootstrap
  (:require [clojure.string :as string]))

(defn menu [opts & items]
  (let [cls (cond->> []
              (:center? opts)
              (cons "justify-content-center")

              :render
              (string/join " "))]
    [:nav.navbar.navbar-dark.bg-primary.navbar-expand
     [:div.container-fluid {:class cls}
      (into
       [:ul.navbar-nav.nav]
       (for [item items]
         [:li.nav-item item]))]]))