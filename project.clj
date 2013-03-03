(defproject hexcanvas "0.1.0-SNAPSHOT"
  :description "Will one day be a hex game"
  :license {:name "MIT License"
            :url "https://github.com/graue/luasynth/blob/master/MIT-LICENSE.txt"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [rm-hull/monet "0.1.4-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds
               {:main {:source-paths ["src-cljs"]
                       :compiler {:output-to "js/main.js"
                                  :optimizations :whitespace
                                  :pretty-print true}}}}
  :source-paths ["no-clj-here"])
