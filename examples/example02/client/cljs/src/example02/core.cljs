(ns example02.core
  (:require [cljs.nodejs :as nodejs]
            [example02.rpc :as rpc]
            [example02.fabric-sdk.core :as fabric]
            [example02.fabric-sdk.user :as fabric.user]
            [example02.util :as util]
            [promesa.core :as p :include-macros true]))

(def pb (nodejs/require "protobufjs"))
(def homedir (nodejs/require "homedir"))

(def builder (.newBuilder pb))

(defn- loadproto [name]
  (do
    (.loadProtoFile pb (str "./protos/" name ".proto") builder)
    (.build builder name)))

(def init (loadproto "appinit"))
(def app (loadproto "org.hyperledger.chaincode.example02"))

(defn connect [{:keys [path peer membersrvc username password]}]
  (let [path (str (homedir) "/.hyperledger/client/kvstore")
        client (fabric/new-client)
        chain (fabric/new-chain {:client client
                                 :name "chaintool-demo"
                                 :peers ["grpc://localhost:7051"]
                                 :orderers ["grpc://localhost:7050"]})
        eventhub (fabric/new-eventhub ["grpc://localhost:7053"])]

    ;; ensure our path is created
    (util/mkdirp path)

    ;; configure the chain environment and log in
    (p/alet [chain (p/await (fabric/new-chain "mychain"))
             kvstore (p/await (fabric/new-file-kv-store path))

             ;; configure the chain environment
             _ (p/await (p/all [(fabric/set-kv-store chain kvstore)
                                (fabric/set-membersrvc-url chain membersrvc)
                                (fabric/add-peer chain peer)]))

             ;; login using the provided username/password
             member (p/await (fabric/get-member chain username))
             _ (p/await (fabric.user/enroll member password))]

            {:chain chain :user member})))

(defn deploy [{:keys [args] :as options}]
  (-> options
      (assoc :func "init"
             :args (init.Init. args))
      rpc/deploy
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
