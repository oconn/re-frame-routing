(ns re-frame-routing.core-test
  (:require [clojure.spec.alpha :as s]
            [cljs.test :refer-macros [deftest is testing]]

            [re-frame-routing.core :as rfr]))

;; Will log errors if no subscriptions are registered
(rfr/register-subscriptions)

(deftest create-route-middleware
  (let [middleware-fn (rfr/create-route-middleware
                       {:loading-view [:p "loading..."]})

        ctx-no-middleware
        ((middleware-fn [:div "my-view"]
                        []))

        ctx-always-loading
        ((middleware-fn [:div "my-view"]
                        [(fn [ctx] (assoc ctx :is-loading true))]))]

    (testing "Will return the view if context is not in a loading state"
      (is (= "my-view" (get-in ctx-no-middleware [0 1]))))

    (testing "Will return the loading view if context is in a loading state"
      (is (= "loading..." (get-in ctx-always-loading [0 1]))))))
