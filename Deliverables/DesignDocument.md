# Design Document 

Authors: Teresa Torresani, Riccardo Ossella, Aurora Anna Pia Sergio, Antonio Macaluso

Date: 30/04/21

Version: 1.3 (26/06/21)


# Contents

- [High level design](#package-diagram)
- [Low level design](#class-diagram)
- [Verification traceability matrix](#verification-traceability-matrix)
- [Verification sequence diagrams](#verification-sequence-diagrams)

# Instructions

The design must satisfy the Official Requirements document, notably functional and non functional requirements

# High level design

```plantuml
title: Package diagram
left to right direction
package EZShop {
}
package EZShopData {
}
package EZShopException {
}
package EZShopModel {
}
package EZShopGUI{
}
package CreditCardCircuit{

}
EZShop -- EZShopGUI
EZShop -- CreditCardCircuit
EZShopData <|-- EZShopException
EZShopData <|-- EZShopModel
EZShop <|- EZShopData

```

The logic of GUI and business have been divided in two different packages. 
We used a multilayer architectural pattern in combination with the MVC (Model, View, Controller).

# Low level design

```plantuml
title: Class diagram for EZShop Package
class EZShop{
  +map<OrderID, Order>
  +map<UserID, User>
  +map<ProductType, quantity>
  +map<ID, CustomerClass>
  +map<ID, LoyaltyCard>
  +map<RFID, Product>

  +createUser()
  +deleteUser()
  +getUser() 
  +getAllUsers()
  +updateUserRights() 

  +createProductType()
  +updateProduct()
  +deleteProductType()
  +getAllProductTypes() 
  +getProductTypeByBarCode() 
  +getProductTypesByDescription() 
  +updateLocation()
  +updateQuantity()

  +issueOrder()
  +payOrderFor()
  +payOrder()

  +recordOrderArrival()
  +getAllOrders()

  +startSaleTransaction()
  +addProductToSale()
  +deleteProductFromSale()
  +applyDiscountRateToProduct()
  +applyDiscountRateToSale()
  +computePointsForSale() 
  +endSaleTransaction() 
  +startReturnTransaction() 
  +returnProduct() 
  +endReturnTransaction() 

  +receiveCashPayment()
  +receiveCreditCardPayment()
  +returnCashPayment()
  +returnCreditCardPayment()

  +recordBalanceUpdate()
  +getCreditsAndDebits()
  +computeBalance()

  +login() 
  +logout()
  +reset()

  +defineCustomer()
  +modifyCustomer()
  +deleteCustomer() 
  +getCustomer() 
  +getAllCustomers()

  +createCard()
  +attachCardToCustomer() 
  +modifyPointsOnCard() 

  +updateQuantity()
  +deleteSaleTransaction()
  +getSaleTransaction()
  +deleteReturnTransaction() 
  
}
EZShop "1" - "*" User

class BalanceOperation {
 +description
 +amount
 +date
 +transactionType  // Credit or debit
}
EZShop "1" - "*" BalanceOperation

class Order

class User {
+userID
+username
+password
+role
}

class Product {
+RFID
+productCode
+status
}


EZShop "1" - "*" Order
SaleTransaction --|> BalanceOperation
ReturnTransaction --|> BalanceOperation
CreditCard - Customer


class ProductType{
    +productId
    +barCode
    +description
    +pricePerUnit
    +quantity
    +location <aisleNumber>-<rackAlphabeticIdentifier>-<levelNumber>
    +notes

}


EZShop "1" -- "*" Product

EZShop -- "*" ProductType

class TicketProduct {
    +productCode
    +description
    +price
    +amount
    +discount
    +list<RFID>
}

class SaleTransaction {
    +map<ProductType, quantity>

    +ID 
    +date
    +time
    +cost
    +paymentType
    +discount rate
    +isClosed // boolean

    +addProductToSale()
    +deleteProductFromSale()
    +applyDiscountRateToProduct()
    +modifyPointsOnCard()
}
SaleTransaction "0...*" - "*" TicketProduct

class LoyaltyCard {
    +ID
    +points
    +cardOwnerID
}
note bottom: cardOwnerID set to -1 if customer \n is not linked to any loyalty card yet


class Customer {
    +ID
    +points
    +name
    +customerCard
}


class CreditCard {
    
}

LoyaltyCard "0..1" - Customer

SaleTransaction "*" -- "0..1" LoyaltyCard

class Order {
  +ID
  +pricePerUnit
  +quantity
  +status
  +ProductCode
  +type
  +money
  +date
}

class ReturnTransaction {
  +quantity
  +returnedValue
  +returnProduct() 
}

ReturnTransaction "*" - SaleTransaction
ReturnTransaction "*" - ProductType
```







# Verification traceability matrix

|          | EZShop  | User  | AccountBook | Order |  ReturnTransaction | SaleTransaction | ProductType | Position | BalanceOperation | LoyaltyCard | Customer | Product |  CreditCard | 
|  ---     |  ---    |  ---  |    ---      |  ---  |    ---             |    ---          |    ---      |    ---   |    ---           |    ---      |    ---   |    ---  |      ---    | 
|   FR1    |   x     |  x    |             |       |                    |                 |             |          |                  |             |          |         |             |
|   FR3    |   x     |       |             |       |                    |                 |    x        |    x     |                  |             |          |    x    |             | 
|   FR4    |   x     |       |             | x     |                    |                 |    x        |          |                  |             |          |         |             | 
|   FR5    |   x     |       |             |       |                    |                 |             |          |                  |   x         |   x      |         |             | 
|   FR6    |   x     |       |   x         |       |        x           |      x          |    x        |          |                  |   x         |          |         |             | 
|   FR7    |   x     |       |             |       |                    |                 |             |          |                  |             |          |         |    x        | 
|   FR8    |   x     |       |   x         |       |                    |                 |             |          |     x            |             |          |         |             | 
                                                                                                                                                                 



# Verification sequence diagrams

```plantuml
title "Sequence Diagram SC 1.1"

participant "ShopManager" as a

box "EZ Shop" #white
participant "EZShop" as ez
participant "ProductType" as u
end box
autonumber

a -> ez : Inserts a new ProductType
a -> ez : Inserts description
a -> ez : Inserts productCode
a -> ez : Inserts pricePerUnit
a -> ez : Inserts notes
ez -> u : createProductType()
u --> ez : return ProductTypeID
ez --> a: successful message
   
```
```plantuml
title "Sequence Diagram SC 1.2"

participant "ShopManager" as a

box "EZ Shop" #white
participant "EZShop" as ez
participant "ProductType" as u
end box
autonumber

a -> ez : getProductTypeByBarCode()
ez -> u: getProductTypeID()
u --> ez : return ProductTypeID
a -> ez : Inserts new Position
ez -> u : updateLocation()
u --> ez: successful update
ez --> a: successful message
   
```

```plantuml
title "Sequence Diagram SC 2.1"

participant "Administrator" as a

participant "EZShop" as ez

autonumber

a -> ez : login()
a -> ez : inserts a new username
a -> ez : inserts password
a -> ez : inserts role
ez -> ez : createUser()
ez -> ez : record userID
ez --> a: successful message
```

Scenario 3.1 
```plantuml

title "Sequence Diagram SC 3.1"
autonumber
Manager -> EZShop : login()
Manager -> EZShop : select ProductCode
Manager -> EZShop : select quantity
Manager -> EZShop : select pricePerUnit

EZShop -> EZShop : issueOrder()
EZShop -> EZShop : record orderId
EZShop --> Manager : successful message

```

```plantuml

title "Sequence Diagram SC 3.2"
autonumber
Manager -> EZShop : login()
Manager -> EZShop : select Order
Manager -> EZShop : payOrder()
EZShop -> AccountBook : computeShopBalance()
AccountBook --> EZShop: return shopBalance  

EZShop -> Order : payOrder()
Order --> EZShop : return true
EZShop -> AccountBook : recordBalanceUpdate()
AccountBook --> EZShop: return true
EZShop --> Manager : successful message

```

```plantuml
title "Sequence Diagram SC 4.2"
autonumber
participant Customer order 10
participant Cashier order 20
box "EZ Shop" #white
participant EZShop order 30
participant CustomerClass order 50
participant LoyaltyCard order 40
end box
 
Customer -> Cashier: Provide name and surname
Cashier -> EZShop : Insert customer name and surname
EZShop -> CustomerClass: getCustomer()
CustomerClass --> EZShop : return Customer
Cashier-> EZShop : Creates a new Loyalty Card
EZShop -> LoyaltyCard: createCard()
LoyaltyCard --> EZShop: return loyaltyCard 
Cashier -> EZShop: Attach customer to loyalty card
EZShop -> LoyaltyCard: attachCardToCustomer()
LoyaltyCard --> EZShop: return updated LoyaltyCard
EZShop --> Cashier: Return success message
Cashier --> Customer: Give Loyalty Card to user
```

```plantuml

title "Sequence Diagram SC 5.1"
autonumber
User -> EZShop : insert username
User -> EZShop : insert password
User -> EZShop : login()
EZShop -> EZShop : check credentials
EZShop --> User : show User's GUI

```

```plantuml
title "Sequence Diagram SC 6.4"
participant "Customer " as cu
participant "Cashier" as c
box "EZ Shop" #white
participant "EZShop" as ez
participant "SaleTransaction " as s
participant "ProductType" as p
participant "AccountBook" as ab
autonumber
end box


cu -> c : Brings items to counter
c -> ez: Starts a new sale transaction
ez -> s: startSaleTransaction()
s --> ez: return transaction ID

group loop [for each product type]  
    c -> ez: Scans bar code
    ez -> ez: getProductTypeByBarCode()
    c -> ez: Adds Product Type number
    ez -> s: addProductToSale()
    s -> p: addProductToSale()
    s <-- p: sellPrice
end

p -> p: updateQuantity()

c -> ez: Closes sale transaction
ez -> s: endSaleTransaction()

s --> ez: amount
ez --> c: Shows sale transaction amount

c -> cu: Asks paymentType
cu --> c: credit card payment

cu -> c: Gives Loyalty Card
c -> ez: Reads Loyalty Card serial number
ez -> s: getLoyaltyCardID()
cu -> c: Gives Credit Card

c -> ez: Indicates credit card payment
ez -> ez: receiveCreditCardPayment()
note right: credit card payment management on UC7
ez --> c: Payment successful

s -> s: modifyPointsOnCard()

ez --> c: print sale receipt

ez->ab: recordBalanceUpdate()
ab->ez: return true
ez --> c: successful message
```

```plantuml
title "Sequence Diagram SC 7.1"

participant "Cashier" as u
box "EZ Shop" #white
participant "EZShop" as ez
participant "AccountBook" as ab
end box
participant "CreditCardCircuit " as cc 

autonumber 

u -> ez: Insert creditCardNumber
ez -> ez: check creditCardNumber
note right: with Luhn algorithm

ez -> cc: request (credit card number, amount)
cc -> ez: authorized
ez -> ez : receiveCreditCardPayment()
cc -> cc: update card balance
cc --> ez: successful update

ez --> u : Payment succesful

ez->ab: recordBalanceUpdate()
ab -> ez: return true
ez --> u : succesful message
```

```plantuml
title "Sequence Diagram SC 8.2"

participant "Cashier" as c
box "EZ Shop" #white
participant "EZShop" as ez
participant "AccountBook" as ab
participant "ReturnTransaction " as s
participant "ProductType" as p
end box
autonumber 

c -> ez: Inserts T.transactionId
ez -> ab: getSaleTransaction()
ab --> ez: return SaleTransaction 
ez -> s: startReturnTransaction() 

c -> ez: Scan bar code
ez -> ez: getProductTypeByBarCode()
c -> ez: Adds Product Type quantity
ez -> s: returnProduct()
s -> p: returnProduct()
p -> p: updateQuantity()
p --> s: sellPrice
s --> ez:returnedValue

ez->ez: ManageCashReturn
note right: on UC10
ez --> c: Return successful

c -> ez: Closes return transaction
ez -> s: endReturnTransaction()
ez -> ab: setSaleTransaction()
ez -> ab: recordBalanceUpdate()
ab --> ez: return successful
```

```plantuml
title "Sequence Diagram SC 9.1"
participant Manager order 10
box "EZ Shop" #white
participant EZShop order 30
participant AccountBook order 40
end box
autonumber
 
Manager -> EZShop: Select start date
Manager -> EZShop : Select end date
EZShop -> AccountBook: getTransactions()
AccountBook --> EZShop: return transactions
EZShop --> Manager: Show list of transactions
```

```plantuml

title "Sequence Diagram SC 10.1"
autonumber
Shop_Manager -> EZShop : insert returnId
ShopManager -> EZShop : insert creditCardNumber
ShopManager -> EZShop : returnCreditCardPayment()
EZShop -> EZShop : check creditCardNumber
EZShop -> CreditCardCircuit : update card balance
CreditCardCircuit --> EZShop : successful update
EZShop --> User : success message

```
