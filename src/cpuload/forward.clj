(ns cpuload.forward
  (:require [cpuload.tsdata :as tsdata]
            [taoensso.timbre :as timbre])
  (:import com.cloudant.client.api.ClientBuilder)
  (:gen-class))
(timbre/refer-timbre)

(def -cloudant-url (or (System/getenv "CPULOAD_CLOUDANT_URL") ""))
(def -cloudant-username (or (System/getenv "CPULOAD_CLOUDANT_USERNAME") ""))
(def -cloudant-password (or (System/getenv "CPULOAD_CLOUDANT_PASSWORD") ""))

; TODO: collapse with other error handler
(defn -handle-ex [f & args] (try (apply f args)
                                 (catch Exception e (error (str "Error: " (.getMessage e))) (Thread/sleep 5000))))
(defn my-pub [data]
  (let [client (-> (ClientBuilder/url (new java.net.URL -cloudant-url)) (.username -cloudant-username) (.password -cloudant-password) (.build))]
    (let [db (.database client "cpuload" false)]
      (debug (format "db state: %s" (.info db)))
      (debug (format "data: %s" data))
      (info (format "Responses: %s" (.bulk db (java.util.ArrayList. data)))))))

(defn -main [& args]
  (info (format "Cloudant URL: %s" -cloudant-url))
  (info (format "Cloudant username: %s" -cloudant-username))
  (info (format "Cloudant password: %s" (if (empty? -cloudant-password) "<unset>" "<set but redacted>")))
  (while true (do (tsdata/enqueue-and-pub (partial -handle-ex my-pub)))))
