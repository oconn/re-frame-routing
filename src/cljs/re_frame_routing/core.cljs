(ns re-frame-routing.core
  (:require [cljs.spec.alpha :as s]
            [re-frame-routing.events :as evts]
            [re-frame-routing.subscriptions :as subs]))

(defn initial-router-state
  [{:keys [authenticated-links
           unauthenticated-links]
    :or {authenticated-links []
         unauthenticated-links []}}]
  {:authenticated-links authenticated-links
   :unauthenticated-links unauthenticated-links
   :route nil
   :route-params nil
   :initialized false})

(s/def ::href string?)
(s/def ::display string?)
(s/def ::key keyword?)
(s/def ::initialized boolean?)
(s/def ::links (s/coll-of
                (s/keys :req-un [::href
                                 ::display
                                 ::key])))
(s/def ::unauthenticated-links ::links)
(s/def ::authenticated-links ::links)
(s/def ::route (s/nilable keyword?))
(s/def ::route-params (s/nilable map?))
(s/def ::router (s/keys :req-un [::route
                                 ::route-params
                                 ::unauthenticated-links
                                 ::initialized]))


(defn register-events
  "Registers re-frame-request events and request handler"
  [opts]
  (evts/register-events opts))

(defn register-subscriptions
  "Registers re-frame-request subscriptions"
  []
  (subs/register-subscriptions))

(defn register-all
  "Registers both re-frame-request events & subscriptions"
  [{:keys [event-options]}]
  (register-events event-options)
  (register-subscriptions))
