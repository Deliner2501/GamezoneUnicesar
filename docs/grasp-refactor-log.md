# GRASP Refactor Log — Phase 2

# GRASP Refactor Log — Phase 2

## No-Regression Verification Strategy

Defined by the Technical Leader before corrections began, per the team's
answer to orienting question 4 in `docs/grasp-analysis.md`. This
complements the per-member checklists (e.g.
`docs/grasp-refactor-verification-dev2.md`) with the full-system
checklist that is the Leader's responsibility to run as baseline and as
final verification.

1. **Baseline on `develop`.** Before any Phase 2 correction is merged,
   every menu operation in the system is exercised on `develop` (commit
   `9c86d20`, Phase 1 merged, no Phase 2 changes yet) and the result is
   recorded below as the reference.
2. **Re-check after every Phase 2 Pull Request.** `mvn clean compile`
   must pass, and the checklist rows for the operations touched by that
   PR are re-run and compared against the baseline: same inputs, same
   results, same messages.
3. **Data files are part of the behavior.** `data/*.txt` and `data/*.csv`
   are compared before and after each scenario; only the lines created by
   the test scenario itself may differ.
4. **Final full-system run.** Once every Phase 2 Pull Request is merged
   to `develop`, the Technical Leader re-runs this entire checklist one
   more time on `develop` and records the final column below as closing
   evidence for the oral defense.

## Baseline Checklist (run on `develop`, commit `9c86d20`, before Phase 2)

| # | Flow | Steps | Expected result | Baseline (pre-correction) | Final (post-correction, pending) |
|---|---|---|---|---|---|
| 1 | Register video game | Products > register a video game with valid data | Success dialog; appears in product listing | Pass | — |
| 2 | Register console | Products > register a console with valid data | Success dialog; appears in product listing | Pass | — |
| 3 | Register customer | Persons > register a customer with valid data | Success dialog; appears in customer listing | Pass | — |
| 4 | Register full sale | Sales > register a sale with one console, one video game, one accessory, best promotion auto-applied, extended warranty accepted for the console | Success dialog; receipt shows subtotal, warranty additional cost, discount, final total | Pass | — |
| 5 | List all sales | Sales > list all sales | Receipt-formatted list, same text as before (`Sale.generateReceipt`, now via `SaleReceiptFormatter`) | Pass | — |
| 6 | Sales by customer / seller | Sales > consult purchase history of the customer from step 4; consult sales of the seller | Same sale appears in both listings | Pass | — |
| 7 | Register accessory (controller) | Accessories > register a controller, mark it compatible with the console from step 2 | Success dialog; appears in accessory listing | Pass | — |
| 8 | List accessories by type / compatible with console | Accessories > list by type; list compatible with the console | Correct filtered lists | Pass | — |
| 9 | Register promotion (percentage) | Promotions > register a percentage discount | Success dialog; appears in promotion listing | Pass | — |
| 10 | List active promotions | Promotions > list active promotions | Only currently-active promotions shown | Pass | — |
| 11 | Register return | Returns > register a return for the sale from step 4, within the 30-day window | Success dialog; refund includes proportional discount and warranty reimbursement; stock restored for product and accessory; console's warranty canceled | Pass | — |
| 12 | View returns (all / by customer / by sale) | Returns > each listing option | Return from step 11 appears consistently in all three | Pass | — |
| 13 | Monthly balance | Returns > consult monthly balance for the current month | Shows total sales, total returns, net balance, consistent with steps 4 and 11 | Pass | — |
| 14 | Warranty queries | Warranties > consult by product/sale from step 4; list all; list active; list expiring soon | Warranty and extended warranty both appear, with correct status | Pass | — |
| 15 | Invalid-data paths | Try registering a product with a non-numeric price, a sale with an unknown customer id, a promotion with 150% | Each shows the corresponding error dialog; nothing is saved | Pass | — |
| 16 | Data file diff | `git diff -- data/` after the full scenario above | Only the lines created by this run are added; no existing line changed or disappeared | Pass | — |

**Build check (baseline):** `mvn clean compile` on `develop` at `9c86d20` → `BUILD SUCCESS`.

