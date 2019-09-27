(ns sample-3.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [sample-3.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[sample-3 started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[sample-3 has shut down successfully]=-"))
   :middleware wrap-dev})
