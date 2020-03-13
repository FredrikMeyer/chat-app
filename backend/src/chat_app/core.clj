(ns chat-app.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [clojure.core.async :refer [chan close! go go-loop alt! timeout >! <! >!! pub sub unsub]]
            [com.walmartlabs.lacinia.pedestal :as lacinia]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.util :as util]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [config.core :refer [env]]
            [chat-app.id :as id]))

;; GraphQL

;; ticks
(defn ticks-streamer
  [_ args source-stream]
  (let [abort-ch (chan)]
    (go
      (loop [countdown (-> args :count dec)]
        (if (<= 0 countdown)
          (do
            (source-stream {:count countdown :time-ms (System/currentTimeMillis)})
            (alt!
              abort-ch nil

              (timeout 1000) (recur (dec countdown))))
          (source-stream nil))))
    ;; Cleanup:
    #(close! abort-ch)))

(def msg-chan (chan))
(def publication
  (pub msg-chan #(:topic %)))

(defn message-streamer2
  [context args source-stream]
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
      (unsub publication :new-msg subscriber)
      (close! subscriber))))

(def schema (-> "schema.edn"
                io/resource
                slurp
                edn/read-string))

(def messages (atom []))
(defn post-message! [message]
  (swap! messages (fn [old-msgs] (conj old-msgs message)))
  (>!! msg-chan {:topic :new-msg :msg message})
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
                              })
      (util/attach-streamers {:subscriptions/ticks ticks-streamer
                              :subscriptions/messages message-streamer2})
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
  (reset! server
          (-> service-map
              (merge {
                      ::http/join? false
                      ::http/host (:host env)
                      ::http/allowed-origins {:creds true :allowed-origins (constantly true)}
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
  (println "Starting server.")
  (start-dev))
