(ns example02.rpc
  (:require [cljs.nodejs :as nodejs]
            [fabric-sdk.core :as fabric]
            [fabric-sdk.chain :as fabric.chain]
            [promesa.core :as p :include-macros true]))

(defn- create-base-request [{:keys [chain peers channel id user]}]
  (let [nonce (fabric/get-nonce)
        txid (fabric/build-txnid nonce user)]

    {:chaincodeType "car"
     :targets peers
     :chainId channel
     :chaincodeId id
     :txId txid
     :nonce nonce}))

(defn- create-request [{:keys [user fcn args] :as options}]
  (-> (create-base-request options)
      (assoc :fcn fcn :args #js [(.toBuffer args)])))

(defn send-install [{:keys [client chain user path version] :as options}]
  (let [request (-> (create-base-request options)
                    (assoc :chaincodeVersion version
                           :chaincodePath path)
                    clj->js)]

    (when (not path)
      (fabric.chain/set-dev-mode chain true))

    (fabric/install-chaincode client request)))

(defn send-instantiate [& args])
(defn send-transaction [& args])
(defn send-query [& args])
