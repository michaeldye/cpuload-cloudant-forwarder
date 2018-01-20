(ns cpuload.forward
  (:require [cpuload.tsdata :as tsdata]
            [taoensso.timbre :as timbre]
            [clojure.data.json :as json])
  (:import com.cloudant.client.api.CloudantClient)
  (:gen-class))
(timbre/refer-timbre)

(defn my-pub [data] (info (format "Publishing read data: %s" data)))

; use agent for the queue so we can get async publishing
(defn -main [& args]
  (while true (tsdata/enqueue-and-pub my-pub)))
