(ns myproject.car_dealer
  (:use [myproject.base :only [is-in?]]))

(defstruct discount-code :CODE :DF)

(def as-code :CODE)

(def discounts (seq [(struct discount-code "CD-ES7MSS" #(/ % 10.0))
                     (struct discount-code "CD-XS7M0S" #(/ (* 2 %) 10.0))
                     (struct discount-code "CD-Q00R21" #(/ (* 3 %) 10.0))
                     (struct discount-code "CD-W91MSP" #(/ % 10.0))
                     ]))

(defn coupons
  "Retrieve list of available coupons mapped as [as-fn] specifies"
  [as-fn]
  (if (not (nil? as-fn))
    (map as-fn discounts)
    discounts
    )
  )

(defn discount-fn-for-coupon
  [code]
  (:DF (first (filter #(= (:CODE %) code) discounts))))

(defn discount-price-fn-for-coupon
  "Apply discount of one [coupon] given its [code] to a [price]"
  [code]
  (fn [price] (- price ((discount-fn-for-coupon code) price)))
  )

(defstruct car-offer :BRAND :PRICE)

(def car-offers
  (seq [(struct car-offer "BMW" 45000)
        (struct car-offer "FERRARI" 68000)
        (struct car-offer "FIAT" 30000)
        (struct car-offer "MERCEDES-BENZ" 60000)
        ])
  )

(defn get-discounted-prices-calculator
  [discount-fn]
  (fn [car] (update car :PRICE discount-fn)))

(defn search-offer-for-a-budget
  "List accessible cars given the [budget] of the client and applying a discount if the [code] is valid"
  [budget, code]
  (if (is-in? (coupons as-code) code)
    (do
      (def discounted-car-offers (map (get-discounted-prices-calculator (discount-price-fn-for-coupon code)) car-offers))
      (println "COUPON VALID!")
      (println "BUDGET: $"budget)
      (println "ACCESSIBLE CARS FOR COUPON" code ":")
      (filter #(<= (:PRICE %) budget) discounted-car-offers)
      )
    (do
      (println "INVALID COUPON!!")
      (println "BUDGET: $"budget)
      (println "ACCESSIBLE CARS:")
      (filter #(<= (:PRICE %) budget) car-offers)
      )
    )
  )
