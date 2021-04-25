(ns pages.info.action
  (:require [modules.htmx :as htmx]
            [pages.pages :refer [action]]
            [pages.info.state :refer [inject-state]]))

(defn- info [req]
  [:div "info"])

(defmethod action :info [req]
  (let [req+ (inject-state req)]
    (htmx/hiccup-response (info req+))))