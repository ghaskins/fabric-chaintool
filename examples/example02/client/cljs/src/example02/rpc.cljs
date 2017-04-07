(ns example02.rpc
  (:require [cljs.nodejs :as nodejs]
            [fabric-sdk.core :as fabric]
            [fabric-sdk.chain :as fabric.chain]
            [promesa.core :as p :include-macros true]))

(defn- create-base-request [{:keys [chain peers chainid id user]}]
  (let [nonce (fabric/get-nonce)
        txid (fabric.chain/build-txnid chain nonce user)]

    {:chaincodeType "car"
     :targets peers
     :chainId chainid
     :chaincodeId id
     :txId txid
     :nonce nonce}))

(defn- create-request [{:keys [user fcn args] :as options}]
  (-> (create-base-request options)
      (assoc :fcn fcn :args #js [(.toBuffer args)])))

(defn- send-install [{:keys [user path version] :as options}]
  (let [request #js (-> (create-base-request options)
                        (assoc :chaincodeVersion version)
                        (when path (assoc :chaincodePath path)))]

    (when (not path)
      (fabric.chain/set-dev-mode true))

    (fabric.chain/send-install-proposal request)))
