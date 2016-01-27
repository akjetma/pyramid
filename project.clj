(defproject pyramid "0.1.0-SNAPSHOT"
  ;; :jvm-opts ["-Xmx3G"]
  :min-lein-version "2.5.3"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [fivetonine/collage "0.2.1"]
                 [http-kit "2.1.18"]
                 [polaris "0.0.15"]
                 [ring "1.4.0"]
                 [ring-cors "0.1.7"]
                 [org.clojure/data.json "0.2.6"]]
  :main pyramid.server)
