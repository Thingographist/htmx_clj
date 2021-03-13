(ns htmx
  (:require [ring.util.response :as res]
            [hiccup.page :refer [html5]]
            [hiccup.core :refer [html]]))


(defn hiccup-response [content]
  (res/response
   (html content)))

(defn- page [content]
  (let [{:keys [title js css]} (meta content)]
    (html5
     {:lang "ru"}
     [:head
      [:meta {:charset "UTF-8"}]
      [:meta
       {:name "viewport"
        :content "width=device-width, initial-scale=1"}]
      [:meta {:http-equiv "X-UA-Compatible", :content "ie=edge"}]
      [:title#hx-page-title (or title "HTMX MAIN")]
      [:link {:rel "stylesheet", :href "/css/mdb.min.css"}]
      [:link {:rel "stylesheet", :href "/css/global.css"}]]
     [:body content
      [:script {:src "/js/mdb.min.js"}]
      [:script {:src "/js/htmx.min.js"}]
      (into  
       [:div#hx-css-list]
       (for [path css]
         [:link {:rel "stylesheet", :href path}]))
      (into 
       [:div#hx-js-list]
       (for [path js]
         [:script {:src path}]))])))

(defn- body [content]
  (let [{:keys [title js css]} (meta content)]
    (cond-> (html content)
      (string? title)
      (str (html [:title#hx-page-title {:hx-swap-oob "true"} title]))

      (seq css)
      (str (html
            (into
             [:div#hx-css-list {:hx-swap-oob "true"}]
             (for [path css]
               [:link {:rel "stylesheet", :href path}]))))

      (seq js)
      (str (str (html
                 (into
                  [:div#hx-js-list {:hx-swap-oob "true"}]
                  (for [path js]
                    [:script {:src path}]))))))))

(defn htmx-response [handler]
  (let [->page-response (comp res/response page handler)
        ->body-response (comp res/response body handler)]
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