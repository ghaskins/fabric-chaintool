(defproject example02 "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.34"]
                 [org.clojure/tools.cli "0.3.3"]
                 [funcool/promesa "1.3.1"]]
  :plugins [[lein-nodecljs "0.5.0"]]
  :npm {:dependencies [[source-map-support "0.4.0"]
                       [protobufjs "5.0.1"]
                       [fabric-client "1.0.0-alpha"]
                       [fabric-ca-client "1.0.0-alpha.0"]]}
  :source-paths ["src" "target/classes"]
  :clean-targets ["out" "release"]
  :target-path "target"
  :nodecljs {:main example02.main})
