(ns sample-3.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [sample-3.ajax :as ajax]
    [sample-3.events]
    [reitit.core :as reitit]
    [clojure.string :as string]
    [ajax.core :refer [GET POST]])
  (:import goog.History))


(defonce answers* [{:x 5 :y 18 :op "+" :total 0}
                   {:x 9 :y 3 :op "+" :total 0}
                   {:x 2 :y 19 :op "+" :total 0}])




(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :active (when (= page @(rf/subscribe [:page])) "active")}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "sample-3"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-end
       [nav-link "#/" "Home" :home]
       [nav-link "#/about" "About" :about]]]]))


(defn- new-equation []
  (rf/dispatch [:add-new-equation]))

(defn- set-key* [idx k new-val]
  (rf/dispatch-sync [:set-key idx k new-val]))

(defn get-answer* [idx]
  (rf/dispatch [:compute-result idx]))



(defn input-field [tag id idx data]
  [:div.field
   [tag
    {:type        :number
     :value       (id data)
     :placeholder (name id)
     :on-change   #(do
                     ; in re-frame this is also kind of hacky - we really
                     ; should have the data change itself (set-key*) trigger
                     ; the computation and just re-render on the result
                     ; being stored in the app-db
                     (set-key* idx id (js/parseInt (-> % .-target .-value)))
                     (get-answer* idx))}]])

(defn colored-field [tag data]
  [tag {:class (cond
                 (< data 0) "negative-result"
                 (and (<= 0 data) (< data 20)) "small-result"
                 (and (<= 20 data) (< data 50)) "medium-result"
                 (<= 50 data) "large-result")}
   (str data)])

(defn- make-row [idx data]
  ^{:key idx}
  [:tr
   [:td (str idx)]
   [:td [input-field :input.input :x idx data]]
   [:td [:select {:style     {:font-size :xx-large}
                  :on-change #(do
                                ; in re-frame this is also kind of hacky - we really
                                ; should have the data change itself (set-key*) trigger
                                ; the computation and just re-render on the result
                                ; being stored in the app-db
                                (set-key* idx :op (-> % .-target .-value))
                                (get-answer* idx))}
         (map #(into ^{:key %} [:option %]) ["+" "-" "*" "/"])]]
   [:td [input-field :input.input :y idx data]]
   [:td "="]
   (colored-field :td.result (:total data))])

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn home-page []
  [:section.section>div.container>div.content
   [:div.button.is-medium.is-primary {:on-click #(new-equation)}
    [:i.material-icons.is-medium :fiber_new]]
   [:table
    [:tbody
     (doall
       (for [idx (range @(rf/subscribe [:num-answers]))]
         ;(prn idx)
         (let [data @(rf/subscribe [:each-answer idx])]
           (make-row idx data))))]]])

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (let [uri (or (not-empty (string/replace (.-token event) #"^.*#" "")) "/")]
          (rf/dispatch
            [:navigate (reitit/match-by-path router uri)]))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:navigate (reitit/match-by-name router :home)])
  
  (ajax/load-interceptors!)

  (rf/dispatch-sync [:init-db answers*])

  (prn "after :init-db" @(rf/subscribe [:num-answers]))

  ; this ia pretty hacky
  (doall
    (for [idx (range @(rf/subscribe [:num-answers]))]
      (doall
        (prn "kicking off" idx)
        (rf/dispatch-sync [:compute-result idx]))))

  (hook-browser-navigation!)
  (mount-components))
