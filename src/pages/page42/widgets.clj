(ns pages.page42.widgets
  (:require [modules.htmx :as htmx]))

(defn plotly [traces & [layout opts]]
  (let [id (gensym (str "plotly-" (rand-int 1000) "-"))
        traces-json (htmx/hx-vals traces)
        layout-json (htmx/hx-vals (or layout {}))
        opts-json (htmx/hx-vals
                   (or opts {:displayModeBar false
                             :scrollZoom     false
                             :dragMode       false}))]
    [:div
     [:div {:id      id
            :hx-load (str "Plotly.newPlot('" id "', " traces-json ", " layout-json ", " opts-json ");")}]]))