(ns myproject.ecommerce
  (:use [myproject.ecommerce.model :only [new_account new_item new_buyer_checkout]])
  )

(def transaction_outcome (atom {}))

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

(defn merchant_stock_validator [stock]
  (nil? (some #(or (< (:price %) 0) (< (:stock %) 0)) stock))
  )

(def merchant_stock
  (ref [(new_item :PEN 1.0 29 "ID7327328")
        (new_item :NOTEBOOK 10.0 31 "ID7327328")
        (new_item :BACKPACK 15.0 33 "ID7327328")
        (new_item :PEN 1.5 0 "ID2329862")
        (new_item :PENCIL 2 29 "ID2329862")
        (new_item :PAPER_CLIP 0.5 29 "ID2329862")
        ] :validator merchant_stock_validator))

(defn update-merchant-stock [current_items merchant item amount]
  (let [selected_item (first
                        (filter
                          #(and (= (:code %) item) (= (:merchant_id %) merchant))
                          current_items))]
    (update selected_item :stock #(- % amount))
    )
  )

(defn get_merchant_items [merchant]
  ((group-by :merchant_id @merchant_stock) merchant)
  )

(defn is_stock_not_empty_for_item [item_code items]
  (some #(and (= (:code %) item_code) (> (:stock %) 0)) items))

(defn get_item_price [current_merchant_items item]
  (:price (first (filter #(and (= (:code %) item) (> (:stock %) 0)) current_merchant_items)))
  )

(defn get-price-when-item-is-in-stock [merchant item]
  (let [current_merchant_items (get_merchant_items merchant)]
    (cond
      (nil? current_merchant_items) (do
                                      (swap! transaction_outcome assoc :outcome {:cause "No such merchant " :merchant merchant})
                                      -1)
      (is_stock_not_empty_for_item item current_merchant_items) (get_item_price current_merchant_items item)
      :default (do
                 (swap! transaction_outcome assoc :outcome {:cause "No availability" :item item :merchant merchant})
                 -1))
    )
  )

(defn buyer-balance-is-enough-for [item_price buyer]
  (let [buyer_bank_account (@bank buyer)]
    (cond
      (< item_price 0) false
      (not (nil? buyer_bank_account)) (> (:balance buyer_bank_account) item_price)
      :default false
      ))

  )

(defn checkout-buy [buyer merchant item]
  (let [item_price (get-price-when-item-is-in-stock merchant item)]
    (dosync
      (when (buyer-balance-is-enough-for item_price buyer)
        (do-transfer buyer merchant item_price)

        (log-checkout buyer item 1 item_price merchant)
        )
      (show_outcome)
      )
    )
  )
