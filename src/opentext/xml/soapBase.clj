(ns opentext.xml.soapBase
  (:require [clojure.data.xml :as xml]))

(defn soapBase [ver xmlBody]
  (let [soapenv "http://schemas.xmlsoap.org/soap/envelope/"
        xmlns (str "http://www.cisco.com/AXL/API/" ver)]
    (xml/element :soapenv:Envelope {:xmlns:soapenv soapenv :xmlns:ns xmlns}
                 (xml/element :soapenv:Header {})
                 (xml/element :soapenv:Body {}
                              xmlBody))))

(def base85
  (partial soapBase "8.5"))

(def base105
  (partial soapBase "10.5"))

