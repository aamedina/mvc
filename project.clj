(defproject mvc "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1859"]
                 [org.clojure/google-closure-library "0.0-20130212-95c19e7f0f5f"]
                 [org.clojure/google-closure-library-third-party
                  "0.0-20130212-95c19e7f0f5f"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [org.clojure/core.match "0.2.0-rc5"]
                 [org.clojure/data.json "0.2.3"]
                 [org.clojure/core.logic "0.8.4"]
                 [org.clojure/jvm.tools.analyzer "0.4.5"]
                 [org.clojure/core.memoize "0.5.6"]
                 [korma "0.3.0-RC5"]
                 [compojure "1.1.5"]
                 [ring "1.2.0"]
                 [prismatic/dommy "0.2.0"]]
  :jvm-opts ["-server" "-Xmx1g"]
  :repositories
  {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :plugins [[lein-marginalia "0.7.1"] [com.cemerick/austin "0.1.1"]
            [lein-cljsbuild "0.3.2"]]
  :source-paths ["src/clj" "src/cljs"]
  :cljsbuild
  {:builds
   {:main
    {:source-paths ["src/cljs"]
     :compiler
     {:optimizations :simple
      :output-to "resources/public/js/main.js"}}}})
