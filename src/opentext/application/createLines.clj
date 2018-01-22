(ns opentext.application.createLines
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [opentext.application.01-CreateLines :as phones]))

(def clusters
  (->> "clusters.edn"
       io/resource
       slurp
       edn/read-string))

(def waterloo (->> clusters
                   :clusters
                   :Waterloo))

(def waterloo_phones
  (phones/phoneMap waterloo "OT-TOMP-DP"))

(def registeredPhones
  (phones/getRegisteredPhones waterloo (:phones waterloo_phones)))

(println registeredPhones)
(println (count registeredPhones))



