(ns chat-app.id)

;; From https://gist.github.com/matemagyari/48598654c10fa58d5b99
(defn create-id-generator
  "Returns an impure function to generate ids"
  []
  (let [next-id (atom 0)]
    (fn []
      (swap! next-id inc)
      @next-id)))

;; here we create our "object"
(def generate-id! (create-id-generator))
