(ns htmx
  (:require [ring.util.response :as res]
            [cheshire.core :as json]
            [hiccup.page :refer [html5]]
            [hiccup.core :refer [html]]))

(defn htmx-trigger [response trigger]
  (->> (cond-> trigger
         (not (string? trigger))
         (json/generate-string))
       (res/header response "HX-Trigger")))

(defn hiccup-response [content & [opts]]
  (cond-> (res/response (html content))
    (:eval-after-load? opts)
    (htmx/htmx-trigger "hx-eval-after-load")))

(defn- page [content]
  (let [{:keys [title js css]} (meta content)]
    (res/response
     (html5
      {:lang "ru"}
      (into
       [:head
        [:meta {:charset "UTF-8"}]
        [:meta
         {:name "viewport"
          :content "width=device-width, initial-scale=1"}]
        [:meta {:http-equiv "X-UA-Compatible", :content "ie=edge"}]
        [:title#hx-page-title (or title "HTMX MAIN")]
        [:link {:rel "stylesheet", :href "/css/mdb.min.css"}]
        [:link {:rel "stylesheet", :href "/css/global.css"}]
        [:script {:src "/js/htmx.events.js"}]]
       (concat
        (for [path css]
          [:link {:rel "stylesheet", :href path}])
        (for [path js]
          [:script {:src path}])))
      [:body.bg-white
       [:main content]
       [:script {:src "/js/mdb.min.js"}]
       [:script {:src "/js/htmx.min.js"}]]))))

(defn- body [content]
  (let [{:keys [title js css]} (meta content)
        full-content (cond-> (html content)
                       (string? title)
                       (str (html [:title#hx-page-title {:hx-swap-oob "true"} title])))]
    (htmx-trigger
     (res/response full-content)
     {:hx-add-resources {:js  js
                         :css css}})))

(defn htmx-response [handler]
  (let [->page-response (comp page handler)
        ->body-response (comp body handler)]
    (fn [request]
      (if (some? (:htmx request))
        (->body-response request)
        (->page-response request)))))

(defn htmx-wrap [handler]
  (fn [request]
    (handler
     (cond-> request
       (= (get-in request [:headers "hx-request"]) "true")
       (assoc
        :htmx
        (let [{:strs [hx-trigger hx-trigger-name hx-current-url hx-target hx-prompt]} (:headers request)]
          {:trigger      hx-trigger
           :trigger-name hx-trigger-name
           :target       hx-target
           :prompt       hx-prompt
           :url          hx-current-url}))))))