(ns monkey.ci.pushover-test
  (:require  [clojure.test :refer [deftest testing is]]
             [monkey.ci.build
              [api :as ba]
              [core :as bc]]
             [monkey.ci.jobs :as j]
             [monkey.ci.plugin.pushover :as sut]
             [monkey.pushover.core :as p]))

(defn with-fake-pushover-fn [reply f]
  (let [inv (atom nil)]
    (with-redefs [p/post-message (fn [_ body]
                                   (future
                                      (reset! inv body)
                                      reply))]
       (f))
    inv))

(defmacro with-fake-pushover [reply & body]
  `(with-fake-pushover-fn ~reply (fn [] ~@body)))

(def default-opts
  {:msg "test message"
   :user "test-user"
   :token "test-token"})

(deftest pushover-msg
  (let [job (sut/pushover-msg default-opts)]
    (testing "creates an action job"
      (is (bc/action-job? job)))

    (testing "has default id"
      (is (= "pushover" (bc/job-id job))))

    (let [inv (with-fake-pushover {:status 200}
                (testing "job succeeds on status 200"
                  (is (bc/success? @(j/execute! job {})))))]
      
      (testing "sends message to pushover on execution"
        (is (map? @inv)))

      (testing "passes msg directly"
        (is (= "test message" (:message @inv))))

      (testing "passes user and token"
        (is (= "test-user" (:user @inv)))
        (is (= "test-token" (:token @inv))))))

  (testing "invokes `msg` with context if a fn"
    (let [job (sut/pushover-msg (assoc default-opts
                                       :msg (fn [ctx]
                                              (str "context property is " (:test-prop ctx)))))
          inv (with-fake-pushover {:status 200}
                @(j/execute! job {:test-prop "some test value"}))]
      (is (= "context property is some test value"
             (:message @inv)))))

  (testing "job fails if invocation fails"
    (with-fake-pushover {:status 500}
      (is (bc/failed? @(j/execute! (sut/pushover-msg default-opts) {})))))

  (testing "passes any additional options as job options"
    (is (= ["test-deps"]
           (-> (sut/pushover-msg {:dependencies ["test-deps"]})
               :dependencies))))

  (testing "fetches user and token from build params if not explicitly specified"
    (with-redefs [ba/build-params (constantly {"pushover-user" "test-user"
                                               "pushover-token" "test-token"})]
      (let [inv (with-fake-pushover {:status 200}
                  @(j/execute! (sut/pushover-msg {:msg "test"}) {}))]
        (is (= "test-user" (:user @inv)))
        (is (= "test-token" (:token @inv))))))

  (testing "can override default param keys"
    (with-redefs [ba/build-params (constantly {"test-user" "test-user"
                                               "test-token" "test-token"})]
      (let [inv (with-fake-pushover {:status 200}
                  @(j/execute! (sut/pushover-msg {:msg "test"
                                                  :user-param "test-user"
                                                  :token-param "test-token"})
                               {}))]
        (is (= "test-user" (:user @inv)))
        (is (= "test-token" (:token @inv))))))

  (testing "passes additional options to pushover"
    (let [inv (with-fake-pushover {:status 200}
                @(j/execute! (sut/pushover-msg (assoc default-opts :ttl 100)) {}))]
      (is (= 100 (:ttl @inv))))))
