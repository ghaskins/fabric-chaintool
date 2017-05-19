(ns fabric-sdk.core
  (:require-macros [fabric-sdk.macros :as m])
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(def hfc (nodejs/require "fabric-client"))
(def utils (nodejs/require "fabric-client/lib/utils.js"))

(defn new-client []
  (new hfc))

(defn new-default-kv-store [path]
  (m/pwrap (.newDefaultKeyValueStore hfc #js {:path path})))

(defn set-state-store [client store]
  (.setStateStore client store))

(defn get-user-context [client username]
  (m/pwrap (.getUserContext client username)))

(defn set-user-context [client user]
  (m/pwrap (.setUserContext client user)))

(defn create-user [client spec]
  (m/pwrap (.createUser client spec)))

(defn new-orderer [client url opts]
  (.newOrderer client url opts))

(defn new-peer [client url opts]
  (.newPeer client url opts))

(defn build-txnid [nonce user]
  (.buildTransactionID hfc nonce user))

(defn get-nonce []
  (.getNonce utils))
