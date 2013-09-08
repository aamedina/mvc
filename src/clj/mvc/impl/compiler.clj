(ns mvc.impl.compiler
  (:import (clojure.lang IPersistentVector IPersistentMap ISeq Named)))

(declare -node)

(def re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(def container-tags
  #{"a" "article" "aside" "b" "body" "canvas" "dd" "div" "dl" "dt" "em" "fieldset"
    "footer" "form" "h1" "h2" "h3" "h4" "h5" "h6" "head" "header" "hgroup" "html"
    "i" "iframe" "label" "li" "nav" "object" "ol" "option" "pre" "section" "select"
    "script" "span" "strong" "style" "table" "textarea" "title" "ul" "video"})

(defn unevaluated?
  [expr]
  (or (symbol? expr) (and (seq? expr) (not= (first expr) `quote))))

(defn literal?
  [x]
  (and (not (unevaluated? x))
       (or (not (or (vector? x) (map? x)))
           (every? literal? x))))

(defn escape-html
  [args]
  (clojure.string/escape args {\& "&amp;" \< "&lt;" \>  "&gt;" \" "&quot;"}))

(defn attrs-str
  [attrs]
  (reduce
   (fn [attr-str attr]
     (str attr-str (when (seq attr-str) " ")
          (key attr) "=\"" (escape-html (val attr)) "\""))
   ""
   (clojure.walk/stringify-keys attrs)))

(defn normalize-attrs
  [tag attrs]
  (let [[tag & classes]
        (if (seq (:class attrs))
          (into (clojure.string/split (name tag) #"[\.]")
                (clojure.string/split (:class attrs) #" "))
          (clojure.string/split (name tag) #"[\.]"))        
        [tag id] (conj (clojure.string/split tag #"[#]") (:id attrs))
        attrs (into {} (for [[k v] attrs] {k (str v)}))
        attrs (-> attrs
                  (assoc :class (clojure.string/join " " classes))
                  (assoc :id id))]
    [tag (into {} (remove #(empty? (val %)) attrs))]))

(defn as-element
  [[tag & [attrs & children :as content] :as form]]
  (let [[tag attrs content]
        (if (map? attrs)
          (conj (normalize-attrs tag attrs) children)
          (conj (normalize-attrs tag {}) content))
        children (doall (map -node content))
        attrs (attrs-str attrs)]
    (println form)
    (str "<" (name tag) (when (seq attrs) " ") attrs ">"
         (-node content)
         "</" tag ">")))

(defn compile-form
  [[sym & args :as form]]
  (condp = sym
    'for (let [[bindings body] args]
           `(apply str (for ~bindings ~(-node body))))
    'if (let [[condition & body] args]
          `(if condition ~@(for [x body] (-node x))))
    `(-node ~form)))

(defn -node
  [form]
  (cond (seq? form) (apply str (map -node form))
        (vector? form) (as-element form)
        (map? form)
        (let [{:keys [tag attrs content]} form 
              [tag attrs] [`(name ~tag) (attrs-str attrs)]]
          `(str "<" ~tag ~(when (seq attrs) " ") ~attrs ">"
                ~@content
                "</" ~tag ">"))
        (keyword? form) (name form)
        (symbol? form) form
        (nil? form) ""
        :else (str form)))

(defn node [html] (identity html))
