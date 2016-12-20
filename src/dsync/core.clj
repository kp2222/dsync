(ns dsync.core
  (:require
   [com.rpl.specter :as specter]
   [dsync.document-store :as ds]))


(def documents [{:tracking-id "1234560"
                 :document-versions [{:document-id "random-id-1"
                                      :latest? false
                                      :version "some-version-1"}
                                     {:document-id "random-id-1"
                                      :latest? true
                                      :version "some-version-1"}]}
                {:tracking-id "1234569"
                 :document-versions [{:document-id "random-id-1"
                                      :latest? false
                                      :version "some-version-1"}
                                     {:document-id "random-id-1"
                                      :latest? true
                                      :version "some-version-1"}]}])




(def source (ds/->MockStore (atom documents)))

(def destination (ds/->MockStore (atom [])))


(defn sync![source destination]
  (doseq [document (ds/fetch-all-documents source)]
    (ds/register-document destination document)
    (doseq [document-version (rest (:document-versions document))]
      (ds/supersede-document destination document document-version))))


(:documents source)
(:documents destination)

(sync! source destination)


