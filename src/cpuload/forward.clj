(ns cpuload.forward
  (:require [cpuload.tsdata :as tsdata]
            [taoensso.timbre :as timbre]
            [clj-gson.json :as gson]
            [clojure.data.json :as json])
  (:import javax.json.Json)
  (:import com.cloudant.client.api.ClientBuilder)
  (:gen-class))
(timbre/refer-timbre)

; TODO: add constants for each part of the ClientBuilder

; TODO: collapse with other error handler
(defn -handle-ex [f & args] (try (apply f args)
                                 (catch Exception e (error (str "Error: " (.getMessage e))) (Thread/sleep 5000))))
(defn my-pub [data]
  (let [client (-> (ClientBuilder/url (new java.net.URL "")) (.username "") (.password "") (.build))]
    (let [db (.database client "cpuload" false) docs (java.util.ArrayList.  (map #(-> (Json/createObjectBuilder)
                                                                                      (.add "ts" (:ts %))
                                                                                      (.add "percent" (:percent %))
                                                                                      (.add "host" (:host %))
                                                                                      (.build)) data))]
      (debug (format "db state: %s" (.info db)))
      (debug (format "docs: %s" docs))
      (info (format "Responses: %s" (.bulk db docs))))))

; use agent for the queue so we can get async publishing
(defn -main [& args]
  (while true (do (tsdata/enqueue-and-pub (partial -handle-ex my-pub)))))
