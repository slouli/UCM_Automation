(defproject opentext.ucm "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.2"]
                 [http-kit "2.2.0"]]
  :main ^:skip-aot opentext.ucm
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
