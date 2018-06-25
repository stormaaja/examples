(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clj-wiite.core :as w]
            [guestbook.config :refer [env]]))

(prn env)

(defn create-watom []
  (let [state (w/watom "postgres://wiiteuser:passu@127.0.0.1:5432/wiitetest")]
    (when (nil? @state)
      (reset! state []))
    state))

(defonce messages (create-watom))

(defn home-page [{:keys [flash]}]
  (layout/render
    "home.html"
    (merge {:messages @messages}
           (select-keys flash [:name :message :errors]))))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> (response/found "/")
        (assoc :flash (assoc params :errors errors)))
    (do
      (swap! messages conj
        (assoc params :timestamp (.toString (java.util.Date.))))
      (response/found "/"))))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" request (home-page request))
  (POST "/" request (save-message! request))
  (GET "/about" [] (about-page)))
