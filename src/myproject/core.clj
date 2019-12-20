(ns myproject.core
  (:gen-class)
  (:require [myproject.pets :as base])

  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (base/age-of-pet-to-human :DOG 3)
  )
