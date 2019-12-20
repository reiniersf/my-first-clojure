(ns myproject.base)

(defn is-in?
  [coll value]
  (not
    (nil?
      (some #(= value %) coll)
      )
    )
  )




