(ns sample-3.routes.services
  (:require
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [sample-3.middleware.formats :as formats]
    [sample-3.middleware.exception :as exception]
    [ring.util.http-response :refer :all]
    [clojure.java.io :as io]))

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]

   ["/ping"
    {:get (constantly (ok {:message "pong"}))}]
   

   ["/math"
    {:swagger {:tags ["math"]}}

    ["/plus"
     {:post {:summary "plus with spec body parameters"
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        (prn "plus" x y)
                        {:status 200
                         :body {:total (+ x y)}})}}]
    ["/minus"
     {:post {:summary "minus with spec body parameters"
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        (prn "minus" x y)
                        {:status 200
                         :body {:total (- x y)}})}}]
    ["/mult"
     {:post {:summary "multiply with spec body parameters"
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        (prn "mult" x y)
                        {:status 200
                         :body {:total (* x y)}})}}]
    ["/div"
     {:post {:summary "divide with spec body parameters"
             :parameters {:body {:x int?, :y int?}}
             :responses {200 {:body {:total int?}}}
             :handler (fn [{{{:keys [x y]} :body} :parameters}]
                        (prn "div" x y)
                        {:status 200
                         :body {:total (quot x y)}})}}]]])


