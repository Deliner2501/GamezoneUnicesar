# Integration Analysis

This document describes each integration adjustment (A1–A7) applied while merging the accessory, promotion, warranty, and return modules into a single coherent system, following the order defined in Requirement 5.

## A1 - Category discount for accessories
**Branch:** `feature/accessory-category-discount` — **Type:** new functionality

**Cause:** `CategoryDiscount` (Requirement 2) only recognized `"VIDEOGAME"` and `"CONSOLE"` as valid target categories. Once the accessory module was integrated, the store had no way to run a promotion targeting accessories specifically.

**Solution applied:** `CategoryDiscount`'s constructor now also accepts `"ACCESSORY"` as a valid target category, and `calculateDiscount` recognizes any `Accessory` instance as belonging to that category via `instanceof`. `PromotionService.registerCategoryDiscount` validates the category against the same three allowed values, the sale submenu in `MainMenu` offers the accessory option when registering a category promotion, and `data/promotions.csv` now includes a category promotion targeting accessories, valid during the integration work period.

## A2 - Circular dependency in the warranty module
**Branch:** `fix/warranty-circular-dependency` — **Type:** correction

**Cause:** Requirement 4's specification created the cycle `SaleService → WarrantyService → WarrantyRepository → SaleService`, since the warranty repository needed to resolve sale references while loading. Java cannot construct three mutually dependent objects through constructor injection alone.

**Solution applied:** `WarrantyRepository` was simplified to persist and load only raw identifiers (product id, sale id, warranty type, start date) through an internal `WarrantyRecord` type, removing its dependency on `SaleService` entirely. `WarrantyService` now resolves those raw records back into real `Warranty` objects itself, receiving `WarrantyRepository`, `SaleDAO` (not `SaleService`, which avoids the cycle since `SaleDAO` has no dependency on any service), and `ProductService` through its constructor. `Main` was reordered accordingly, and `docs/warranty-class-diagram.md` was updated to reflect the new dependency direction.

## A3 - Unified sale registration flow
**Branch:** `refactor/unified-sale-registration` — **Type:** reorganization

**Cause:** Requirements 1, 2, and 4 each modified `SaleService.registerSale` independently. Once integrated, the order of operations inside the method determined the result — for example, whether the promotion discount was calculated before or after the warranty cost was known.

**Solution applied:** `registerSale` was reorganized into eight explicit, commented steps: (1) validate the sale has at least one item, (2) resolve every item as a product or accessory and validate its stock, (3) create the sale and add every item, calculating the subtotal, (4) find and apply the best promotion — calculated strictly over the items' subtotal, (5) generate the automatic basic warranty for every console plus any requested extended warranty, summing their cost, (6) register that cost on the sale, (7) update the inventory for every item, delegating to the DAO that owns its stock, and (8) persist the sale. While implementing this, a related bug was also fixed: `Sale.addAdditionalCost` was adding the warranty cost directly into the `total` field, which made the receipt's "Subtotal" line silently include the warranty charge. The fix keeps `total` as the pure product subtotal and moves the warranty cost into `getFinalTotal()`'s formula: `subtotal − discount + additionalCost`.

## A4 - Accessory stock on returns
**Branch:** `fix/return-accessory-stock` — **Type:** correction

**Cause:** Requirement 3's `ReturnService` only invoked `ProductService.restoreStock`, which has no knowledge of the accessory inventory. Returning an accessory therefore never restored its stock.

**Solution applied:** `AccessoryService` gained a `restoreStock(String accessoryId, int quantity)` method, symmetrical to `ProductService`'s. `ReturnRepository` now also receives `AccessoryService` through its constructor to resolve accessory references while loading returns. `ReturnService` receives `AccessoryService` as well, and `registerReturn` now checks whether each returned item is an `Accessory` and delegates the stock restoration to the matching service accordingly.

## A5 - Refund amount on discounted sales
**Branch:** `fix/return-discounted-refund` — **Type:** correction

**Cause:** `Return.calculateRefundAmount` summed the list price of every returned item. If the original sale had a promotion applied, the customer would be refunded more than they actually paid.

**Solution applied:** `calculateRefundAmount` now computes the original sale's discount rate (`discountAmount / subtotal`) and applies that same proportional reduction to each returned item's list price before summing them, so the refund can never exceed what was actually charged. `generateReturnReceipt` was updated to show, per item, its list price, its proportional discount, and the amount actually refunded.

## A6 - Monthly balance report breakdown
**Branch:** `fix/monthly-balance-report` — **Type:** correction

**Cause:** Requirement 3 required the monthly balance option to show the total sales, the total returns, and the net balance for the period, but `generateMonthlyBalance` only ever returned the net balance — the two components it depends on were never exposed separately. Additionally, once promotions and warranties were integrated, "total sales" needed to reflect each sale's actual final total, not just the sum of product prices.

**Solution applied:** `ReturnService` gained two new public methods, `calculateMonthlySales(int month, int year)` and `calculateMonthlyReturns(int month, int year)`, which `generateMonthlyBalance` now simply subtracts (its own signature is unchanged). `calculateMonthlySales` sums `Sale.getFinalTotal()` for that period, so discounts and warranty costs are already reflected. `MainMenu.consultMonthlyBalance` now calls and prints all three values instead of only the net balance.

## A7 - Warranty cancellation on console returns
**Branch:** `feature/return-warranty-cancellation` — **Type:** new functionality

**Cause:** None of the individual requirements defined what should happen to a console's warranty when that console is returned. In the integrated system, a returned console cannot keep an active warranty, and if it had an extended warranty, its cost should be reimbursed.

**Solution applied:** `WarrantyService` gained `cancelWarranties(String productId, String saleId)`, which removes every warranty matching that product and sale and returns the total reimbursable cost (zero for a basic warranty, the additional cost for an extended one). `ReturnService` now receives `WarrantyService` through its constructor, and `registerReturn` invokes `cancelWarranties` for every returned item that is a `Console`, adding the returned amount to the refund. `Return` gained a `warrantyRefund` field, incorporated both into `calculateRefundAmount` and into `generateReturnReceipt`, which now shows the amount reimbursed for canceled warranties as its own line.

**Process note:** this branch was started after A5 was merged, since both modify the refund calculation and starting from an unmerged base would have caused conflicts.