**Note:** the "Final" column stays as `—` until every Phase 2 Pull Request
(mine and both teammates') is merged to `develop`. I will re-run every
row then and fill it in as the closing evidence, per step 4 above.

---

| Class | Change made | Violation ID | Pattern applied | Commit/PR | Responsible |
|---|---|---|---|---|---|
| `Sale`, `SaleReceiptFormatter` (new) | Extracted `generateReceipt()`'s body into a new `SaleReceiptFormatter.format(Sale)` Pure Fabrication; `Sale.generateReceipt()` now delegates to it | V-01 | Pure Fabrication / Protected Variations | `refactor: extract receipt formatting to SaleReceiptFormatter (V-01)` | Technical Leader |
| `SaleDAO` | Wrapped `IOException` as `PersistenceException` in `findByCustomer`/`findBySeller`, consistent with `save`/`findAll` | V-02 | Protected Variations | `fix: wrap IOException as PersistenceException in SaleDAO findByCustomer/findBySeller (V-02)` | Technical Leader |
| `Product`, `Console` | Added `Product.requiresWarranty()` (default `false`) and overrode it in `Console` (`true`), so callers can ask the product instead of testing `instanceof Console`. Prerequisite for the Leader's and Developer 2's fixes | V-04, V-07, V-29 (FF-01) | Polymorphism | `refactor: add Product.requiresWarranty to replace instanceof Console checks (V-04, V-07, V-29)` | Developer 1 |
| `Customer` | `getPurchaseHistory()` now returns `Collections.unmodifiableList(purchaseHistory)` instead of the live internal list, so callers can only add purchases through `addPurchase` | V-15 | Protected Variations | `refactor: return unmodifiable purchase history from Customer (V-15)` | Developer 2 |
| `PersonDAO`, `PersonService`, `Main`, `MainMenu` | Removed the dead `throws IOException` from `PersonDAO`'s four methods, from `PersonService`'s constructor and `registerCustomer`; removed the now-unreachable `catch (IOException)` in `Main.main` and `MainMenu.registerCustomer` (change authorized by the Technical Leader per FF-04) | V-16 (FF-04) | Protected Variations | `refactor: remove dead throws IOException from PersonDAO, PersonService, Main and MainMenu (V-16)` | Developer 2 (Main and MainMenu edits coordinated with the Technical Leader) |
| WarrantyRepository, Warranty, BasicWarranty, ExtendedWarranty | Replaced the `instanceof ExtendedWarranty` check in `toLine` with a new polymorphic method `Warranty.getTypeCode()` (returns "BASIC" or "EXTENDED"); removed the type constants from the repository | V-31 | Polymorphism / Protected Variations | refactor commit 98f86d1 | Developer 2 |
| WarrantyRepository | `saveAll` and `loadAll` no longer declare `throws IOException`; the IOException is wrapped in `PersistenceException` with a message that names the file and the operation | V-32 | Protected Variations | refactor commit 98f86d1 | Developer 2 |
| WarrantyService | Replaced `IllegalArgumentException` with `InvalidDataException` in `assignBasicWarranty`, `assignExtendedWarranty` and `listWarrantiesExpiringSoon`; removed the `System.out.println` calls from `resolveWarranty`. Orphan records are still skipped silently (no behavior change: a single orphan warranty must not break the whole listing) | V-33 | Protected Variations / High Cohesion | refactor commit (V-33) | Developer 2 |
| `ProductService` | Added `registerVideoGame(...)` and `registerConsole(...)`, which build the domain object and delegate to `registerProduct`. Prerequisite for the Leader's V-05 fix in `MainMenu` | V-05 (FF-02) | Creator | `refactor: add registerVideoGame and registerConsole to ProductService (V-05)` | Developer 1 |
| `ProductCategory` (new), `Product`, `VideoGame`, `Console`, `Accessory` | Added the `ProductCategory` enum and the abstract `Product.getCategory()`, implemented per subtype | V-13 | Polymorphism | `refactor: add ProductCategory and Product.getCategory (V-13)` | Developer 1 |
| `CategoryDiscount` | Category validation and matching now use `ProductCategory` instead of `instanceof` and string literals | V-13 | Polymorphism | `refactor: use ProductCategory in CategoryDiscount instead of instanceof (V-13)` | Developer 1 |
| `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Added abstract `Promotion.toCsvLine()` implemented per subtype, with the same CSV format as before. Prerequisite for Developer 2's `PromotionDAO` fix | V-22 (FF-06) | Polymorphism | `refactor: add abstract Promotion.toCsvLine and implement it per subtype (V-22)` | Developer 1 |
| `Accessory`, `Controller`, `Cable`, `Memory` | Added abstract `getTypeCode()` and `toCsvLine()` implemented per subtype, with the same CSV format as before. Prerequisite for Developer 2's `AccessoryDAO` and `AccessoryService` fixes | V-17, V-19 (FF-05) | Polymorphism | `refactor: add Accessory.getTypeCode and toCsvLine, implement per subtype (V-17, V-19)` | Developer 1 |
| `Product`, `ProductService`, `ProductValidator` | The "enough stock" rule now lives in `Product.hasEnoughStock`; `reduceStock`, `checkStock` and `validateStockAvailability` delegate to it | V-08 | Information Expert | `refactor: centralize enough-stock rule in Product.hasEnoughStock (V-08)` | Developer 1 |
| `ProductDAO`, `DataLogger` (new), `ConsoleDataLogger` (new) | Replaced `System.out.println` with an injected logger that prints the same message; the catch now handles only parsing exceptions | V-11 | High Cohesion / Pure Fabrication | `refactor: inject logger and catch only parsing errors in ProductDAO.findAll (V-11)` | Developer 1 |
| `ProductDAO`, `ProductCsvMapper`, `VideoGameCsvMapper`, `ConsoleCsvMapper` (new) | Replaced the `instanceof` and type-code branching with one CSV mapper per product type | V-12 | Polymorphism / Pure Fabrication | `refactor: replace instanceof in ProductDAO with per-type CSV mappers (V-12)` | Developer 1 |
| `ProductRepository` (new), `ProductDAO`, `ProductService` | `ProductService` depends on the `ProductRepository` interface; the file path is received by the constructor with `data/products.csv` as default | V-10 | Low Coupling / Indirection | `refactor: introduce ProductRepository and constructor-injected file path (V-10)` | Developer 1 |
| AccessoryDAO | `findAll` and `writeAll` now wrap `IOException` in `PersistenceException` instead of printing to the console; corrupted lines are still skipped silently (no behavior change for valid data) | V-18 | Protected Variations | refactor commit <7ba3166> | Developer 2 |
| AccessoryService, AccessoryValidator (new) | Extracted the private `validateAccessory` rules (id and price checks) into a new `AccessoryValidator` in `com.gamezone.validation`, following the existing `ProductValidator` pattern, and added reusable checks for existence, positive quantity and stock availability | V-20 | High Cohesion / Pure Fabrication | refactor commit <ed86b8f> | Developer 2 |
| AccessoryService | Replaced every `IllegalArgumentException` with `InvalidDataException`, `ResourceNotFoundException` or `BusinessRuleException` through `AccessoryValidator`; removed the private `validateAccessory` method. Validation rules are unchanged (id not empty, price not negative, quantity positive, stock sufficient) | V-21 | Protected Variations | refactor commit <f1076d9> | Developer 2 |
| PromotionDAO | `saveAll` and `loadAll` now wrap `IOException` in `PersistenceException` instead of printing to the console; corrupted lines are still skipped silently (no behavior change for valid data) | V-23 | Protected Variations | refactor commit <5ba8b70> | Developer 2 |
| PromotionService, PromotionValidator (new) | Extracted the private `validatePercentage` and `validateTargetCategory` rules into a new `PromotionValidator` in `com.gamezone.validation`, following the existing `ProductValidator` pattern | V-24 | High Cohesion / Pure Fabrication | refactor commit <9fa1c5d> | Developer 2 |
| PromotionService | Replaced `IllegalArgumentException` with `InvalidDataException` (thrown through `PromotionValidator`) in the three `register...Discount` methods; removed the private validation methods. Validation rules are unchanged (percentage between 0 and 100, category VIDEOGAME/CONSOLE/ACCESSORY) | V-25 | Protected Variations | refactor commit <15c65fa> | Developer 2 |
| ReturnRepository, ReturnService, Main | `ReturnRepository` no longer depends on `SaleService`, `ProductService` or `AccessoryService`: it now loads raw `ReturnRecord` objects (ids, dates, amounts) and `ReturnService` resolves them into real `Return` objects, following the same pattern as `WarrantyRepository`/`WarrantyRecord`. `Main` now builds the repository with no arguments (change coordinated with the Technical Leader) | V-26 | Low Coupling / Indirection | refactor commit <65fc88d> | Developer 2 |
| ReturnRepository, ReturnService | Moved the resolution of sales and products out of the persistence layer and removed the `System.out.println` calls for skipped records; orphan returns are still skipped silently (no behavior change) | V-27 | High Cohesion / Protected Variations | refactor commit <65fc88d> | Developer 2 |
| ReturnService | Replaced every `IllegalArgumentException` in `registerReturn` with `InvalidDataException` (empty sale id, products or reason), `ResourceNotFoundException` (sale not found) and `BusinessRuleException` (30-day window, product not in the sale). Rules and messages are unchanged | V-30 | Protected Variations | refactor commit d7272e4 | Developer 2 |
| AccessoryDAO | `writeAll` now calls `Accessory.toCsvLine()` instead of an `instanceof` chain over Controller, Cable and Memory; the file format is unchanged. Uses the polymorphic method added by Developer 1 (FF-05) | V-17 | Polymorphism | refactor commit <fca12ea> | Developer 2 |
| AccessoryService | `matchesType` now compares `Accessory.getTypeCode()` with the requested type instead of using `instanceof`; accepted values and results are unchanged (FF-05) | V-19 | Polymorphism | refactor commit <73b4a5d> | Developer 2 |
| PromotionDAO | `saveAll` now calls `Promotion.toCsvLine()` instead of an `instanceof` chain over the three promotion types; the file format is unchanged. Uses the polymorphic method added by Developer 1 (FF-06) | V-22 | Polymorphism | refactor commit <95839d5> | Developer 2 |
| ReturnService | Split `registerReturn` into private helpers (`validateReturnRequest`, `findReturnableSale`, `resolveReturnedProducts`, `cancelConsoleWarranties`, `restoreStockOf`); the method now reads as a short sequence of steps. Order of operations and business rules are unchanged | V-28 | High Cohesion | refactor commit <3729f0a> | Developer 2 |
| ReturnService | `cancelConsoleWarranties` now asks `product.requiresWarranty()` instead of testing `instanceof Console`, using the method added by Developer 1 (FF-01). The `instanceof Accessory` check used to route stock restoration is kept as a documented exception | V-29 | Polymorphism | refactor commit <52c5354> | Developer 2 |
| AccessoryService, AccessoryDAO, ReturnService, ReturnRepository | Verified that no code in Developer 2's scope modifies the lists returned by `Accessory.getCompatibleConsoleIds()` and `Return.getReturnedProducts()`: the only usage is read-only (`ReturnRepository.toLine`). Developer 1 can make both lists unmodifiable without side effects (FF-07 check requested for V-14) | V-14 (FF-07) | Protected Variations | verification only, no code change | Developer 2 |
| `Accessory` | Fixed `getCategory()`, which returned `VIDEOGAME` by mistake; it now returns `ACCESSORY`. Correction of an error introduced in the V-13 commit, so the behavior matches the original `instanceof Accessory` check | V-13 (fix) | Polymorphism | `fix: return ACCESSORY category in Accessory.getCategory` | Developer 1 |
| `Accessory`, `Return` | `getCompatibleConsoleIds()` and `getReturnedProducts()` return unmodifiable views; the setter and the constructor copy the lists they receive. Developer 2 verified no call site mutates them (FF-07) | V-14 (FF-07) | Protected Variations | `refactor: return unmodifiable views and copy lists in Accessory and Return (V-14)` | Developer 1 |
| `Sale` | Added `getDiscountRate()` as the single source of the discount-rate formula. Prerequisite for Developer 1's `Return` fix | V-09 (FF-03) | Information Expert | `refactor: add Sale.getDiscountRate for Return to reuse (V-09)` | Technical Leader |
| `SaleService`, `ProductService`, `Main` | Added `ProductService.reduceStock`; `SaleService` now depends on `ProductService` instead of `ProductDAO` directly, resolving and updating stock through it. `SaleDAO`'s dependency on `ProductDAO` was left unchanged (persistence-to-persistence, consistent with the `ui→service→persistence→model` layering rule) | V-34 | Low Coupling | `refactor: route SaleService through ProductService instead of ProductDAO (V-34)` | Technical Leader |
| `SaleService` | Extracted `registerSale`'s 9 steps into private helper methods (`resolveCustomer`, `resolveSeller`, `resolveSaleItems`, `buildSale`, `applyBestPromotion`, `assignWarranties`, `updateInventory`); replaced `instanceof Console` with `product.requiresWarranty()` in the warranty step, using the method added by Developer 1 (FF-01) | V-03, V-04 | High Cohesion / Polymorphism | `refactor: extract registerSale into helper methods and use requiresWarranty (V-03, V-04)` | Technical Leader |
| `Return` | `calculateRefundAmount` and `generateReturnReceipt` now take the discount rate from `Sale.getDiscountRate()` through one private method, instead of recomputing it from two `Sale` fields twice. The zero-subtotal guard is kept so the behavior is identical | V-09 (FF-03) | Information Expert | `refactor: use Sale.getDiscountRate in Return instead of recomputing it (V-09)` | Developer 1 |
| `MainMenu` | Replaced direct `new VideoGame(...)`/`new Console(...)` instantiation in `registerVideoGame()`/`registerConsole()` with calls to `ProductService.registerVideoGame`/`registerConsole`, now that those factory methods exist (unblocked by Developer 1's FF-02 fix) | V-05 | Creator | `refactor: delegate product creation to ProductService factory methods (V-05)` | Technical Leader |
| `MainMenu` | Replaced `instanceof Console` with `product.requiresWarranty()` (with an explicit null check, since the product may be an accessory) in the sale-registration item loop | V-07 | Polymorphism | `refactor: replace instanceof Console with requiresWarranty in sale flow (V-07)` | Technical Leader |
| `MainMenu`, `MenuDialogs`, `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu` | Split the 1105-line MainMenu god class into one dedicated menu class per feature area, plus a `MenuDialogs` Pure Fabrication for the shared dialog helpers and error-message formatting. MainMenu itself now only holds the main loop and the top-level exception-to-message translation | V-06 | High Cohesion | `refactor: extract MenuDialogs...` through `refactor: clean up MainMenu...` (9 commits) | Technical Leader |