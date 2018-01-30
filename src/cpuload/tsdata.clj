(ns cpuload.tsdata
  (:require [taoensso.timbre :as timbre]
            [clj-http.client :as http]
            [clojure.data.json :as json]))
(timbre/refer-timbre)

(def ^:const -queue-len
  (let [env (System/getenv "CPULOAD_TS_QUEUE")]
    (if (not (empty? env))
      (.parse (java.text.NumberFormat/getInstance) env)
      50)))

(def -service-url (or (System/getenv "CPULOAD_SERVICE_URL") "http://cpuload:8347/"))

(defn -store-ts [queue datum] (if (not (nil? datum)) (conj queue datum) queue))

(defn -handle-http [f] (try (f)
                            (catch Exception e (error (str "Error: " (.getMessage e))) (Thread/sleep 5000))))

(defn -read-cpu [] (let [{:keys [status headers body] as :resp}
                         (http/get -service-url {:throw-exceptions true})]
                     (if (== status 200)
                       (let [m (json/read-str body :key-fn keyword) ts (.toString (java.time.Instant/now)) host (.getHostName (java.net.InetAddress/getLocalHost))]
                         (info (format "Use on all CPUs %s%s, ts: %s, hostname: %s" (:cpu m) "%" ts host))
                         { "percent" (:cpu m) "ts" ts "host" host})
                       (do
                         (info (format "Unexpected status code: %d. Headers: %s, Body: %s" status headers body))
                         {}))))

; use agent for the queue so we can get async publishing
(defn enqueue-and-pub [pub-fn]
  (info (format "Using CPU service URL: %s" -service-url))
  (loop [q (agent '()) ct -queue-len]
    (if (= ct 0)
      ; TODO: add error handling here if publishing fails for this queue
      (send q pub-fn)
      (do (send q -store-ts (-handle-http -read-cpu))
          (recur q (dec ct))))))
