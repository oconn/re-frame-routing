(ns re-frame-routing.core
  (:require [cljs.spec.alpha :as s]
            [re-frame-routing.events :as evts]
            [re-frame-routing.subscriptions :as subs]))

(def initial-state
  {:route nil
   :route-params nil
   :initialized false})

(s/def ::href string?)
(s/def ::display string?)
(s/def ::key keyword?)
(s/def ::initialized boolean?)
(s/def ::route (s/nilable keyword?))
(s/def ::route-params (s/nilable map?))
(s/def ::router (s/keys :req-un [::route
                                 ::route-params
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
