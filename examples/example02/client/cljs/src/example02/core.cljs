(ns example02.core
  (:require [cljs.nodejs :as nodejs]
            [example02.rpc :as rpc]
            [example02.util :as util]
            [fabric-sdk.core :as fabric]
            [fabric-sdk.chain :as fabric.chain]
            [fabric-sdk.eventhub :as fabric.eventhub]
            [fabric-sdk.ca :as fabric.ca]
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

(defn- enroll [client ca username password]
  (-> (fabric.ca/enroll ca username password)
      (p/then (fn [enrollment]
                (let [user (fabric.user/new client username)]
                  (-> (fabric.user/set-enrollment user enrollment)
                      (p/then #(fabric/set-user-context client user))))))))

(defn- get-user [client ca username password]
  (-> (fabric/get-user-context client username)
      (p/then (fn [user]
                (if (fabric.user/enrolled? user)

                  ;; either we found an enrolled user cached in the store
                  user

                  ;;else, we need to enroll them now
                  (enroll client ca username password))))))

(defn- make-url [host port]
  (str "grpc://" host ":" port))

(defn connect! [{:keys [peer peer-port event-port ca username password]}]
  (let [client (fabric/new-client)
        chain (fabric.chain/new client "chaintool-demo")
        eventhub (fabric.eventhub/new)]

    (fabric.eventhub/set-peer-addr eventhub (make-url peer event-port))
    (fabric.eventhub/connect! eventhub)

    (fabric.chain/add-peer chain (make-url peer peer-port))
    (fabric.chain/add-orderer chain orderer)

    (-> (fabric/new-default-kv-store ".hfc-kvstore")
        (p/then (fn [kvstore]

                  (fabric/set-state-store client kvstore)

                  (let [ca-instance (fabric.ca/new ca)]
                    (-> (get-user client ca-instance username password)
                        (p/then {:client client
                                 :chain chain
                                 :eventhub eventhub
                                 :ca ca-instance}))))))))

(defn install [{:keys [args] :as options}]
  (-> (rpc/send-install options)
      (p/then #(println "Success!"))))

(defn instantiate [{:keys [args] :as options}]
  (-> options
      (assoc :func "init"
             :args (init.Init. args))
      rpc/instantiate
      (p/then #(println "Success!"))))

(defn make-payment [{:keys [args] :as options}]
  (-> options
      (assoc :func "org.hyperledger.chaincode.example02/txn/1"
             :args (app.PaymentParams. args))
      rpc/invoke
      (p/then #(println "Success!"))))

(defn delete-account [{:keys [args] :as options}]
  (-> options
      (assoc :func "org.hyperledger.chaincode.example02/txn/2"
             :args (app.Entity. args))
      rpc/invoke
      (p/then #(println "Success!"))))

(defn check-balance [{:keys [args] :as options}]
  (-> options
      (assoc :func "org.hyperledger.chaincode.example02/query/1"
             :args (app.Entity. args))
      rpc/query
      (p/then #(println "Success: Balance =" (->> % app.BalanceResult.decode64 .-balance)))))
