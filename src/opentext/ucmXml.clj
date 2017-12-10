(ns opentext.ucmXml
  (:require [clojure.data.xml :as xml])
  (:require [org.httpkit.client :as http])
  (:require [clojure.zip :as zip])
  (:require [clojure.data.zip.xml :as zip-xml])
  (:require [opentext.http.client :as ucmhttp]))

;Base SOAP structure for all UCM AXL Requests
(defn soapBase [xmlBody]
  (def soapenv "http://schemas.xmlsoap.org/soap/envelope/")
  (def xmlns "http://www.cisco.com/AXL/API/8.5")
  (xml/element :soapenv:Envelope {:xmlns:soapenv soapenv :xmlns:ns xmlns}
               (xml/element :soapenv:Header {})
               (xml/element :soapenv:Body {}
                            xmlBody)))

(defn soapBase105 [xmlBody]
  (def soapenv "http://schemas.xmlsoap.org/soap/envelope/")
  (def xmlns "http://www.cisco.com/AXL/API/10.5")
  (xml/element :soapenv:Envelope {:xmlns:soapenv soapenv :xmlns:ns xmlns}
               (xml/element :soapenv:Header {})
               (xml/element :soapenv:Body {}
                            xmlBody)))

(defn getPhones [devicePool]
  (xml/emit-str (soapBase
    (xml/element :ns:listPhone {:sequence "?"}
                 (xml/element :searchCriteria {}
                              (xml/element :name {} "SEP%")
                              (xml/element :devicePoolName {} devicePool))
                 (xml/element :returnedTags {}
                              (xml/element :name {})
                              (xml/element :model {})
                              (xml/element :protocolSide {})
                              (xml/element :vendorConfig))))))


(defn addDeviceProfile
  [name description dn]
  (xml/emit-str
    (soapBase105
      (xml/element :ns:addDeviceProfile {:sequence "?"}
                   (xml/element :deviceProfile {}
                                (xml/element :name {} name)
                                (xml/element :description {} description)
                                (xml/element :product {} "Cisco 7945")
                                (xml/element :class {} "Device Profile")
                                (xml/element :protocol {} "SIP")
                                (xml/element :protocolSide {} "User")
                                (xml/element :userLocale {} "English United States")
                                (xml/element :lines {}
                                             (xml/element :line {}
                                                          (xml/element :index {} "1")
                                                          (xml/element :dirn {}
                                                                       (xml/element :pattern {} dn)
                                                                       (xml/element :routePartitionName {} (str "PT-Ottawa-Dev")))
                                                          (xml/element :display {} description)
                                                          (xml/element :displayAscii {} description)
                                                          (xml/element :e164Mask {} "6132381761")
                                                          (xml/element :maxNumCalls {} "4")
                                                          (xml/element :busyTrigger {} "2")))
                                (xml/element :phoneTemplateName {} "OT 1 Line + Speed Dial - 7945 SIP")
                                (xml/element :softkeyTemplateName {} "OT - Standard User - AbbrDial"))))))

(defn udpateUser
  [user dn]
  (xml/emit-str
    (soapBase105
      (xml/element :ns:updateUser {:sequence "?"}
                   (xml/element :userid {} user)
                   ;(xml/element :pin {} dn)
                   ;(xml/element :userLocale {} "English United States")
                   ;(xml/element :phoneProfiles {}
                   ;             (xml/element :profileName {} user))
                   ;(xml/element :defaultProfile {} user)
                   (xml/element :primaryExtension {}
                                (xml/element :pattern {} dn)
                                (xml/element :routePartitionName {} "PT-Ottawa-Dev"))
                   (xml/element :homeCluster {} "true")))))

(defn doDeviceLogin
  [device user]
  (xml/emit-str (soapBase105
                  (xml/element :ns:doDeviceLogin {}
                               (xml/element :deviceName {} device)
                               (xml/element :loginDuration {} "0")
                               (xml/element :profileName {} user)
                               (xml/element :userId {} user)))))

