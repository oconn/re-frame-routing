(ns re-frame-routing.coercion
  (:require
   [spec-tools.core :as st]
   [cljs.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [clojure.walk :refer [postwalk keywordize-keys]]))

(defn enchanced-routes->bidi-routes*
  "Takes a route tree where the leafs/coercion-config-info are maps and makes it bidi compliment by transforming them to keywords."
  [routes]
  (postwalk (fn [node]
              (if-let [name (and (map? node) (:name node))]
                name
                node
                )) routes))

(def enchanced-routes->bidi-routes (memoize enchanced-routes->bidi-routes* ))

(defn params+corecion-info->coererced-params
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
  ;;TODO make this faster and lazier by only computing as needed and maybe only once by creating a handler->config lookup map
  (let [handler-info (atom nil)]
    (postwalk (fn [node]
                (if (= handler (:name node))
                  (do
                    (reset! handler-info node)
                    node)
                  node
                  )) routes)
    (let [path (params+corecion-info->coererced-params  (keywordize-keys route-params) (-> @handler-info :parameters :path))
          query (params+corecion-info->coererced-params (keywordize-keys route-query) (-> @handler-info :parameters :query))]
      (cond-> db
        handler (assoc-in [:router :route-parameters :key] handler)
        path (assoc-in [:router :route-parameters :path] path)
        query (assoc-in [:router :route-parameters :query] query)))))
