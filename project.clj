(defproject cpuload-cloudant-forwarder "0.1.0-SNAPSHOT"
    :description "CPU Load timeseries data forwarder to IBM Cloudant instance"
    :url "https://github.com/open-horizon/example/..."
    :license {:name "Apache License Version 2.0, January 2004"
              :url "http://www.apache.org/licenses/LICENSE-2.0"
              :distribution :repo}
    :min-lein-version "2.0.0"
    :dependencies [[org.clojure/clojure "1.9.0"]
                   [org.clojure/data.json "0.2.6"]
                   [clj-http "3.7.0"]
                   [com.cloudant/cloudant-client "2.11.0"]
                   [com.taoensso/timbre "4.10.0"]]
    :plugins [[lein-cljfmt "0.5.7"]]
    :profiles {:dev {:source-paths ["startup"]
                     :dependencies [[org.clojure/tools.namespace "0.2.7"]]}
               :uberjar {:aot :all}}
    :main cpuload.forward)
