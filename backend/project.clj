(defproject chat-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 ;; Logging (http://brownsofa.org/blog/2015/06/14/clojure-in-production-logging/)
                 [org.clojure/tools.logging "1.0.0"]
                 [ch.qos.logback/logback-core "1.2.3"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 ;; https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api
                 ;; GraphQL
                 [com.walmartlabs/lacinia-pedestal "0.13.0"]
                 [com.walmartlabs/lacinia "0.36.0"]
                 [io.pedestal/pedestal.service "0.5.7"]
                 [io.pedestal/pedestal.route "0.5.7"]
                 [io.pedestal/pedestal.jetty "0.5.7"]
                 ;; Redis client
                 [com.taoensso/carmine "2.19.1"]
                 ;; Config 
                 [yogthos/config "1.1.7"]
                 ]
  :main ^:skip-aot chat-app.core
  :target-path "target/%s"
  ;; :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
  :profiles {:prod {:resource-paths ["config/prod"]}
             :dev {:resource-paths ["config/dev"]}
             :uberjar {:aot :all}
             }
  )
