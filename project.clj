(defproject hex "0.1.0-SNAPSHOT"
  :description "Hex game for browsers"
  :url "https://github.com/graue/cljs-hex"
  :license {:name "MIT License"
            :url "https://github.com/graue/luasynth/blob/master/MIT-LICENSE.txt"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [rm-hull/monet "0.1.9"]]
  :plugins [[lein-cljsbuild "1.0.1"]]
  :cljsbuild {:builds
               [{:id "main"
                 :source-paths ["src-cljs"]
                 :compiler {:output-to "hex.js"
                            :output-dir "out"
                            :optimizations :none
                            :source-map true}}
                {:id "optimized"
                 :source-paths ["src-cljs"]
                 :compiler {:output-to "hex-opt.js"
                            :optimizations :advanced
                            :pretty-print false}}]}
  :source-paths ["src-cljs"])
