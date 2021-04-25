(ns user.page
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn- ->page [page-name]
  (string/replace
   "(ns pages.%page-name%.page
    (:require [pages.pages :refer [page]]
              [pages.%page-name%.state :refer [inject-state]]))

    (defn- %page-name% [req] 
     [:div \"%page-name%\"])
    
   (defmethod page :%page-name% [req]
     (let [req+ (inject-state req)]
       (%page-name% req+)))"
   "%page-name%"
   page-name))

(defn- ->action [page-name]
  (string/replace
   "(ns pages.%page-name%.action
     (:require [modules.htmx :as htmx]
               [pages.pages :refer [action]]
               [pages.%page-name%.state :refer [inject-state]]))

    (defn- %page-name% [req] 
     [:div \"%page-name%\"])
 
   (defmethod action :%page-name% [req]
     (let [req+ (inject-state req)]
       (htmx/hiccup-response (%page-name% req+))))"
   "%page-name%"
   page-name))

(defn- ->state [page-name]
  (string/replace
   "(ns pages.%page-name%.state
     (:require [system.db :as db]))

   (defn inject-state [req]
     (let [new-state {}]
       (assoc req :state new-state)))"
   "%page-name%"
   page-name))

(defn add-page [page-name]
  (assert (some? (seq page-name)))
  (let [add-file (fn [file-name method]
                   (let [file (io/as-file (format "./src/pages/%s/%s.clj" (string/replace page-name "-" "_") file-name))]
                     (when (not (.isFile file))
                       (doto file
                         (io/make-parents)
                         (spit (method page-name))))))]
    (add-file "page" ->page)
    (add-file "action" ->action)
    (add-file "state" ->state)
    {:ns     `[~(symbol (str "pages." page-name)) ~'page ~'action]
     :page   [page-name `(~'page-route ~(keyword page-name))]
     :action [page-name `(~'action-route ~(keyword page-name))]}))
