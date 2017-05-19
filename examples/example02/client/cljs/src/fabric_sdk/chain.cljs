(ns fabric-sdk.chain
  (:require-macros [fabric-sdk.macros :as m])
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(def peer (nodejs/require "fabric-client/lib/Peer.js"))
(def orderer (nodejs/require "fabric-client/lib/Orderer.js"))

(defn new [client name]
  (.newChain client name))

(defn add-peer [chain instance]
  (.addPeer chain instance))
    
(defn add-orderer [chain instance]
  (.addOrderer chain instance))

(defn build-txnid [chain nonce user]
  (.buildTransactionID chain nonce user))

(defn set-dev-mode [chain enabled]
  (.setDevMode chain enabled))

(defn send-install-proposal [chain request]
  (m/pwrap (.sendInstallProposal chain request)))
