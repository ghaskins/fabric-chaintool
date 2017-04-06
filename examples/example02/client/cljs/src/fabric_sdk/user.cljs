(ns fabric-sdk.user
  (:require-macros [fabric-sdk.macros :as m])
  (:require [cljs.nodejs :as nodejs]
            [promesa.core :as p :include-macros true]))

(def user (nodejs/require "fabric-client/lib/User.js"))

(defn new [username client]
  (new user username client))

(defn enrolled? [user]y
  (.isEnrolled user))

(defn set-enrollment [user enrollment]
  (m/pwrap (.setEnrollment user enrollment.key enrollment.certificate)))
