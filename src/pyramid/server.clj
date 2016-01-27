(ns pyramid.server
  (:require [org.httpkit.server :as http]
            [polaris.core :as polaris]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [resource-response content-type]]
            [pyramid.util :as util]))

(defn tile-handler
  [{tile :params}]
  (-> tile
      util/tile-path
      resource-response
      (content-type "image/jpeg")))

(def routes
  [["/tile" :tile tile-handler]])

(def router
  (-> routes
      polaris/build-routes
      polaris/router
      wrap-keyword-params
      wrap-params))

(defonce server (atom nil))

(defn stop-server
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (println "server stopped")
    (reset! server nil)))

(defn start-server
  []
  (stop-server)
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))
        server* (http/run-server #'router {:port port})]
    (println "server started on port" port)
    (reset! server server*)))

(defn -main
  []
  (start-server))



