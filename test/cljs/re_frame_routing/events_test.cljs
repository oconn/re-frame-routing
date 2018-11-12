(ns re-frame-routing.events-test
  (:require [clojure.spec.alpha :as s]
            [cljs.test :refer-macros [deftest is testing]]
            [cljs.pprint :refer [pprint]]

            [re-frame-routing.events :as rfr-event]))

(def initial-db
  {:router {:route :home
            :route-params {:user-uuid "123"}
            :route-query {}
            :initialized false}})

;; Need to mock js/window
#_(deftest set-route
  (let [updated-db
        (rfr-event/set-route initial-db
                             [nil
                              {:handler :new-route
                               :route-params {:user-uuid "456"}}])]
    ))

(deftest nav-to
  (testing "Triggers nav-to fx"
    (is (= "/route" (:nav-to (rfr-event/nav-to
                              {:db initial-db} [nil "/route"]))))))

(deftest initialized
  (testing "Toggles initialized to true"
    (is (true? (get-in (rfr-event/initialized initial-db nil)
                       [:router :initialized])))))
