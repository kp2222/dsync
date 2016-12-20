(ns dsync.document-store
  (:require
   [com.rpl.specter :as specter]))

(defprotocol DocumentStore
  (fetch-all-documents[this])
  (register-document[this document])
  (supersede-document[this document document-version]))



;; A mock datastore implementation for simulation during development

(defn- document-exists? [{:keys [tracking-id] :as document} documents]
  (some #(= tracking-id (:tracking-id %)) @documents))

(defn document-with-only-first-version [document]
  (specter/transform [:document-versions] #(vec (take 1 %)) document))

(defn- add-document! [document documents-atom]
  (swap! documents-atom  conj (document-with-only-first-version document)))

(defn add-document-version [{:keys [tracking-id ]} document-version documents]
  (let [matching-document? #(= (:tracking-id %) tracking-id)
        version-append-fn   #(conj % document-version) ]
    (specter/transform [specter/ALL (specter/pred matching-document?) :document-versions] version-append-fn documents)))

(defrecord MockStore [documents]
  DocumentStore
  (fetch-all-documents[this] @documents)
  (register-document[this {:keys [tracking-id] :as document}]
    (if-not (document-exists? document documents)
      (add-document! document documents)))
  (supersede-document [this document document-version]
    (if (document-exists? document documents)
      (reset! documents (add-document-version document document-version @documents)))))