(def getCss
  (soapBase
    (xml/element :ns:getCss {:sequence "?"}
                 (xml/element "name" {} "CSS-Local"))))

(defn zipper
  [resp]
  (zip/xml-zip (xml/parse-str resp)))


;RISPort CODE

(def params
  {:soap "http://schemas.cisco.com/ast/soap/"
   :soapenc "http://schemas.xmlsoap.org/soap/encoding/"
   :soapenv "http://schemas.xmlsoap.org/soap/envelope/"
   :tns "http://schemas.cisco.com/ast/soap/"
   :types "http://schemas.cisco.com/ast/soap/encodedTypes"
   :xsi "http://www.w3.org/2001/XMLSchema-instance"
   :xsd "http://www.w3.org/2001/XMLSchema"})

(defn risPort85
  [deviceList]
  (xml/emit-str
  (xml/element :soapenv:Envelope {:xmlns:soapenv (get params :soapenv)
                                  :xmlns:xsd (get params :xsd)
                                  :xmlns:xsi (get params :xsi)}
               (xml/element :soapenv:Body {}
                            (xml/element :ns1:SelectCmDevice {:soapenv:encodingStyle (get params :soapenc)
                                                              :xmlns:ns1 (get params :soap)}
                                         (xml/element :StateInfo {:xsi:type "xsd:string"})
                                         (xml/element :CmSelectionCriteria {:href "#id0"}))
                            (xml/element :multiRef {:id "id0"
                                                    :soapenc:root "0"
                                                    :xsi:type "ns2:CmSelectionCriteria"
                                                    :soapenv:encodingStyle (get params :soapenc)
                                                    :xmlns:soapenc (get params :soapenc)
                                                    :xmlns:ns2 (get params :soap)}
                                         (xml/element :MaxReturnedDevices {:xsi:type "xsd:unsignedInt"} "1000")
                                         (xml/element :Class {:xsi:type "xsd:string"} "Phone")
                                         (xml/element :Status {:xsi:type "xsd:string"} "Registered")
                                         (xml/element :SelectBy {} "Name")
                                         (xml/element :SelectItems {:soapenc:arrayType (str "ns2:SelectItem[" (count deviceList) "]")
                                                                    :xsi:type "soapenc:Array"}
                                                      (map (fn [device] (xml/element :item {:xsi:type "soap:SelectItem"}
                                                                                     (xml/element :Item {:xsi:type "xsd:string"} device))) deviceList)))))))

(defn risPort105
  [deviceList]
  (xml/emit-str
    (xml/element :soapenv:Envelope {:xmlns:soapenv "htp://schemas.xmlsoap.org/soap/envelope/"
                                    :xmlns:soap "http://schemas.cisco.com/ast/soap"}
                 (xml/element :soapenv:Header {}
                              (xml/element :soapenv:Body {}
                                           (xml/element :soap:selectCmDevice {}
                                                        (xml/element :StateInfo {})
                                                        (xml/element :soap:CmSelectionCriteria {}
                                                                     (xml/element :soap:MaxReturnedDevices {} "1000")
                                                                     (xml/element :soap:DeviceClass {} "Any")
                                                                     (xml/element :soap:Model {} "255")
                                                                     (xml/element :soap:Status {} "Any")
                                                                     (xml/element :soap:NodeName {})
                                                                     (xml/element :soap:SelectBy {} "Name")
                                                                     (xml/element :soap:SelectItems {}
                                                                                  (xml/element :soap:item {}
                                                                                               (map (fn [device] (xml/element :soap:Item {} device)) (list (first deviceList)))))
                                                                     (xml/element :soap:Protocol {} "Any")
                                                                     (xml/element :soap:DownloadStatus {} "Any"))))))))

;(def risPortStr (xml/emit-str risPort))
