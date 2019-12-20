(ns myproject.StructMaps)

(defn pets
  "Sytucture of a pet"
  []
  (defstruct pet :PetType :PetName)
  (def scooby (struct pet "Dog" "Scooby"))
  (println scooby)
  (println (assoc scooby :PetName "Scooby-Doo"))

  )
