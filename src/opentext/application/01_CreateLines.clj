(ns opentext.application.01-CreateLines
  (:require [opentext.ucmXml :as ucmXml]
            [clojure.data.zip.xml :as zip-xml]
            [opentext.http.client :as client]
            [opentext.xml.phone :as phone]))

(comment
  01 Identify registered phones to be migrated - usually by device pool
  02 Pick start of the extension range
  03 Create Extensions)

(def not-nil? (complement nil?))

(defn flatmap
  "Flatten a sequence of sequences"
  [fn seq]
  (flatten (map fn seq)))

(defn filterPhones
  [phoneList phoneModelSet]
  (filter (fn [phone] (not-nil? (phoneModelSet (:model phone)))) (:phones phoneList)))

(defn groupRequests
  "UCM RisPort only supports requests up to 200 phones.
  This function creates sequence blocks of 200 devices"
  ([phoneList]
   (loop [phoneList phoneList returnList (empty '())]
     (cond
       (empty? phoneList) returnList
       :else (recur (drop 200 phoneList) (conj returnList (take 200 phoneList)))))))

(defn phoneMap
  [{:keys [ip ver] :as server} dp]
  (let [server {:ip ip :ver ver :method "listPhone" :xml (phone/listPhone dp)}
        request (client/axl server)
        zipper (ucmXml/zipper @request)
        format (fn [phone model] {:phone phone :model model})]
    {:phones
     (map format
          (zip-xml/xml-> zipper :Body :listPhoneResponse :return
                         :phone :name zip-xml/text)
          (zip-xml/xml-> zipper :Body :listPhoneResponse :return
                         :phone :model zip-xml/text))}))

(defn getRegisteredPhones1
  "A method for submitting a single xml request to the UCM risport to gather
  registered phone information.  The phone list must not contain more than 200
  devices due to limitations of the UCMs API."
  [{:keys [ip ver]} phoneList]
  (let [request (client/ris {:ip ip :ver ver :xml (ucmXml/risPort85 phoneList)})
        zipper (ucmXml/zipper @request)
        format (fn [phone user ip] {:phone phone :user user :ip ip})]
    (map format
         (zip-xml/xml-> zipper :Body :SelectCmDeviceResponse :SelectCmDeviceResult :CmNodes
                        :item :CmDevices :item :Name zip-xml/text)
         (zip-xml/xml-> zipper :Body :SelectCmDeviceResponse :SelectCmDeviceResult :CmNodes
                        :item :CmDevices :item :LoginUserId zip-xml/text)
         (zip-xml/xml-> zipper :Body :SelectCmDeviceResponse :SelectCmDeviceResult :CmNodes
                        :item :CmDevices :item :IPAddress :item :IP zip-xml/text))))

(defn getRegisteredPhones
  "Gathers all of the registered phones in the provided phoneList"
  [{:keys [ip ver] :as server} phoneList]
  (flatmap (partial getRegisteredPhones1 server) (groupRequests phoneList)))

;(defn getFilteredPhones
;  [{:keys [ip ver] :as server} xml phoneModels]
;  (flatmap (partial getRegisteredPhones server) (map ucmXml/risPort85 (groupRequests (filterPhones phones #{"Cisco 7945" "Cisco 7965"})))))

;==============================================================================

;(def from_waterloo
;  {:ip "10.2.92.50" :ver 8.5})
;
;(def to_americas
;  {:ip "10.230.154.5" :ver 10.5})

;(def phones
;  (phoneMap from_waterloo "OT-TOMP-DP"))

;;FIGURE OUT WAY TO GET SERVER AND XML PASSED TO FUNCTION
;(def registeredPhones
;  (getRegisteredPhones from_waterloo (:phones phones)))

;(println (:phones phones))
;(println registeredPhones)
;(println (count registeredPhones))
