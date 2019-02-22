(ns re-frame-routing.core
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]
            [re-frame-routing.events :as evts]
            [re-frame-routing.subscriptions :as subs]))

(def initial-state
  {:route nil
   :route-params nil
   :route-query nil
   :initialized false})

(s/def ::href string?)
(s/def ::display string?)
(s/def ::key keyword?)
(s/def ::initialized boolean?)
(s/def ::route (s/nilable keyword?))
(s/def ::route-params (s/nilable map?))
(s/def ::route-query (s/nilable map?))
(s/def ::router (s/keys :req-un [::route
                                 ::route-params
                                 ::route-query
                                 ::initialized]))

(defn register-events
  "Registers re-frame-routing events"
  [opts]
  (evts/register-events opts))

(defn register-subscriptions
  "Registers re-frame-routing subscriptions"
  []
  (subs/register-subscriptions))

(defn register-all
  "Registers both re-frame-routing events & subscriptions"
  [{:keys [event-options]}]
  (register-events event-options)
  (register-subscriptions))

(defn create-route-middleware
  "Route middleware is an itterator that reduces over a sequence of
  middleware functions to ensure all required route logic has been
  met before mounting the containing view.

  Use cases: Protecting routes
             Redirecting routes
             Bootstraping data"
  [{:keys [loading-view]}]
  (fn [container middleware]
    (let [route-params (re-frame/subscribe [:router/route-params])
          route-query (re-frame/subscribe [:router/route-query])
          route-key (re-frame/subscribe [:router/route])
          ;; Middleware state can be used for anything, and is the recommended
          ;; method for sharing infromation between middleware functions
          ;; or track information on global state updates that will trigger
          ;; re-renders of the middleware chain.
          ;;
          ;; Realworld application of using middleware-state:
          ;; View relies on data from a server before rendering. Add request
          ;; information to middleware state to cause the view to display
          ;; a loading indicator before displaying the view. (Works well with
          ;; re-frame-request, TODO: example)
          middleware-state (atom {})]
      (fn []
        (let [{:keys [is-loading container] :as ctx}
              (reduce
               (fn [{:keys [is-loading] :as middleware-ctx}
                   middleware-fn]
                 (if is-loading
                   middleware-ctx
                   (middleware-fn middleware-ctx)))
               {:is-loading false
                :middleware-state @middleware-state
                :route-params @route-params
                :route-query @route-query
                :container container}
               middleware)]

          (reset! middleware-state (:middleware-state ctx))

          (if (true? is-loading)
            [loading-view]
            [container @route-params @route-query @route-key]))))))
