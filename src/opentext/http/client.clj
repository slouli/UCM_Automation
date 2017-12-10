(ns opentext.http.client
  (:require [org.httpkit.client :as http]
            [opentext.http.creds :as creds]
            [clojure.data.xml :as xml]))

(defn axl
  [{:keys [ip ver]} xmlReq]
  (http/post (str "https://" ip ":8443/axl/")
             {:basic-auth creds/creds
              :headers {"Content-Type" "text/xml"
                        "SOAPAction" (str "CUCM:DB ver=" ver)}
              :body (cond
                      (string? xmlReq) xmlReq
                      :else (xml/emit-str xmlReq))
              :insecure? true}
             (fn [{:keys [status headers body error]}]
               (str body))))

(defn ris
  [{:keys [ip ver]} xmlReq]
  (http/post (str "https://" ip ":8443/realtimeservice/services/RisPort70")
             {:basic-auth creds/creds
              :headers {"Content-Type" "text/xml"
                        "SOAPAction" "http://schemas.cisco.com/ast/soap/action/#RisPort70#SelectCmDevice"}
              :body (cond
                      (string? xmlReq) xmlReq
                      :else (xml/emit-str xmlReq))
              :insecure? true}
             (fn [{:keys [status headers body error]}]
               (str body))))