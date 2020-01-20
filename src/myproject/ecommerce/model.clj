(ns myproject.ecommerce.model)

(defstruct merchant_item :code :price :stock :merchant_id)
(defn new_item [item_id price stock merchant_id] (struct merchant_item item_id price stock merchant_id))

(defstruct bank_account :client_id :balance)
(defn new_account [account_id balance] (struct bank_account account_id balance))

(defstruct buyer_checkout :client_id :acquired_item :amount :cost :sold_by)
(defn new_buyer_checkout [buyer item amount item_price merchant]
  (struct buyer_checkout buyer item amount item_price merchant))
