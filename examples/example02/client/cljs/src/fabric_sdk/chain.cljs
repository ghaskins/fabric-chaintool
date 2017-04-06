(ns fabric-sdk.chain
  (:require-macros [fabric-sdk.macros :as m])
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(def peer (nodejs/require "fabric-client/lib/Peer.js"))
(def orderer (nodejs/require "fabric-client/lib/Orderer.js"))

(defn new [client name]
  (.newChain client name))

(defn add-peer [chain addr]
  (let [p (new peer addr)]
    (.addPeer chain p)))

(defn add-orderer [chain addr]
  (let [o (new orderer addr)]
    (.addOrderer chain o)))

(defn set-kv-store [chain store]
  (p/do* (.setKeyValStore chain store)))

(defn set-membersrvc-url [chain url]
  (p/do* (.setMemberServicesUrl chain url)))

(defn build-txnid [chain nonce user]
  (.buildTransactionID chain nonce user))

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
