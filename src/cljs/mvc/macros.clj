(ns mvc.macros
  (:refer-clojure :exclude [future dosync sync])
  (:require
   [cljs.core]
   [cljs.analyzer :as analyzer]
   [clojure.walk :as walk]
   [cljs.closure :as closure]
   [clojure.java.io :as io]
   [mvc.impl.compiler :as compiler :refer [-node]]))

(defmacro dochan [[binding chan] & body]
  `(let [chan# ~chan]
     (cljs.core.async.macros/go
      (loop []
        (if-let [~binding (cljs.core.async/<! chan#)]
          (do
            ~@body
            (recur))
          :done)))))

(defmacro go-loop
  [bindings & body]
  `(go (loop ~bindings
         ~@body)))

(defmacro go!
  [bindings & body]
  `(cljs.core.async.macros/go
    (while true
      (let ~bindings
        ~@body))))

(defmacro defcollection
  [name spec]
  `(def ~name (partial 'mvc.core.collection/collection ~spec)))

(defmacro defmodel
  [name spec]
  `(def ~name (partial 'mvc.core.model/model ~spec)))

(defmacro defview
  [name spec])

(defmacro with-resource
  [binding close-fn & body]
  `(let ~binding
     (try (do ~@body)
          (finally
            (~close-fn ~(binding 0))))))

(defmacro with-api
  [prefix & body]
  `(binding [mvc.browser.ajax/*api* ~prefix]
     ~@body))

(defmacro with-agent
  [a & body]
  `(binding [mvc.impl.agent/*agent* ~a]
     (do ~@body)))

(defmacro with-worker
  [w & body]
  `(binding [mvc.browser.worker/*worker* ~w]
     (try (do ~@body)
          (catch js/Error err "error goes here"))))

(defmacro with-db
  [db & body]
  `(binding [mvc.browser.db/*db* ~db]
     (try (do ~@body)
          (catch js/Error err "error goes here"))))

(defmacro with-fs
  [fs & body]
  `(binding [mvc.browser.fs/*fs* ~fs]
     (try (do ~@body)
          (catch js/Error err "error goes here"))))

(declare compile-seq compile-html)

(defn compile-element
  [[tag & [attrs & children :as content] :as element]]
  (let [[tag attrs content]
        (if (map? attrs)
          (conj (compiler/normalize-attrs tag attrs) children) 
          (conj (compiler/normalize-attrs tag {}) content))
        children (doall (map compile-seq content))]
    (compiler/-node {:tag tag :attrs attrs :content children})))

(defn compile-form
  [[sym & args :as form]]
  (condp = sym
    'for (let [[bindings body] args]
           `(apply str (for ~bindings ~(compile-html body))))
    'if (let [[condition & body] args]
          `(if ~condition ~@(for [x body] (compile-html x))))
    `(-node ~form)))

(defn compile-seq
  [form]
  (cond (seq? form)
        (cond (symbol? (first form)) (compile-form form)
              :else (map compile-seq form))
        (vector? form)
        (cond (every? compiler/literal? form) (compile-element form)
              (map? (second form))
              (if (some seq? (vals (second form)))
                (compile-form form)
                (compile-element form))
              :else
              (compiler/-node {:tag (first form) :attrs {} :content (rest form)}))
        (compiler/literal? form) form
        (keyword? form) (compile-element [(name form) {} ""])
        (string? form) form
        (number? form) form
        :else form))

(defn compile-html
  [& content]
  `(str ~@(doall (for [form content] (compile-seq form)))))

(defmacro node
  [body]
  (let [frag# (compile-html body)]
    `(mvc.impl.compiler/node ~frag#)))

(defmacro defpartial
  [name args & body]
  (let [frag# (apply compile-html body)]
    `(defn ~name ~args
       (mvc.impl.compiler/node ~frag#))))

(defmacro defgoog
  [name]
  name)

(defn upper-case?
  [s]
  (empty? (-> (clojure.string/replace s #"[^A-Za-z]" "")
              (clojure.string/replace #"[A-Z]" ""))))

(defn clojurify
  [m lib-sym]
  (let [{:keys [method doc-string]} m
        doc-string (eval doc-string)
        method-name (if (upper-case? method)
                      (clojure.string/replace method #"[_]" "-")
                      (if (upper-case? (first method))
                        method
                        (-> (clojure.string/replace method #"([A-Z])" "-$1")
                            (clojure.string/replace #"[_]" "-")
                            (clojure.string/lower-case))))
        method-sym (symbol method-name)
        cljs-fn (if (upper-case? (first method-name))
                  `(def ~method-sym
                     ~(symbol (str lib-sym "/" method)))
                  `(def ~method-sym
                     ~(symbol (str lib-sym "/" method)))
                  ;; `(defn ~(with-meta method-sym
                  ;;           (assoc (meta method-sym) :doc doc-string))
                  ;;    [& args#]
                  ;;    (apply ~(symbol (str lib-sym "/" method))
                  ;;           (map cljs.core/clj->js args#)))
                  )]
    cljs-fn))

(defmacro import-goog
  [libs]
  (let [defs#
        (reduce
         (fn [defs lib]
           (let [libname (clojure.string/lower-case (str lib))
                 libpath-split (clojure.string/split libname #"\.")
                 libpath-prefix (clojure.string/join "/" libpath-split)
                 libpath-suffix (if (> (count libpath-split) 2)
                                  ".js"
                                  (str "/" (last libpath-split) ".js"))
                 libpath (str libpath-prefix libpath-suffix)
                 libstring (slurp (io/resource libpath))
                 methods (->> libstring
                              (re-seq (re-pattern
                                       (str libname "\\" "." "(" "\\" "w+)")))
                              (map last)
                              (into #{})
                              (map (fn [method]
                                     (let [qualified (str lib "." method)
                                           doc-string
                                           (-> (str "/\\*\\*(.*)" qualified " =")
                                               (re-pattern)
                                               (re-seq libstring))]
                                       {:method method :doc-string doc-string})))
                              (map #(clojurify % lib)))
                 cljs-libpath (clojure.string/replace libpath #"\.js$" ".cljs")]
             (into defs methods)))
         [] libs)]
    `(do ~@(for [def# defs#] def#))))

(defmacro future
  [& body]
  `(cljs.core.async.macros/go ~@body))

(defmacro cell
  [])

(defn event-stream
  [])

(defn behavior
  [])

(defmacro atomic
  [& body]
  ~@body)

(defmacro sync
  [& body]
  `(mvc.impl.transaction/locking-transaction (fn [] ~@body)))

(defmacro dosync
  [& exprs]
  `(sync nil ~@exprs))
