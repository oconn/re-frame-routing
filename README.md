# re-frame-routing

ClojureScript (re-frame) library that manages routing and route state.

**re-frame-routing** is built on top of [bidi](https://github.com/juxt/bidi) and persists route state to [re-frame's](https://github.com/Day8/re-frame) app state. Registering *re-frame-routing* to an existing re-frame application is ease.


### Step 1: Install

[![Clojars Project](https://img.shields.io/clojars/v/oconn/re-frame-routing.svg)](https://clojars.org/oconn/re-frame-routing)

[![CircleCI](https://circleci.com/gh/oconn/re-frame-routing/tree/master.svg?style=shield)](https://circleci.com/gh/oconn/re-frame-routing/tree/master)

### Step 2: Import Events

```cljs
(ns app.events.core
  (:require [re-frame-routing.core :as rfr]

            [app.router.core :as router]))

(rfr/register-events {:routes router/routes})
```

### Step 3: Import Subscriptions

```cljs
(ns app.subscriptions.core
  (:require [re-frame-routing.core :as rfr]))

(rfr/register-subscriptions)
```

## Route Middleware

*(Note this API is subject to change in future releases)*

### Pre Dom Render Loading 

Sometimes it's nice to perform work when a route is loading, maybe even prevent the route's view from displaying until that work is done, or even redirect if a user does not have access to view the route. These are good use cases for route-middleware.

To add route-middleware to a route, first create the route-middleware function

```cljs
(def route-middleware
  (rfr/create-route-middleware {:loading-view loading/render}))
```

`loading-view` is a reagent component that will display while your route is loading.

Now let's say you have a sign-in page, but only unauthenticated users should be able to view it. Here is you sign-in route with no middleware

```cljs
(defmethod containers :sign-in []
  [sign-in/render])
```

And now we add some middleware

```cljs
(defmethod containers :sign-in []
  [(route-middleware
    sign-in/render
    [redirect-authenticated])])
```

Without diving into `redirect-authenticated` quite yet, let's first look at our `route-middleware` function. It takes in a view component and a vector of middleware functions (executed from left to right). The result after all functions have executed is the view. The view will receive path and query params as arguments.

Next let's look at `redirect-authenticated`

```cljs
(defn redirect-authenticated
  "Redirects authenticated users to the authenticated home page"
  [{:keys [route-query route-params] :as ctx}]
  (let [is-authenticated (re-frame/subscribe [:user/is-authenticated?])]

    (when (true? @is-authenticated)
      (re-frame/dispatch [:router/nav-to "/authenticated-home"]))

    (if (true? @is-authenticated)
      (assoc ctx :is-loading true)
      ctx)))
```

Middleware functions will be passed a context object and are expected to return a context object. To prevent the execution of the next middleware function in the chain, toggle the `is-loading` property on the middleware context object to `true`.


### Route Coercion

Given data sent to the servers and to the component originates from information synced in the url (path and query params), it's ideal to have a way to share concerns over configuration (coercion and defaults).
The library supports that concern by allowing you to supply an `routes-enirched` tree where leafs can be supplied a configuration instead of a keyword. e.g

```
{:query {:site {:coercion int?}
         :role {:coercion int?}
         :learner {:coercion #{"least-ready" "most-ready" "highest-mindset" "lowest-mindset"}
                   :default "least-ready"}
         :current-page {:coercion int?
                :default 0}}}
```

this will then provide a route-parameters map to the component. e.g

```
{:route-parameters {:role 1 :learner "least-ready" :site 2 :current-page 0}}
```

This functionality is then handled by [spec-tools](https://github.com/metosin/spec-tools). Overall this is a less feature rich version of what's provided by [reitit](https://github.com/metosin/reitit/blob/master/doc/coercion/clojure_spec_coercion.md). 


## Development

Run ` clj -M:shadow-cljs watch app` to and get a watched build. See Shadow cljs docs for more.

## License

Copyright © 2018 Matt O'Connell

Distributed under the Eclipse Public License.
