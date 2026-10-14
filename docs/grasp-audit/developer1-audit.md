# Developer 1 GRASP Audit - Ronald

## Scope

**Audited state.** Branch `feature/grasp-audit` at commit `a377cfb`. The `src` tree is identical to `develop` at `d669eed` (`git diff d669eed HEAD --stat -- src` returns no differences), so all the evidence below describes the current state of `develop`.

**Classes and methods audited**, by module and requirement (responsibility table, section 4 of the requirement):

| Module / requirement | Classes and methods |
|---|---|
| Products (base workshop) | `Product`, `VideoGame`, `Console`, `ProductDAO`, `ProductService` (every method except `restoreStock`, which the Technical Leader audits) |
| Accessories (Req. 1) | `Accessory`, `Controller`, `Cable`, `Memory` |
| Promotions (Req. 2) | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`, including adjustment A1 in `CategoryDiscount` |
| Returns (Req. 3) | `Return` (including adjustment A5) and `Sale.canBeReturned()` (both overloads) |
| Warranties (Req. 4) | `Warranty`, `BasicWarranty`, `ExtendedWarranty` |
| Exceptions layer (Req. 6) | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` |
| Validation layer (Req. 6) | `ProductValidator`, `PersonValidator`, `SaleValidator` |

Problems whose origin is in another member's code are not audited here. They are listed in the section "Boundary findings" and were reported to the Technical Leader.


<!-- COMMIT-BREAK -->

## 1. Information Expert - Verdict: Violation

Two violations were found (V-01 and V-02). The pattern is applied correctly in the places listed at the end of this section.

### V-01

- **ID:** V-01
- **Pattern:** Information Expert
- **Module:** Products (base workshop) and validation layer (Req. 6)
- **Evidence:** the rule "there is enough stock for a quantity" exists in three classes. `Product.reduceStock`, line 32, applies it inside the class that owns the stock: `if (quantity > this.stock) {`. The same rule is repeated outside the owner in `ProductService.checkStock`, line 57: `return product.getStock() >= quantity;` and in `ProductValidator.validateStockAvailability`, line 53: `if (product.getStock() < requestedQuantity) {`.
- **Explanation:** `Product` owns `stock` and already decides whether a quantity is available. `ProductService` and `ProductValidator` read the value through a getter and take the same decision again, instead of asking the product.
- **Consequence:** if the rule changes (for example, a minimum reserve stock), three places must be edited and they can diverge: the validator could accept a quantity that `reduceStock` later rejects with a different exception type.
- **Proposed solution:** add `Product.hasEnoughStock(int quantity)` as the single source of the rule, and make `reduceStock`, `checkStock` and `validateStockAvailability` delegate to it (Information Expert).
- **Code of another member:** No.

### V-02

- **ID:** V-02
- **Pattern:** Information Expert
- **Module:** Returns (Req. 3), adjustment A5
- **Evidence:** `Return.calculateRefundAmount` computes `double subtotal = originalSale.getTotal();` and then `discountRate = originalSale.getDiscountAmount() / subtotal;`. The same block is repeated in `Return.generateReturnReceipt`. The per-item formula is also written twice in different forms: `product.getPrice() * (1 - discountRate)` and `listPrice - proportionalDiscount`.
- **Explanation:** the discount rate is a fact about the sale, and `Sale` owns both the subtotal and the discount amount. `Return` pulls two fields out of `Sale` and does the arithmetic itself, twice.
- **Consequence:** if the definition of the discount changes (for example, if it is calculated over the subtotal plus warranties), `Return` must be edited in two places and any other class that needs the rate would copy the formula again.
- **Proposed solution:** add `Sale.getDiscountRate()` and use it from `Return`; extract a private helper in `Return` for the refund of a single item so the receipt and the total use the same formula.
- **Code of another member:** Yes, the Technical Leader (`Sale`). The change is an additive method.

### Where the pattern is correctly applied

- `Warranty.isActive` and `Promotion.isActive`: each class owns its dates and answers whether it is active.
- `Accessory.isCompatibleWith`: `Accessory` owns the compatibility list and answers the question.
- `Sale.canBeReturned` (lines 146-161): `Sale` owns the sale date and decides whether the 30-day window is still open.
- `Return.calculateRefundAmount` sums the prices of the products that the return itself holds.

## 2. Creator - Verdict: Correct

- `ProductDAO.parseLine` (lines 124 and 126) creates `VideoGame` and `Console` from a CSV record. The DAO holds the data needed to initialize them, which is the Creator criterion.
- The `Accessory` constructor creates the `ArrayList` for the compatibility list it contains.
- The validators create the exception that matches the problem at the exact point where the problem is detected.
- No other class in the audited scope instantiates domain objects. Instantiation in the user interface is reported in the section "Boundary findings" (B-2).