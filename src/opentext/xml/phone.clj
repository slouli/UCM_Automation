(ns opentext.xml.phone
  (:require [clojure.data.xml :as xml]
            [opentext.xml.soapBase :as soap]
            [clojure.zip :as zip]))

;product <xsd:enumeration value="Cisco 7945"/>
;class <xsd:enumeration value="Phone"/>
;protocol "SCCP"
;protoclSide "User"
(defn add
  "Creates an XML configuration string to add a phone to Cisco UCM"
  ([soapBase name description product loc]
   (let [securityProfile
         (cond
           (= product "Cisco 7945") "Cisco 7945 - Standard SIP Non-Secure Profile"
           (= product "Cisco 7965") "Cisco 7965 - Standard SIP Non-Secure Profile"
           (= product "Cisco 8851") "Cisco 8851 - Standard SIP Non-Secure Profile")
         phoneTemplate
         (cond
           (= product "Cisco 7945") "OT 1 Line + Speed Dial - 7945 SIP"
           (= product "Cisco 7965") "OT 1 Line + SPeed Dial - 7965 SIP"
           (= product "Cisco 8851") "OT 1 Line + Speed Dial - 8851 SIP")]
     (soapBase
       (xml/element :ns:addPhone {:sequence "?"}
                    (xml/element :phone {:ctiid "?"}
                                 (xml/element :name {} name)
                                 (xml/element :description {} description)
                                 (xml/element :product {} product)
                                 (xml/element :class {} "Phone")
                                 (xml/element :protocol {} "SIP")
                                 (xml/element :protocolSide {} "User")
                                 (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-Device"))
                                 (xml/element :devicePoolName {} (str "DP-" loc "-IPC"))
                                 (xml/element :locationName {} (str "Loc-" loc))
                                 (xml/element :mediaResourceListName {} "MRL-NA")
                                 (xml/element :useTrustedRelayPoint {} "Default")
                                 (xml/element :securityProfileName {} securityProfile)
                                 (xml/element :sipProfileName {} "Standard SIP Profile")
                                 (xml/element :phoneTemplateName {} phoneTemplate)
                                 (xml/element :softkeyTemplateName {} "OT - Standard User - AbbrDial")
                                 (xml/element :ownerUserName {} "americas-lic-user")
                                 (xml/element :userLocale {} "English United States")
                                 (xml/element :networkLocale {} "Canada")
                                 (xml/element :enableExtensionMobility {} "true")
                                 (xml/element :vendorConfig {}
                                              (xml/element :settingsAccess {} "2")
                                              (xml/element :webAccess {} "0")))))))
  ([soapBase name description product loc dn e164Mask]
   "Include line configuration in the XML"
   (let [phoneBase (zip/xml-zip (add soapBase name description product loc))]
     (zip/root
       (zip/append-child
         (zip/down (zip/down (zip/right (zip/down phoneBase))))
         (xml/element :lines {}
                      (xml/element :line {}
                                   (xml/element :index {} "1")
                                   (xml/element :dirn {}
                                                (xml/element :pattern {} dn)
                                                (xml/element :routePartitionName {} (str "PT-" loc "-Dev")))
                                   (xml/element :e164Mask {} e164Mask)
                                   (xml/element :maxNumCalls {} "4")
                                   (xml/element :busyTrigger {} "2"))))))))


(def add105
  (partial add soap/base105))

;(println
;  (xml/emit-str (addPhone soap/base85 "name" "Descr" "Cisco 7945" "loc" "12345" "123456")))
