(ns modules.htmx
  (:require [clojure.string :as string]

            [ring.util.response :as res]
            [cheshire.core :as json]
            [hiccup.page :refer [html5]]
            [hiccup.core :refer [html]]

            [system.env :refer [env]]))

(defn hs [& notations]
  (let [->str (fn [x] (cond-> x (keyword? x) (name)))]
    (->> (for [xs notations] (string/join " " (map ->str xs)))
         (string/join "\n"))))

(defn hx-vals [vals-map]
  (json/generate-string vals-map))

(defn hidden-vals [m]
  (into
   [:div.d-none]
   (for [[k v] m]
     [:input {:type  "hidden"
              :name  (cond-> k (keyword? k) (name))
              :value v}])))

(defn htmx-trigger [response trigger]
  (->> (cond-> trigger
         (not (string? trigger))
         (json/generate-string))
       (res/header response "HX-Trigger")))

(defn hiccup-response [content & [opts]]
  (cond-> (res/response (html content))
    (:eval-after-load? opts)
    (htmx-trigger "hx-eval-after-load")
    (:trigger opts)
    (htmx-trigger (:trigger opts))
    (:redirect opts)
    (res/header "HX-Redirect" (:redirect opts))
    (:session opts)
    (assoc :session (:session opts))))

(defn- static-with-version [path]
  (let [version (env :STATIC_VERSION)]
    (cond->  path
      (some? version)
      (str "?_v=" version))))

(defn- page [content]
  (let [{:keys [title js css session]} (meta content)]
    (cond->
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
         [:link {:rel "stylesheet", :href (static-with-version "/css/mdb.min.css")}]
         [:script {:src (static-with-version "/js/htmx.events.js")}]]
        (concat
         (for [path css]
           [:link {:rel  "stylesheet"
                   :href (static-with-version path)}])))
       (into
        [:body
         [:main content]
         [:script {:src (static-with-version "/js/mdb.min.js")}]
         [:script {:src (static-with-version "/js/htmx.min.js")}]
         [:script {:src (static-with-version "/js/hyperscript.min.js")}]
         [:script#scripts-bucket]]
        (for [path js]
          [:script {:src (static-with-version path)}]))))

      (some? session)
      (assoc :session session))))

(defn- body [content]
  (let [{:keys [title js css session]} (meta content)
        full-content (cond-> (html content)
                       (string? title)
                       (str (html [:title#hx-page-title {:hx-swap-oob "true"} title]))
                       (or (seq js) (seq css))
                       (str (html [:script#scripts-bucket
                                   {:hx-swap-oob "true"
                                    :scripts     (json/generate-string
                                                  {:js  (map static-with-version js)
                                                   :css (map static-with-version css)})}])))]
    (-> (res/response full-content)
        (assoc :session session))))

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