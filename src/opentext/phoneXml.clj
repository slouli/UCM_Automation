(ns opentext.phoneXml
  (:require [org.httpkit.client :as http])
  (:require [clojure.data.xml :as xml])
  (:require [opentext.http.creds :as creds]))


(def keySeq
  (list "Init:Settings" "Key:Settings" "Key:KeyPad4" "Key:KeyPad5" "Key:KeyPad2"
        "Key:KeyPadStar" "Key:KeyPadStar" "Key:KeyPadPound" "Key:Soft4" "Key:Soft2" nil))

(defn keyCmd [oper]
  (str "XML=" (xml/emit-str
    (xml/element :CiscoIPPhoneExecute {}
                 (xml/element :ExecuteItem {:Priority "0" :URL oper})))))

;(println (xml/emit-str (keyCmd (second keySeq))))

(defn phoneRequest [ip oper]
  (http/post (str "http://" ip "/CGI/Execute")
             {:basic-auth creds/creds
              :headers {"Content-Type" "application/x-www-form-urlencoded"}
              :body oper}
             (fn [{:keys [status headers body error]}]
               (str body))))

(def xmlList (map keyCmd keySeq))

(def cmd1 (first xmlList))
(def cmd2 (first (rest xmlList)))
(def cmd3 (first (rest (rest xmlList))))
(def cmd4 (first (rest (rest (rest xmlList)))))
(def cmd5 (first (rest (rest (rest (rest xmlList))))))
(def cmd6 (first (rest (rest (rest (rest (rest xmlList)))))))
(def cmd7 (first (rest (rest (rest (rest (rest (rest xmlList))))))))
(def cmd8 (first (rest (rest (rest (rest (rest (rest (rest xmlList)))))))))
(def cmd9 (first (rest (rest (rest (rest (rest (rest (rest (rest xmlList))))))))))
(def cmd10 (first (rest (rest (rest (rest (rest (rest (rest (rest (rest xmlList)))))))))))

(defn submitRequests
  [ip]
  (let [resp (phoneRequest ip cmd1)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd2)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd3)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd4)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd5)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd6)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd7)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd8)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd9)]
    (println @resp)
    (Thread/sleep 150))
  (let [resp (phoneRequest ip cmd10)]
    (println @resp)
    (Thread/sleep 150)))


;(defn submitRequests
;  [ip xml]
;  (cond
;    (nil? (second xmlList)) (println "Complete")
;    :else (let [resp (phoneRequest ip (first xmlList))]
;            (do
;              (println @resp)
;              (Thread/sleep 100)
;              (submitRequests ip (rest xmlList))))))



;(println xmlList)

;(submitRequests xmlList)
