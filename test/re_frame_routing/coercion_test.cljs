(ns re-frame-routing.coercion-test
  (:require [cljs.test :refer-macros [deftest testing are]]
            [re-frame-routing.coercion :as c]
            [clojure.spec.alpha :as s]))

(deftest coercion-behavior
  (testing "and documenting the behavior of the coercion enhancement"
    (are [raw-params coercion-info coerced-params] (= (c/params+corecion-info->coerced-params raw-params coercion-info) coerced-params)
      nil nil nil
      {} {} {}
      {:a "1"} {} {:a "1"}
      {} {:a {:coercion int?}} {}
      {:a "1"} {:a {:coercion int?}} {:a 1}
      {:a "1" :b "2"} {:a {:coercion int?} :b {:coercion int?} } {:a 1 :b 2}
      {:a "a"} {:a {:coercion int?}} {:a "a"}
      {:a "a"} {:a {:coercion int? :default 1}} {:a 1}
      {:a "a"} {:a {:coercion (s/and keyword? #{:a})}} {:a :a}
      {:a "a"} {:a {:coercion (s/and keyword? #{:b})}} {:a "a"})))

