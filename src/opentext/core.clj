(ns opentext.core
  (:require [opentext.ucmXml :as ucmXml]
            [opentext.http.client :as client]
            [clojure.data.zip.xml :as zip-xml]
            [opentext.udplist :as udplist]
            [opentext.xml.phone :as phone]
            [clojure.data.xml :as xml]
            [opentext.xml.line :as line])
  (:gen-class))

;Note, version is SOAPAction header, not necessarily UCM version
(def WLOO {:ip "10.2.92.50" :ver 8.5})
(def AMER {:ip "10.230.154.5" :ver 10.0})

(def axlResponse
  (let [resp (client/axl WLOO (ucmXml/getPhones "OT-TOMP-DP"))]
    @resp))

(def phones
  (let [zipper (ucmXml/zipper axlResponse)]
    {:names (zip-xml/xml-> zipper :Body :listPhoneResponse :return
                                  :phone :name zip-xml/text)
     :models (zip-xml/xml-> zipper :Body :listPhoneResponse :return
                                   :phone :model zip-xml/text)}))

;((fn [{:keys [names models]}]
;   (println names)
;   (println models)
;   (def model_id (into #{} models))
;   (println model_id)
;   (println (filter (fn [name] (= (second name) "Cisco 7945")) (map list names models)))) phones)


(def phones2
  (map (fn [name model] {:name name :model model}) (get phones :names) (get phones :models)))

(defn sortPhones
  [phones]
  {:Cisco7945 (filter #(= (:model %) "Cisco 7945") phones)
   :Cisco7965 (filter #(= (:model %) "Cisco 7965") phones)
   :Cisco7911 (filter #(= (:model %) "Cisco 7911") phones)
   :Cisco7937 (filter #(= (:model %) "Cisco 7937") phones)
   :CIPC (filter #(= (:model %) "Cisco IP Communicator") phones)})

;(println (map :name (:Cisco7945 (sortPhones phones2))))
(def Cisco7945s
  (->> (sortPhones phones2) (:Cisco7945) (map :name)))
(def Cisco7965s
  (->> (sortPhones phones2) (:Cisco7965) (map :name)))
(def Cisco7911s
  (->> (sortPhones phones2) (:Cisco7911) (map :name)))
(def Cisco7937s
  (->> (sortPhones phones2) (:Cisco7937) (map :name)))

(println (ucmXml/risPort85 (get phones :names)))

(def httpresp2
  (let [resp (client/ris WLOO (ucmXml/risPort85 Cisco7945s))]
    @resp))

(def registeredPhones
  (let [zipper (ucmXml/zipper httpresp2)]
    {:names (zip-xml/xml-> zipper :Body :SelectCmDeviceResponse :SelectCmDeviceResult :CmNodes
                   :item :CmDevices :item :Name zip-xml/text)
     :users (zip-xml/xml-> zipper :Body :SelectCmDeviceResponse :SelectCmDeviceResult :CmNodes
                   :item :CmDevices :item :LoginUserId zip-xml/text)
     :ips (zip-xml/xml-> zipper :Body :SelectCmDeviceResponse :SelectCmDeviceResult :CmNodes
                           :item :CmDevices :item :IPAddress :item :IP zip-xml/text)}))

(println registeredPhones)

;CREATE LIST OF LOGGED OUT DNs TO ADD======
(def reqList (let [params (map list (map str (repeat "#") (range 37484 (+ 37484 (count (:names registeredPhones))))) (repeat "Ottawa"))]
  (map (partial apply line/add105) params)))
(println (xml/emit-str reqList))

;CREATE LIST OF LOGGED IN DNs=====
;(def reqList (let [params (map list udplist/dn udplist/nameList)]
;              (map (partial apply line/add) params)))

;EXECUTE DN HTTP REQUESTS
;Can't use (doall) to make concurrent because webserver can't handle it
;(let [resp (map (partial client/axl AMER) reqList)]
;  (println (map deref resp)))


;CREATE LIST OF PHONES TO ADD
;[name description product loc dn e164Mask]
(def phoneName (map str (repeat "SEP") udplist/userid))
;(def description (map (partial format "Ottawa-HS%03d") (range 99 (+ 99 (count (:names registeredPhones))))))
(def description (map str udplist/nameList (repeat " IPC")))
(def product (repeat "Cisco IP Communicator"))
(def loc (repeat "Ottawa"))
(def dn (map str (repeat "#") (range 37498 (+ 37498 (count (:names registeredPhones))))))
(def e164Mask (repeat "6132381761"))

(def phoneParams (map list phoneName description product loc dn e164Mask))

;CREATE PHONES=====
(def phoneReqList (map (partial apply phone/add105) phoneParams))
(println (xml/emit-str phoneReqList))
;CREATE PHONE HTTP REQUESTS
;(let [resp (map (partial client/axl AMER) phoneReqList)]
;  (println (map deref resp)))


;CREATE DEVICE PROFILES
(def dpList (let [params (map list udplist/userid udplist/nameList udplist/dn)]
              (map (partial apply ucmXml/addDeviceProfile) params)))
(println (first dpList))

;(let [resp (map (partial client/axl AMER) dpList)]
;  (println (map deref resp)))


;UPDATE LDAP IMPORTED USER PROFILES
(def updateUserList (let [params (map list udplist/userid udplist/dn)]
              (map (partial apply ucmXml/udpateUser) params)))

;(let [resp (map (partial client/axl AMER) updateUserList)]
;  (println (map deref resp)))


;LOGIN TO EXTENSION MOBILITY
;(def emList (filter #(not= (second %) "") (map list (map str (repeat "SEP") udplist/userid) udplist/userid)))
;(def emReqList (map (partial apply ucmXml/doDeviceLogin) emList))
;(println emReqList)

;(let [resp (map (partial client/axl AMER) emReqList)]
;  (println (map deref resp)))

;(def partialstuff (partial phone/submitRequests))

;DELETE ITL

(println (:ips registeredPhones))
;(println phone/xmlList)
;(println (map (repeat "10.16.32.22") phone/xmlList))

;(def executeList
;  (doall (map phone/submitRequests '("10.16.32.156" "10.16.32.48"))))

;executeList



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
