(ns opentext.psa.itl
  (:require [org.httpkit.client :as http]
            [clojure.data.xml :as xml]
            [opentext.http.creds :as creds]))

(defn gen-xml [operand]
  (str "XML=" (xml/emit-str
                (xml/element :CiscoIPPhoneExecute {}
                             (xml/element :ExecuteItem {:Priority "0" :URL operand})))))

(defn delete
  [model]
  (let [keyseq {:cisco79XX (list "Init:Settings" "Key:Settings" "Key:KeyPad4" "Key:KeyPad5" "Key:KeyPad2"
                                 "Key:KeyPadStar" "Key:KeyPadStar" "Key:KeyPadPound" "Key:Soft4" "Key:Soft2")
                :cisco88XX (list "Init:Applications" "Key:Applications" "Key:KeyPad4")}]
    (map gen-xml (model keyseq))))


(def options
  (fn [operation]
    {:timeout 5000
     :basic-auth creds/creds
     :headers    {"Content-Type" "application/x-www-form-urlencoded"
                  "Content-Length" (count operation)}
     :body       operation}))

(defn phoneRequest [ip operations]
  (cond (empty? operations) (println ip ": COMPLETE")
        :else (http/post (str "http://" ip "/CGI/Execute") (options (first operations))
                         (fn [{:keys [status headers body error]}]
                           (if (= status 200)
                             (phoneRequest ip (rest operations))
                             (println ip ": FAILED :" status ":" body))))))

(def send-cmd
  (fn [ip] (phoneRequest ip (delete :cisco88XX))))

(println (map send-cmd '("10.3.34.102" "192.168.0.19")))

(Thread/sleep 60000)