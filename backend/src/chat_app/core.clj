(ns chat-app.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [clojure.core.async :refer [chan close! go-loop <! >!! pub sub unsub]]
            [com.walmartlabs.lacinia.pedestal :as lacinia]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [config.core :refer [env]]
            [taoensso.carmine :as car]
            [clojure.tools.logging :as log]
            [chat-app.id :as id]))


(def msg-chan (chan))

;; Redis

(defn redis-server []
  {:pool {}
   :spec {:uri (str  "redis://" (:redis-host env) ":" (:redis-port env) "/")}})

(defmacro wcar* [& body] `(car/wcar (redis-server) ~@body))

(defn set-key [value]
  (wcar* (car/set "somekey" value)))

(defn get-key []
  (wcar* (car/get "somekey")))

(defn listen-to-messages []
  (car/with-new-pubsub-listener (:spec (redis-server))
    {
     "chat-msg" (fn f1 [msg]
                  (let [[_ _ message-object] msg]
                    (log/info "received msg: " message-object)
                    (>!! msg-chan {:topic :new-msg :msg message-object})))
     }
    (car/subscribe "chat-msg")
    ))

;; GraphQL

(def publication
  (pub msg-chan #(:topic %)))

(defn message-streamer
  [_ _ source-stream]
  (let [subscriber (chan)]
    (sub publication :new-msg subscriber)
    (go-loop []
      (let [obj (<! subscriber)]
        (if (some? obj) ;; Had to do this to avoid infinite loop
          (do
            (source-stream (:msg obj))
            (recur))
          (source-stream nil))))
    (fn []
      (log/info "Client disconnected.")
      (unsub publication :new-msg subscriber)
      (close! subscriber))))

(def schema (-> "schema.edn"
                io/resource
                slurp
                edn/read-string))

(def messages (atom []))

(defn post-message! [message]
  (swap! messages (fn [old-msgs] (conj old-msgs message)))
  (wcar* (car/publish "chat-msg" message))
  message)

(def service-map
  (-> schema
      (util/attach-resolvers {:query/hello (constantly "Hello you!")
                              :tick/time-ms (fn [_ _ tick]
                                              (-> tick :time-ms str))
                              :mutation/post-message! (fn [_ arguments _]
                                                        (let [msg (:message arguments)
                                                              frm (:from arguments)]
                                                          (post-message! {
                                                                          :message msg
                                                                          :from frm
                                                                          :id (id/generate-id!)
                                                                          })))
                              :mutation/set-redis-var! (fn [_ arguments _]
                                                         (let [val (:value arguments)]
                                                           (set-key val)))
                              :query/get-key (fn [_ _ _] (get-key))
                              })
      (util/attach-streamers {:subscriptions/messages message-streamer})
      schema/compile
      (lacinia/service-map {:graphiql true
                            :path "/graphql"
                            :ide-path "/ui"
                            :subscriptions true
                            :subscriptions-path "/ws"})))

(defn create-server []
  (http/create-server service-map))

(defonce server (atom nil))

(defn start-dev []
  (listen-to-messages)
  (reset! server
          (-> service-map
              (merge {
                      ::http/join? false
                      ::http/host (:host env)
                      ::http/port (:port env)
                      ::http/allowed-origins {
                                              :creds true
                                              :allowed-origins (constantly true)
                                              }
                      })
              http/default-interceptors
              http/create-server
              http/start)))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))

(defn start []
  (http/start (create-server)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/info "Starting server. Port: " (:port env) ". Host: " (:host env))
  (start-dev))
