(ns re-frame-routing.events
  (:require [bidi.bidi :as bidi]
            [cemerick.url :refer [url]]
            [pushy.core :as pushy]
            [re-frame.core :as re-frame]))

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
  [db [_ {:keys [handler route-params]}]]
  (-> db
      (assoc-in [:router :route] handler)
      (assoc-in [:router :route-params] route-params)
      (assoc-in [:router :route-query] (get-route-query))))

(defn nav-to
  [{:keys [db]} [_ route]]
  {:db db
   :nav-to route})

(defn initialized
  [db _]
  (assoc-in db [:router :initialized] true))

(defn- pushy-init
  [routes]
  (fn [_]
    (let [history (pushy/pushy #(re-frame/dispatch [:router/set-route %])
                               #(bidi/match-route routes %))]

      (listen-for-navigation! history)

      (pushy/start! history)

      (re-frame/dispatch [:router/initialized]))))

;; Public functions

(defn register-events
  [{:keys [set-route-interceptors
           nav-to-interceptors
           initialized-interceptors
           router-interceptors
           routes]
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
   (pushy-init routes)))
