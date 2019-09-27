(ns sample-3.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[sample-3 started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[sample-3 has shut down successfully]=-"))
   :middleware identity})
