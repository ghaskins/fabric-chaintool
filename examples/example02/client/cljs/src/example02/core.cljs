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
(def homedir (nodejs/require "homedir"))

(def builder (.newBuilder pb))

(defn- loadproto [name]
  (do
    (.loadProtoFile pb (str "./protos/" name ".proto") builder)
    (.build builder name)))

(def init (loadproto "appinit"))
(def app (loadproto "org.hyperledger.chaincode.example02"))

(def get-user [client ca username password]
  (-> (fabric/get-user-context client username)
      (p/then (fn [user]
                (if (and user (fabric.user/enrolled? user))
                  user
                  ;;else
                  (-> (fabric.ca/enroll ca username password)
                      (p/then (fn [enrollment]
                                (let [user (fabric.user/new client username)]
                                  (-> (fabric.user/set-enrollment enrollment)
                                      (p/then fabric/set-user-context)))))))))))

(defn connect [{:keys [path peer membersrvc username password]}]
  (let [path (str (homedir) "/.hyperledger/client/kvstore")
        client (fabric/new-client)
        chain (fabric.chain/new client "chaintool-demo")
        eventhub (fabric.eventhub/new)]

    (fabric.eventhub/set-peer-addr "grpc://localhost:7053")
    (fabric.eventhub/connect!)

    (fabric.chain/add-peer chain "grpc://localhost:7051")
    (fabric.chain/add-orderer chain "grpc://localhost:7050")

    ;; ensure our path is created
    (util/mkdirp path)

    (-> (fabric/new-default-kv-store path)
        (p/then (fn [kvstore]

                  (fabric/set-state-store client kvstore)

                  (let [ca (fabric.ca/new "http://localhost:7054")]
                    (-> (get-user client ca "admin" "adminpw")
                        (p/then {:client client
                                 :chain chain
                                 :eventhub eventhub
                                 :ca ca}))))))))

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
