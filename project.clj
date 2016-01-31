(defproject pyramid "0.1.0-SNAPSHOT"
  :min-lein-version "2.5.3"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [fivetonine/collage "0.2.1"]
                 [http-kit "2.1.18"]
                 [polaris "0.0.15"]
                 [ring "1.4.0"]
                 [ring-cors "0.1.7"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/clojurescript "1.7.145"]
                 [binaryage/devtools "0.5.2"]
                 [reagent "0.5.1"]
                 [cljs-ajax "0.5.2"]]
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.5.0-1"]]
  :source-paths ["src"]
  :clean-targets ^{:protect false} ["resources/public/js/pyramid.js"
                                    "resources/public/js/out"
                                    "target"]
  :cljsbuild {:builds 
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "pyramid.client/load"}
                :compiler {:main pyramid.client
                           :asset-path "js/out"
                           :output-to "resources/public/js/pyramid.js"
                           :output-dir "resources/public/js/out"
                           :source-map-timestamp true}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:main pyramid.client
                           :output-to "resources/public/js/pyramid.js"
                           :optimizations :advanced
                           :pretty-print false}}]}
  :figwheel {:css-dirs ["resources/public/css"]}
  :main pyramid.server)
