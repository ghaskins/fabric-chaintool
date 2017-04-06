(ns example02.fabric-sdk.core
  (:require-macros [example02.fabric-sdk.macros :as m])
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(def hfc (nodejs/require "fabric-client"))
(def utils (nodejs/require "fabric-client/lib/utils.js"))

(defn new-client []
  (new hfc))

(defn new-default-kv-store [path]
  (m/pwrap (.newDefaultKeyValStore hfc #js {:path path})))

(defn set-state-store [client store]
  (.setStateStore client store))

(defn get-user-context [client username]
  (m/pwrap (.getUserContext client username)))

(defn set-user-context [client user]
  (m/pwrap (.setUserContext client user)))
