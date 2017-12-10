(ns opentext.xml.line
  (:require [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [opentext.xml.soapBase :as soap]))

(defn append-children
  [loc childList]
  (cond
    (empty? childList) (zip/root loc)
    :else (recur
            (zip/append-child loc (first childList))
            (rest childList))))

(defn add
  ([soapBase dn loc]
   (let [partition (str "PT-" loc "-Dev")
         css (str "CSS-" loc "-International")]
     (soapBase
       (xml/element :ns:addLine {:sequence "?"}
                    (xml/element :line {}
                                 (xml/element :pattern {} dn)
                                 (xml/element :usage {} "Device")
                                 (xml/element :routePartitionName {} partition)
                                 (xml/element :shareLineAppearanceCssName {} css)
                                 (xml/element :voiceMailProfileName {} "Voicemail_NA"))))))
  ([soapBase dn loc description]
   (let [lineBase (zip/xml-zip (add soapBase dn loc))
         cFWD (str "CSS-" loc "-National")
         cFWDsecondary (str "CSS-" loc "-Device")
         location (zip/down (zip/down (zip/right (zip/down lineBase))))]

     (append-children location
                  (list
                    (xml/element :description {} description)
                    (xml/element :alertingName {} description)
                    (xml/element :asciiAlertingName {} description)
                    (xml/element :callForwardAll {}
                                 (xml/element :forwardToVoiceMail {} "false")
                                 (xml/element :callingSearchSpaceName {} cFWD)
                                 (xml/element :secondaryCallingSearchSpaceName {} cFWDsecondary))
                    (xml/element :callForwardBusy {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD))
                    (xml/element :callForwardBusyInt {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD))
                    (xml/element :callForwardNoAnswer {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD))
                    (xml/element :callForwardNoAnswerInt {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD))
                    (xml/element :callForwardNoCoverage {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD))
                    (xml/element :callForwardNoCoverageInt {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD))
                    (xml/element :callForwardOnFailure {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD))
                    (xml/element :callForwardNotRegistered {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD))
                    (xml/element :callForwardNotRegisteredInt {}
                                 (xml/element :forwardToVoiceMail {} "true")
                                 (xml/element :callingSearchSpaceName {} cFWD)))))))


(def add85
  (partial add soap/base85))

(def add105
  (partial add soap/base105))


