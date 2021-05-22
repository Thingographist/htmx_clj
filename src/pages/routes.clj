(ns pages.routes
  (:require [modules.htmx :as htmx]
            [pages.pages :refer [page action pages]]
            [pages.user page action]
            [pages.info page action]
            [pages.page42 page action]))

(defn page-route [p]
  (htmx/htmx-response (comp page #(assoc-in % [:page :page-key] p))))

(defn action-route [p]
  (comp action #(assoc-in % [:page :action-key] p)))

(def routes
  [["" (for [{:keys [href page-key]} pages]
         [(if (string? href)
            (subs href 1)
            (update href 0 subs 1))
          {:get  (page-route page-key)
           :post (action-route page-key)}])]])


(comment

  ;; MANAGEMENT API
  
  ;; restart
  
  (do
    (require 'application)
    (application/restart))

  ;; pages
  
  (user.page/add-page "")

  ;; migrations
  
  (user.db.migrations/create-migration "")

  (system.db/migrate)

  (user.db.migrations/rollback "")

  (user.db.migrations/reset "")

 ;;  
  )