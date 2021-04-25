(ns pages.user.action
  (:require [modules.htmx :as htmx]
            [pages.pages :refer [action]]
            [pages.user.state :refer [inject-state]]))

(defn- user [req]
  [:b (-> req :params :username)])

(defmethod action :user [req]
  (let [req+ (inject-state req)]
    (htmx/hiccup-response (user req+))))