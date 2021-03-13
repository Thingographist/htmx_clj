(ns htmx
  (:require [ring.util.response :as res]
            [hiccup.page :refer [html5]]
            [hiccup.core :refer [html]]))


(defn hiccup-response [content]
  (res/response
   (html content)))

(defn- page [content]
  (let [{:keys [title]} (meta content)]
    (html5
     {:lang "ru"}
     [:head
      [:meta {:charset "UTF-8"}]
      [:meta
       {:name "viewport"
        :content "width=device-width, initial-scale=1"}]
      [:meta {:http-equiv "X-UA-Compatible", :content "ie=edge"}]
      [:title (or title "HTMX MAIN")]
      ; [:link {:rel "stylesheet", :href "/css/bootstrap.min.css"}]
      [:link {:rel "stylesheet", :href "/css/mdb.min.css"}]
      [:link {:rel "stylesheet", :href "/css/global.css"}]]
     [:body content]
    ;  [:script {:src "/js/bootstrap.min.js"}]
     [:script {:src "/js/mdb.min.js"}]
     [:script {:src "/js/htmx.min.js"}])))

(defn htmx-response [handler]
  (let [->response (comp res/response page)]
    (fn [request] 
      (if (:htmx? request)
        (handler request)
        (->response (handler request))))))

(defn htmx-wrap [handler]
  (fn [request]
    (let [{:strs [hx-request hx-trigger-name hx-current-url]} (:headers request)]
      (handler
       (cond-> request
         (= hx-request "true")
         (assoc :htmx {:trigger hx-trigger-name
                       :url     hx-current-url}))))))