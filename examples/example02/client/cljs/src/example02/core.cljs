(ns example02.core
  (:require [cljs.nodejs :as nodejs]
            [example02.rpc :as rpc]
            [fabric-sdk.core :as fabric]
            [fabric-sdk.chain :as fabric.chain]
            [fabric-sdk.eventhub :as fabric.eventhub]
            [fabric-sdk.user :as fabric.user]
            [promesa.core :as p :include-macros true]))

(def pb (nodejs/require "protobufjs"))

(def builder (.newBuilder pb))

(defn- loadproto [name]
  (do
    (.loadProtoFile pb (str "./protos/" name ".proto") builder)
    (.build builder name)))

(def init (loadproto "appinit"))
(def app (loadproto "org.hyperledger.chaincode.example02"))

(defn- set-state-store [client path]
  (-> (fabric/new-default-kv-store path)
      (p/then #((fabric/set-state-store client %)))))

(defn- create-user [client identity]
  (let [config #js {:username (:principal identity)
                    :mspid (:mspid identity)
                    :cryptoContent #js {:privateKeyPEM (:privatekey identity)
                                        :signedCertPEM (:certificate identity)}}]

    (fabric.chain/create-user client config)))

(defn connect! [{:keys [config id channel] :as options}]

  (let [client (fabric/new-client)]

    (-> (set-state-store client ".hfc-kvstore")
        (p/then #(create-user client (:identity config)))
        (p/then (fn [user]

                  (let [chain (fabric.chain/new client channel)
                        eventhub (fabric.eventhub/new)]

                    (fabric.eventhub/set-peer-addr eventhub (make-url peer event-port))
                    (fabric.eventhub/connect! eventhub)

                    (fabric.chain/add-peer chain (make-url peer peer-port))
                    (fabric.chain/add-orderer chain orderer))              
                  
                  user)))))

(defn disconnect! [{:keys [eventhub]}]
  (fabric.eventhub/disconnect! eventhub))

(defn install [{:keys [args] :as context}]
  (-> (rpc/send-install context)
      (p/then #(println "Success!"))))

(defn instantiate [{:keys [args] :as context}]
  (-> context
      (assoc :func "init"
             :args (init.Init. args))
      rpc/send-instantiate
      (p/then #(println "Success!"))))

(defn make-payment [{:keys [args] :as context}]
  (-> context
      (assoc :func "org.hyperledger.chaincode.example02/fcn/1"
             :args (app.PaymentParams. args))
      rpc/send-transaction
      (p/then #(println "Success!"))))

(defn delete-account [{:keys [args] :as context}]
  (-> context
      (assoc :func "org.hyperledger.chaincode.example02/fcn/2"
             :args (app.Entity. args))
      rpc/send-transaction
      (p/then #(println "Success!"))))

(defn check-balance [{:keys [args] :as context}]
  (-> context
      (assoc :func "org.hyperledger.chaincode.example02/fcn/3"
             :args (app.Entity. args))
      rpc/send-transaction
      (p/then #(println "Success: Balance =" (->> % app.BalanceResult.decode64 .-balance)))))
