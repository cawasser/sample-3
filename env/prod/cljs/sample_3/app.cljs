(ns sample-3.app
  (:require [sample-3.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
