(ns myproject.ecommerce
  (:use [myproject.ecommerce.model :only [new_account new_item new_buyer_checkout]])
  )

(def transaction_outcome (atom {}))

(defn set-outcome
  ([message] (swap! transaction_outcome assoc :outcome message))
  ([message merchant] (swap! transaction_outcome assoc :outcome {:cause message :merchant merchant}))
  ([message merchant item] (swap! transaction_outcome assoc :outcome {:cause message :item item :merchant merchant}))
  )

(defn show_outcome []
  (do
    (print @transaction_outcome)
    (reset! transaction_outcome {})
    )
  )

(def bank
  (ref {"ID7327328" (new_account "ID7327328" 10210)
        "ID0027321" (new_account "ID0027321" 10)
        "ID2329862" (new_account "ID2329862" 1127)
        "ID7102367" (new_account "ID7102367" 210)
        }))

(defn alter_account [accounts account_id amount_mutator]
  (update accounts account_id #(update % :balance amount_mutator))
  )

(defn do-transfer [buyer merchant amount]
  (dosync
    (alter bank alter_account buyer #(- % amount))
    (alter bank alter_account merchant #(+ % amount))
    )
  )

(def buyer_checkouts (ref []))

(defn log-checkout [buyer item amount item_price merchant]
  (alter buyer_checkouts conj (new_buyer_checkout buyer item amount item_price merchant)))

(defn merchant_stock_validator [current_stock]
  (not
    (some #(< % 0)
          (map :stock (flatten (for [[_ v] current_stock] (for [[_ iv] v] iv))))
          )))

(def merchant_stock
  (ref {"ID7327328" {:PEN      {:price 1 :stock 29}
                     :NOTEBOOK {:price 10.0 :stock 31}
                     :BACKPACK {:price 15.0 :stock 33}
                     }
        "ID2329862" {:PEN        {:price 1.5 :stock 0}
                     :PENCIL     {:price 2 :stock 29}
                     :PAPER_CLIP {:price 0.5 :stock 29}
                     }}
       :validator merchant_stock_validator
       ))

(defn update-merchant-stock [merchant item amount]
  (dosync
    (alter merchant_stock update-in [merchant item :stock] #(- % amount)))
  )

(defn is_stock_not_empty_for_item [item_code items]
  (> (:stock (items item_code)) 0)
  )

(defn get_item_price [item current_merchant_items]
  (:price (current_merchant_items item))
  )

(defn get-price-when-item-is-in-stock [merchant item]
  (let [current_merchant_items (@merchant_stock merchant)]
    (cond
      (nil? current_merchant_items) (do (set-outcome "No such merchant!" merchant) -1)
      (is_stock_not_empty_for_item item current_merchant_items) (get_item_price item current_merchant_items)
      :default (do (set-outcome "No availability" merchant item) -1))
    )
  )

(defn buyer-balance-is-enough-for [item_price buyer]
  (let [buyer_bank_account (@bank buyer)]
    (cond
      (< item_price 0) false
      (not (nil? buyer_bank_account)) (if (> (:balance buyer_bank_account) item_price)
                                        true
                                        (do
                                          (set-outcome
                                            (str "Not enough money for the item price: " item_price))
                                          false))
      :default false
      ))

  )

(defn checkout-buy [buyer merchant item]
  (let [item_price (get-price-when-item-is-in-stock merchant item)]
    (dosync
      (when (buyer-balance-is-enough-for item_price buyer)
        (do-transfer buyer merchant item_price)
        (update-merchant-stock merchant item 1)
        (log-checkout buyer item 1 item_price merchant)
        (set-outcome "Item bought!")
        )
      (show_outcome)
      )
    )
  )
