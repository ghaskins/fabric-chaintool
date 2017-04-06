(ns example02.rpc
  (:require [cljs.nodejs :as nodejs]
            [fabric-sdk.core :as fabric]
            [fabric-sdk.user :as fabric.user]
            [promesa.core :as p :include-macros true]))

(defn- create-base-request [{:keys [chain peers id user]}]
  (let [nonce (fabric/get-nonce)
        txid (fabric.chain/build-txnid chain nonce user)]

    #js {:chaincodeType "car"
         :targets peers
         :chainId "testchainid"
         :chaincodeId id
         :txId txid
         :nonce nonce}))

(defn- post [method {:keys [user id func args] :as options}]
  (let [request #js {:chaincodeID id
                     :fcn func
                     :args #js [(.toBase64 args)]}]

    (p/chain (method user request) str)))

(defn deploy [args]
  (post fabric.user/deploy args))

(defn invoke [args]
  (post fabric.user/invoke args))

(defn query [args]
  (post fabric.user/query args))
