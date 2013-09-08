(ns mvc.core
  (:require
   [compojure.core :refer [GET defroutes]]
   [compojure.route :as route]
   [clojure.java.shell :refer [sh with-sh-dir]]
   [clojure.java.browse :as browse]
   [ring.util.response :refer [file-response]]
   [cemerick.austin.repls :refer [browser-connected-repl-js]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.reload :refer [wrap-reload]]))

(defroutes app
  (route/resources "/" {:root "resources/public"})
  (route/files "/" {:root "resources/public"})
  (GET "/repl" [] (browser-connected-repl-js))
  (GET "/*" [] (file-response "index.html"))
  (route/not-found (file-response "404.html")))

(def handler
  (-> app
      wrap-reload))

(defn start-server
  [port]
  (defonce server
    (run-jetty #'handler {:port port :join? false}))
  server)

(defn setup
  []
  (def repl-env
    (reset! cemerick.austin.repls/browser-repl-env (cemerick.austin/repl-env)))
  (cemerick.austin.repls/cljs-repl repl-env))

(defn -main
  [& args]
  (let [{:keys [host port repl?]
         :or {host "localhost" port 3000 repl? false}} args]
    (start-server port)
    (with-sh-dir "resources"
      (sh "npm" "install")
      (sh "grunt" "recess"))
    (println "Launching Jetty development server on http://" host ":" port "...")
    (if repl?
      (do (setup)
          (future (Thread/sleep 3000)
                  (browse/browse-url (str "http://" host ":" port))))
      (browse/browse-url (str "http://" host ":" port)))
    (println "Jetty listening on http://" host ":" port ".")
    (future
      (with-sh-dir "resources"        
        (sh "grunt" "watch")))))
