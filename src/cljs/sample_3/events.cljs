(ns sample-3.events
  (:require
    [re-frame.core :as rf]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [ajax.core :as ajax]))



(defonce empty-eq {:x 1 :y 1 :op "+" :total 2})






;;dispatchers

(rf/reg-event-db
  :init-db
  (fn-traced
    [db [_ data]]
    (assoc db :data data)))

(rf/reg-event-db
  :set-key
  (fn-traced
    [db [_ idx k val]]
    (prn ":set-key" idx k val)
    (assoc-in db [:data idx k] val)))

(rf/reg-event-db
  :navigate
  (fn-traced
    [db [_ route]]
    (assoc db :route route)))

(rf/reg-event-db
  :set-result
  (fn-traced
    [db [_ idx val]]
    (prn ":set-result" idx val)
    (assoc-in db [:data idx :total] (:total val))))

(rf/reg-event-fx
  :compute-result
  (fn-traced
    [cofx [_ idx]]
    (let [data (get (-> (:db cofx) :data) idx)
          op   (:op data)
          path (str "/api/math/"
                    (cond
                      (= op "+") "plus"
                      (= op "-") "minus"
                      (= op "*") "mult"
                      (= op "/") "div"))]
     (prn ":compute-result" data path)
     {:http-xhrio {:method          :post
                   :params          data
                   :uri             path
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:set-result idx]
                   :on-failure      [:common/set-error]}})))

(rf/reg-event-db
  :common/set-error
  (fn-traced
    [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-event-db
  :add-new-equation
  (fn-traced
    [db _]
    (assoc db :data (conj (-> db :data) empty-eq))))









;;subscriptions


(rf/reg-sub
  :fetch-test-message
  (fn [db _]
    "yet another small change that flows from a -> b"))


(rf/reg-sub
  :answers
  (fn [db _]
    (-> db :data)))

(rf/reg-sub
  :x
  (fn [db [_ idx]]
    (-> db :data idx :x)))

(rf/reg-sub
  :y
  (fn [db [_ idx]]
    (-> db :data idx :y)))

(rf/reg-sub
  :op
  (fn [db [_ idx]]
    (-> db :data idx :op)))

(rf/reg-sub
  :num-answers
  :<- [:answers]
  (fn [answers]
    (count answers)))

(rf/reg-sub
  :each-answer
  (fn [db [_ idx]]
    (get (-> db :data) idx)))

(rf/reg-sub
  :route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :name)))

(rf/reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))
