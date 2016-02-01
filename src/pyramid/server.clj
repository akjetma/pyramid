(ns pyramid.server
  (:require [org.httpkit.server :as http]
            [polaris.core :as polaris]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.util.response :refer [response file-response content-type]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.java.browse :as browse]
            [pyramid.util :as util]
            [pyramid.tile :as tile]))

(defonce image-root
  (atom nil))

(defn prefix
  [path]
  (util/make-path @image-root path))

;; --- Didn't feel this warranted a separate namespace ---

(defn list-files
  [path]
  (remove 
   #(.isHidden %)
   (.listFiles (io/file path))))

(defn count-children
  [path]
  (count (list-files path)))

(defn zoom-count
  [zoom]
  {:rows (count-children (prefix (util/zoom-path zoom)))
   :cols (count-children (prefix (util/row-path zoom 0)))})

(defn zoom-counts
  []
  (into 
   {}
   (map
    (juxt str zoom-count)
    (range 
     (count-children (prefix util/tile-prefix))))))



;; --- Server/handlers ---

(defn app-handler
  [_]
  (-> "public/pyramid.html"
      io/resource
      slurp
      response
      (content-type "text/html")))

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
      prefix
      file-response
      (content-type "image/jpeg")))

(def routes
  [["/" :app app-handler]
   ["/tile" :tile tile-handler]
   ["/zoom-levels" :zoom-levels zoom-levels-handler]])

(def router
  (-> routes
      polaris/build-routes
      polaris/router
      (wrap-resource "public")
      wrap-file-info
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
  ([] (start-server 5000))
  ([port]
   (stop-server)
   (let [server* (http/run-server #'router {:port port})]
     (println "server started on port" port)
     (reset! server server*))))

(defn -main
  ([image-path] (-main image-path tile/default-tile-width))
  ([image-path tile-width]
   (let [port (Integer/parseInt (get (System/getenv) "PORT" "5000"))
         width (Integer/parseInt (str tile-width))]
     (reset! image-root (util/dir image-path))
     (tile/make-tiles! image-path @image-root width)
     (start-server port)
     (browse/browse-url "http://localhost:5000"))))



