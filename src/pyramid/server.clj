(ns pyramid.server
  (:require [org.httpkit.server :as http]
            [polaris.core :as polaris]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response resource-response content-type]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [pyramid.util :as util]))


;; --- Didn't feel this warranted a separate namespace ---

(defn list-files
  [path]
  (remove 
   #(.isHidden %)
   (.listFiles (io/file (io/resource path)))))

(def count-files (comp count list-files))

(defn zoom-count
  [zoom]
  {:rows (count-files (util/zoom-path zoom))
   :cols (count-files (util/row-path zoom 0))})

(defn zoom-counts
  []
  (into 
   {}
   (map
    (juxt str zoom-count)
    (range 
     (count-files util/tile-root)))))



;; --- Server/handlers ---

(defn zoom-levels-handler
  [_]
  (-> (zoom-counts)
      json/write-str
      response
      (content-type "application/json")))

(defn tile-handler
  [{tile :params}]
  (-> tile
      util/tile-path
      resource-response
      (content-type "image/jpeg")))

(def routes
  [["/tile" :tile tile-handler]
   ["/zoom-levels" :zoom-levels zoom-levels-handler]])

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



