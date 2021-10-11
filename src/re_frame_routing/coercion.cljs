(ns re-frame-routing.coercion
  (:require
   [spec-tools.core :as st]
   [cljs.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [clojure.walk :refer [postwalk keywordize-keys]]))

(def handler->info (atom {}))

(defn params+corecion-info->coerced-params
  "Applies coercion information to raw parameters with some sensible default logic in case of failure."
  [raw-params corecion-info]
  (reduce-kv
   (fn [coerced-params coercion-param-key {:keys [coercion default] :or {coercion string?}}]
     (let [qp-value (coercion-param-key coerced-params )
           coerced-value (st/coerce coercion qp-value st/string-transformer)
           new-params (if (some? default)
                        (assoc coerced-params coercion-param-key default)
                        coerced-params)]
       (cond
         (nil? qp-value) new-params

         (not (s/valid? coercion coerced-value))
         (do
           (re-frame/dispatch [:router/coercion-error (s/explain-data coercion coerced-value)])
           new-params)

         :else
         (assoc coerced-params coercion-param-key coerced-value))))
   raw-params
   corecion-info))

(defn coerce
  [db {:keys [handler route-params route-query]} routes]
  ;;NOTE This does a one time walk to gather the necessary information for coercion and cache it for fast lookup. An alternative
  ;; approach would be to pass this lookup instead of the enriched routes, but that seems less future proof (e.g adding more enriched features)
  (when-not (seq @handler->info)
    (postwalk (fn [node]
                (if (and (map? node) (:name node))
                  (swap! handler->info assoc (:name node) node)
                  node
                  )) routes))
  (let [path (params+corecion-info->coerced-params  (keywordize-keys route-params) (-> @handler->info handler :parameters :path))
        query (params+corecion-info->coerced-params (keywordize-keys route-query) (-> @handler->info handler :parameters :query))]
      (cond-> db
        handler (assoc-in [:router :route-parameters :key] handler)
        path (assoc-in [:router :route-parameters :path] path)
      query (assoc-in [:router :route-parameters :query] query))))
