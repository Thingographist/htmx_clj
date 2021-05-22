(ns modules.bootstrap
  (:require [clojure.string :as string]
            [cheshire.core :as json]
            [modules.hyperscript :as hs]))

(defn fmt-num [v]
  (when (number? v)
    (-> (format "%,.2f" (double v))
        (string/replace #",?0+$" ""))))

(defn time->date-str [t]
  (subs (str t) 0 10))

(defn input [opts]
  (let [datalist-id (str (:name opts) "-datalist")
        datalist (when-let [options (:options opts)]
                   (into
                    [:datalist {:id datalist-id}]
                    (for [opt options]
                      [:option opt])))
        input-opts (cond-> (select-keys opts [:name :value :type :class :list])
                     (some? datalist)
                     (assoc :list datalist-id))]
    [:div.form-outline
     [:input.form-control input-opts]
     datalist
     (let [label-opts (:label opts)]
       (cond 
         (not (string? label-opts))
         label-opts

         :string
         [:label.form-label label-opts]))]))

(defn select [{:keys [name options]}]
  (into
   [:select.form-select {:name name}]
   (for [{:keys [value text]} options]
     [:option {:value value} text])))

(defn plotly [traces & [layout opts]]
  (let [traces-json (json/generate-string traces)
        layout-json (json/generate-string (or layout {}))
        opts-json (json/generate-string
                   (or opts {:displayModeBar false
                             :scrollZoom     false
                             :dragMode       false}))
        tpl "Plotly.newPlot($currentNode, %s, %s, %s);"]
    [:div {:hx-load (format tpl traces-json layout-json  opts-json)}]))

(defn button-with-preloaded [title btn-opts & hs-success]
  (let [hs (hs/hs [:on "htmx:beforeRequest"
                   [:tell ".spinner-border" :in :me :remove ".d-none" :end]]
                  [:on "htmx:afterRequest(xhr)"
                   [:tell ".spinner-border" :in :me :add ".d-none" :end]
                   [:if "xhr.status == 400" [:call "hsAlert(xhr.response)"] :end]
                   [:if "xhr.status == 500" [:call "hsAlert('<div>Вот незадача! Что-то с сервером.</div>')"] :end]
                   (if (seq hs-success) [:if "xhr.status == 200" hs-success :end] "")])]
    [:button.btn (assoc btn-opts :_ hs)
     title
     [:span.spinner-border.spinner-border-sm.text-success.ms-2.d-none]]))
