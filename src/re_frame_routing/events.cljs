(ns re-frame-routing.events
  (:require [bidi.bidi :as bidi]
            [cemerick.url :refer [url]]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]
            [re-frame-routing.coercion :as coercion]))

(defn listen-for-navigation!
  "Wraps the history object and begins listening for history events"
  [history]
  (re-frame/reg-fx
   :nav-to
   (fn [route]
     (pushy/set-token! history route))))

(defn get-route-query
  []
  (-> js/window
      .-location
      .-href
      url
      :query))

(defn set-route
  [db [_ {:keys [handler route-params] :as params} routes]]
  (-> db
      (assoc-in [:router :route] handler)
      (assoc-in [:router :route-params] route-params)
      (assoc-in [:router :route-query] (get-route-query))
      (coercion/coerce params routes)))

(defn nav-to
  [{:keys [db]} [_ route]]
  {:db db
   :nav-to route})

(defn initialized
  [db _]
  (assoc-in db [:router :initialized] true))

(defn- pushy-init
  ([routes]
  (fn [_]
     (let [history (pushy/pushy #(re-frame/dispatch [:router/set-route (assoc % :route-query (get-route-query)) routes])
                                #(bidi/match-route (coercion/enchanced-routes->bidi-routes routes) %))]

      (listen-for-navigation! history)

      (pushy/start! history)

      (re-frame/dispatch [:router/initialized]))))
  ([routes {:keys [routes-enriched]}]
   (fn [_]
     (let [history (pushy/pushy #(re-frame/dispatch [:router/set-route (assoc % :route-query (get-route-query)) routes-enriched])
                                #(bidi/match-route routes %))]

       (listen-for-navigation! history)

       (pushy/start! history)

       (re-frame/dispatch [:router/initialized])))))

(defn log
  [report-error-fn _ [_ error]]
  ((or report-error-fn js/console.log) error))

;; Public functions

(defn register-events
  [{:keys [set-route-interceptors
           nav-to-interceptors
           initialized-interceptors
           router-interceptors
           routes
           routes-enriched
           routes-error-report-fn]
    :or {set-route-interceptors []
         nav-to-interceptors []
         initialized-interceptors []
         router-interceptors []}}]

  (re-frame/reg-event-db
   :router/set-route
   (into router-interceptors set-route-interceptors)
   set-route)

  (re-frame/reg-event-fx
   :router/nav-to
   (into router-interceptors nav-to-interceptors)
   nav-to)

  (re-frame/reg-event-db
   :router/initialized
   (into router-interceptors initialized-interceptors)
   initialized)

  (re-frame/reg-fx
   :pushy-init
   (pushy-init routes {:routes-enriched routes-enriched}))

  (re-frame/reg-event-fx
   :router/coercion-error
   (into router-interceptors initialized-interceptors)
   (partial log routes-error-report-fn)))

