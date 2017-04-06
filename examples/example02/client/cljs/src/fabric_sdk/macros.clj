(ns fabric-sdk.macros
    (:require [promesa.core :as promesa]))

(defmacro pwrap
  ;; Implements an interop wrapper between JS promise and promesa
  [expr]
  `(promesa/promise
    (fn [resolve# reject#]
      (-> ~expr
          (.then #(resolve# %)
                 #(reject# %))))))
