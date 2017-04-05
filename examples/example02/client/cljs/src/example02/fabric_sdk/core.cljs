(ns example02.fabric-sdk.core
  (:require-macros [example02.fabric-sdk.macros :as m])
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(def hfc (nodejs/require "fabric-client"))
(def utils (nodejs/require "fabric-client/lib/utils.js"))
(def peer (nodejs/require "fabric-client/lib/Peer.js"))
(def orderer (nodejs/require "fabric-client/lib/Orderer.js"))
(def eventhub (nodejs/require "fabric-client/lib/Eventhub.js"))
(def ca (nodejs/require "fabric-ca-client"))
(def user (nodejs/require "fabric-client/lib/User.js"))

(defn new-client []
  (new hfc))

(defn new-chain [{:keys [client name peers orderers]}]
  (let [chain (.newChain client name)]

    (doseq [peer peers]
      (let [p (new peer addr)]
        (.addPeer chain p)))

    (doseq [addr orderers]
      (let [o (new orderer addr)]
        (.addOrderer chain o)))

    chain))

(defn new-eventhub [peers]
  (let [instance (new eventhub)]

    (doseq [addr peers]
      (.setPeerAddr instance addr))

    (.connect instance)

    instance))

(defn new-default-kv-store [path]
  (p/promise
   (fn [resolve reject]
     (-> (.newDefaultKeyValStore hfc #js {:path path})
         (.then #(resolve %) #(reject %))))))

(defn set-kv-store [chain store]
  (p/do* (.setKeyValStore chain store)))

(defn set-membersrvc-url [chain url]
  (p/do* (.setMemberServicesUrl chain url)))

(defn add-peer [chain url]
  (p/do* (.addPeer chain url)))

(defn get-member [chain username]
  (p/promise
   (fn [resolve reject]
     (.getMember chain username
                 (fn [err member]
                   (if err
                     (reject err)
                     (resolve member)))))))

(defn register-and-enroll [chain request]
  (p/promise
   (fn [resolve reject]
     (.registerAndEnroll chain request
                         (fn [err user]
                           (if err
                             (reject err)
                             (resolve user)))))))
