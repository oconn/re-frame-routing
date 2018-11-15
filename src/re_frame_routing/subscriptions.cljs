(ns re-frame-routing.subscriptions
  (:require [re-frame.core :as re-frame]))

(defn register-subscriptions
  "Register re-frame-routing subscriptions"
  []

  (re-frame/reg-sub
   :router/core
   (fn [{:keys [router]} _] router))

  (re-frame/reg-sub
   :router/route
   #(re-frame/subscribe [:router/core])
   (fn [{:keys [route]} _] route))

  (re-frame/reg-sub
   :router/route-params
   #(re-frame/subscribe [:router/core])
   (fn [{:keys [route-params]} _] route-params)))

(re-frame/reg-sub
   :router/route-query
   #(re-frame/subscribe [:router/core])
   (fn [{:keys [route-query]} _] route-query))
