(ns example02.fabric-sdk.ca
  (:require-macros [example02.fabric-sdk.macros :as m])
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(def ca (nodejs/require "fabric-ca-client"))

(defn new [addr]
  (new ca addr))

(defn enroll [ca username password]
  (m/pwrap (.enroll ca #js {:enrollmentID username :enrollmentSecret password})))
