(ns pages.page42.action
  (:require [modules.htmx :as htmx]
            [pages.pages :refer [action]]
            [pages.page42.state :refer [inject-state]]
            [modules.bootstrap :refer [plotly]]))

(defn- page42 [req]
  (plotly [{:x [1, 2, 3, 4, 5]
            :y (repeatedly 5 #(inc (rand-int 10)))}]
          {:xaxis  {:fixedrange true}
           :yaxis  {:fixedrange true}
           :margin {:t 25
                    :b 25
                    :l 25
                    :r 25}}))

(defmethod action :page42 [req]
  (let [req+ (inject-state req)]
    (htmx/hiccup-response (page42 req+))))