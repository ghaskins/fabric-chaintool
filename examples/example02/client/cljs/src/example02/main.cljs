(ns example02.main
  (:require [clojure.string :as string]
            [cljs.nodejs :as nodejs]
            [cljs.tools.cli :refer [parse-opts]]
            [example02.core :as core]
            [promesa.core :as p :include-macros true]))

(nodejs/enable-util-print!)

(def _commands
  [["install"
    {:fn core/install}]
   ["instantiate"
    {:fn core/instantiate
     :default-args #js {:partyA #js {:entity "foo"
                                     :value 100}
                        :partyB #js {:entity "bar"
                                     :value 100}}}]
   ["make-payment"
    {:fn core/make-payment
     :default-args #js {:partySrc "foo"
                        :partyDst "bar"
                        :amount 10}}]
   ["delete-account"
    {:fn core/delete-account
     :default-args #js {:id "foo"}}]
   ["check-balance"
    {:fn core/check-balance
     :default-args #js {:id "foo"}}]])

(def commands (into {} _commands))
(defn print-commands [] (->> commands keys vec print-str))

(def options
  [[nil "--peer HOST" "Peer HOST"
    :default "localhost"]
   [nil "--peer-port PORT"
    :default 7051
    :parse-fn #(js/parseInt %)
    :validate [#(< 0 % 65535) "Must be between 1 and 65535"]]
   [nil "--event-port PORT"
    :default 7053
    :parse-fn #(js/parseInt %)
    :validate [#(< 0 % 65535) "Must be between 1 and 65535"]]
   [nil "--orderer URL" "Orderer URL"
    :default "grpc://localhost:7050"]
   [nil "--ca URL" "CA URL"
    :default "grpc://localhost:7054"]
   [nil "--username USER" "Username"
    :default "admin"]
   [nil "--password PASS" "Password"
    :default "adminpw"]
   ["-i" "--id ID" "ChaincodeID as a path/url/name"]
   [nil "--chainid ID" "ChainID"
    :default "testchainid"]
   ["-c" "--command CMD" (str "One of " (print-commands))
    :default "check-balance"
    :validate [#(contains? commands %) (str "Supported commands: " (print-commands))]]
   ["-a" "--args ARGS" "JSON formatted arguments to submit"]
   ["-h" "--help"]])

(defn exit [status msg & rest]
  (do
    (apply println msg rest)
    status))

(defn prep-usage [msg] (->> msg flatten (string/join \newline)))

(defn usage [options-summary]
  (prep-usage ["Usage: example02 [options]"
               ""
               "Options Summary:"
               options-summary
               ""]))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args options)
        {:keys [id command args]} options]
    (cond

      (:help options)
      (exit 0 (usage summary))

      (not= errors nil)
      (exit -1 "Error: " (string/join errors))

      (nil? id)
      (exit -1 "Error: Must specify a chaincodeID")

      :else
      (let [desc (commands command)
            _args (if (nil? args) (:default-args desc) (.parse js/JSON args))]

        (p/alet [{:keys [eventhub] :as context} (p/await (core/connect! options))
                 params (-> options
                            (assoc :args _args)
                            (merge context))]

                (println (str "Running " command "(" (.stringify js/JSON _args) ")"))

                ;; Run the subcommand funtion
                (-> ((:fn desc) params)
                    (p/catch #(println "Error:" %))
                    (p/then (fabric.eventhub/disconnect! eventhub))))))))

(set! *main-cli-fn* -main)
