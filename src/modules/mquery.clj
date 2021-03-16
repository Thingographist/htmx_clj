(ns modules.mquery
  (:require [clojure.set :as set]
            [clojure.java.jdbc :as jdbc])
  (:import (java.sql DatabaseMetaData)))

(defn- get-references [md tbl]
  (for [{:keys [table_name]}                                            (jdbc/metadata-result (.getTables md nil nil tbl (into-array ["TABLE"])))
        {:keys [fktable_name fkcolumn_name pkcolumn_name pktable_name]} (jdbc/metadata-result (.getImportedKeys md nil nil table_name))]
    {:table_name             (keyword fktable_name)
     :column_name            (keyword fkcolumn_name)
     :referenced_table_name  (keyword pktable_name)
     :referenced_column_name (keyword pkcolumn_name)}))

(defn db-info [db]
  (jdbc/with-db-metadata [md db]
    (let [tables (->> (.getTables md nil nil nil (into-array ["TABLE"]))
                      (jdbc/metadata-result)
                      (map :table_name))]
      (->> (for [t    tables
                 :let [primary-keys (->> (jdbc/metadata-result (.getPrimaryKeys ^DatabaseMetaData md nil nil t))
                                         (into #{} (map :column_name)))
                       fields (->> (.getColumns md nil nil t nil)
                                   (jdbc/metadata-result)
                                   (map (fn [{:keys [column_name type_name]}]
                                          {:column   (keyword column_name)
                                           :primary? (contains? primary-keys column_name)
                                           :type     type_name})))]]
             [(keyword t)
              {:fields     (vec fields)
               :references (vec (get-references md t))}])
           (into {})))))

(defn build-honey-sql [table query references]
  (let [v-aliases (volatile! {})
        add-sub-table (fn [table sub-table]
                        (vswap! v-aliases update-in [(keyword table) :sub-tables] conj (keyword sub-table)))
        field-with-alias (fn [table-alias field-key]
                           (let [field-alias (keyword (str table-alias "__" (name field-key)))]
                             (vswap! v-aliases assoc-in [(keyword table-alias) :fields field-alias] field-key)
                             [(keyword (str table-alias "." (name field-key))) field-alias]))
        make-alias (fn make-alias
                     ([t] (make-alias t nil))
                     ([t a]
                      (let [alias (or (some-> a (name)) (gensym (name t)))]
                        (vswap! v-aliases assoc-in [(keyword alias) :table] t)
                        alias)))
        simple-query (fn [t fs]
                       (let [alias (make-alias t)]
                         {:select (vec (map #(field-with-alias alias %) fs))
                          :from   [[t (keyword alias)]]}))
        remove-sys-keys #(dissoc %
                                 :$alias :$where :$fields :$left-join?
                                 :!first? :!group-by :!limit)
        ->join-expr (fn [table alias sub-table sub-alias]
                      (->> (for [{:keys [table_name column_name referenced_table_name referenced_column_name]} references
                                 :when (and
                                        (= table_name sub-table)
                                        (= referenced_table_name table))]
                             [:=
                              (first (field-with-alias sub-alias column_name))
                              (first (field-with-alias alias referenced_column_name))])
                           (into [:and])))
        assert-join (fn [table sub-table join-expr]
                      (when (= [:and] join-expr)
                        (throw
                         (ex-info
                          "not found references for join"
                          {:main-table table
                           :sub-table  sub-table}))))
        compile-subquery (fn compile-subquery [table alias q]
                           (for [[t sq] (remove-sys-keys q)]
                             (if (map? sq)
                               ;; complex
                               (let [{:keys [$alias $fields $where $left-join?]} sq
                                     sub-alias (make-alias t $alias)
                                     join-key (if $left-join? :left-join :join)
                                     join-expr (->join-expr table alias t sub-alias)]
                                 (add-sub-table alias sub-alias)
                                 (assert-join table t join-expr)
                                 (apply
                                  merge-with
                                  (comp vec concat)
                                  {:select  (map #(field-with-alias sub-alias %) $fields)
                                   join-key (concat [[t (keyword sub-alias)]] [(into join-expr $where)])}
                                  (compile-subquery t sub-alias sq)))
                               (let [{:keys [select from]} (simple-query t sq)
                                     [sub-table sub-alias] (first from)
                                     join-expr (->join-expr table alias sub-table (name sub-alias))]
                                 (add-sub-table alias sub-alias)
                                 (assert-join table sub-table join-expr)
                                 {:select select
                                  :join   (concat from [join-expr])}))))
        complex-query (fn [t {:keys [$alias $fields $where] :as q}]
                        (let [alias (make-alias t $alias)
                              query (merge
                                     {:select (vec (map #(field-with-alias alias %) $fields))
                                      :from   [[t (keyword alias)]]}
                                     (when $where {:where $where}))]
                          (apply merge-with (comp vec concat) query (compile-subquery t alias q))))
        query (doall
               (if (map? query)
                 (complex-query table query)
                 (simple-query table query)))]
    {:aliases @v-aliases
     :root    (:from query)
     :query   query}))

(defn exec [mquery {:keys [references query-fn coercions]}]
  (assert (fn? query-fn) "(:query-fn opts) must be fn")
  (let [deep-coerce (partial merge-with (fn [field coerce-fn] (coerce-fn field)))
        decode (fn decode [{:keys [aliases result] :as res} alias-key]
                 (let [{:keys [fields table sub-tables]} (get aliases alias-key)
                       table-coercions (get coercions table)]
                   {table (for [items (vals (group-by (apply juxt (keys fields)) result))
                                :let [obj (-> (select-keys (first items) (keys fields))
                                              (set/rename-keys fields)
                                              (cond-> (map? table-coercions) (deep-coerce table-coercions)))]]
                            (into obj (map #(decode res %)) sub-tables))}))]
    (->> (for [[t q] mquery
               :let [{:keys [root query] :as res} (build-honey-sql t q references)
                     root-k (second (first root))]]
           (-> (assoc res :result (query-fn query))
               (decode root-k)))
         (into {}))))


(comment
  
  {:table1 [:col1 :col2]
   :table2 {:$alias  :t
            :$where  [:= :id 1]
            :$fields [:col1 :col2]
            :subtable {:$fields   [:col1 :col2 :!count.col3]
                       :!first?   true
                       :!group-by [:id]
                       :!order-by [:col1]
                       :!limit    10
                       :!offset   10}}}

  ;;
  )
