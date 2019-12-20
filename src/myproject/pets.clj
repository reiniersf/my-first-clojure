(ns myproject.pets)

(defn age-of-pet-to-human
  "Just a function"
  [pkey, age]
  (let [pets {:DOG #(* 7 %), :CAT #(* 5 %), :FISH #(* 10 %)}]
    ((get pets pkey) age)
    )
  )
