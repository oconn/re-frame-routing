(set-env! :resource-paths #{"src/cljs"}
          :source-paths   #{"test/cljs"}
          :dependencies   '[[bidi                      "2.1.2"]
                            [kibu/pushy                "0.3.8"]
                            [org.clojure/clojurescript "1.9.946" :scope "provided"]
                            [re-frame                  "0.10.2"  :scope "provided"]

                            [adzerk/bootlaces  "0.1.13" :scope "test"]
                            [adzerk/boot-test  "1.2.0"  :scope "test"]])

(require '[adzerk.boot-test :refer [test]]
         '[adzerk.bootlaces :refer :all]
         '[boot.git         :refer [last-commit]])

(def project 'oconn/re-frame-routing)
(def +version+ "0.1.0-SNAPSHOT")

(task-options!
 pom {:project     project
      :version     +version+
      :description "ClojureScript (re-frame) library that manages routing and route state."
      :url         "https://github.com/oconn/re-frame-routing"
      :scm         {:url "https://github.com/oconn/re-frame-routing"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 push {:repo "deploy-clojars"
       :ensure-clean true
       :ensure-tag (last-commit)
       :ensure-version +version+})

(bootlaces! +version+ :dont-modify-paths? true)

(deftask install-local
  "Build and install the project locally."
  []
  (comp (pom)
        (jar)
        (install)))

(deftask dev
  "Watches for changes and then installs locally"
  []
  (comp (watch)
        (install-local)))

(deftask deploy-snapshot
  "Deploys a new build to clojars"
  []
  (comp (build-jar)
        (push-snapshot)))

(deftask deploy-release
  "Deploys a release build to clojars"
  []
  (comp (build-jar)
        (push-release)))
