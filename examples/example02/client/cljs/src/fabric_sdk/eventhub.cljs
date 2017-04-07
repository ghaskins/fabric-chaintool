(ns fabric-sdk.eventhub
  (:require-macros [fabric-sdk.macros :as m])
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(def eventhub (nodejs/require "fabric-client/lib/Eventhub.js"))

(defn new []
  (new eventhub))

(defn set-peer-addr [instance addr]
  (.setPeerAddr instance addr))

(defn connect! [instance]
  (.connect instance))

(defn disconnect! [instance]
  (.disconnect instance))
