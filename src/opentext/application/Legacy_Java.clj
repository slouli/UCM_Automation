(ns opentext.application.02-AddPhones
  (:import [com.cisco.axl.api._10 GetLineReq]
           [com.cisco.axlapiservice AXLAPIService]
           [java.net URL]
           [javax.net.ssl X509TrustManager]))

(def url
  (URL. "https" "usdc5-ucm1.opentext.com" 8443 "/axl/"))

(def url2
  (URL. "file:/Users/slouli/Desktop/Clojure/opentext.ucm/resources/wsdl/AXLAPI_10.wsdl"))

(def _getLine
  (GetLineReq.))

(->> (setPattern "60362")
     (. _getLine))

(println (. _getLine (getPattern)))

(println (. _getLine (getSequence)))

(def axlService
  (AXLAPIService. url2))

(->> (getLine _getLine)
     (. axlService))
