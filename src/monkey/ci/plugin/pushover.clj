(ns monkey.ci.plugin.pushover
  "Pushover plugin for MonkeyCI.  It allows you to create a job that sends a
   message to Pushover."
  (:require [monkey.ci.build
             [api :as api]
             [core :as bc]]
            [monkey.pushover.core :as p]))

(defn- make-msg [msg ctx]
  (if (fn? msg)
    (msg ctx)
    (str msg)))

(defn- get-creds [{:keys [user token user-param token-param]
                   :or {user-param "pushover-user"
                        token-param "pushover-token"}}
                  ctx]
  (if (and user token)
    {:user user
     :token token}
    (let [params (api/build-params ctx)]
      {:user (or user (get params user-param))
       :token (or token (get params token-param))})))

(def pushover-opts [:device :sound :title :url :url-title :html :priority :timestamp :ttl])

(defn pushover-msg [{:keys [id msg]
                     :or {id "pushover"}
                     :as config}]
  (bc/action-job
   id
   (fn [ctx]
     (let [client (p/make-client {})
           r @(p/post-message client (-> (get-creds config ctx)
                                         (assoc :message (make-msg msg ctx))
                                         (merge (select-keys config pushover-opts))))]
       (if (>= (:status r) 400)
         bc/failure
         bc/success)))
   (apply dissoc config :id :msg :user :token pushover-opts)))
