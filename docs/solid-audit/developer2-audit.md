# SOLID Audit — Developer 2

Audited commit of `develop`: `<4154b03>`

## 1. Scope

This report audits the code developed by Developer 2 against the five SOLID principles (SRP, OCP, LSP, ISP and DIP), organized by module and requirement, according to the responsibility table of the assignment. In this project the persistence classes are named `*DAO` or `*Repository`; the audited classes are the ones that exist in `develop`.

| Module (requirement) | Classes | Methods audited |
|---|---|---|
| Persons (Workshop 1) | `Person`, `Customer`, `Seller`, `PersonDAO`, `PersonService` | `Person` constructor, getters and `toString`; `Customer` constructor, `getEmail`, `getPurchaseHistory`, `addPurchase`, `toString`; `Seller` constructor, getters and `toString`; `PersonDAO.saveCustomers`, `loadCustomers`, `saveSellers`, `loadSellers`; `PersonService` constructor, `registerCustomer`, `listCustomers`, `listSellers`, `findCustomerById`, `findSellerById` |
| Accessories (Requirement 1) | `AccessoryDAO`, `AccessoryService` | `AccessoryDAO.save`, `findAll`, `findById`, `update`, `writeAll`, `parseLine`, `applyCompatibleConsoles`; `AccessoryService.registerController`, `registerCable`, `registerMemory`, `registerCompatibility`, `listAllAccessories`, `listAccessoriesByType`, `findAccessoriesCompatibleWith`, `findById`, `checkStock`, `updateStock`, `restoreStock`, `matchesType` |
| Promotions (Requirement 2) | `PromotionDAO`, `PromotionService` | `PromotionDAO.saveAll`, `loadAll`, `parseLine`; `PromotionService.registerPercentageDiscount`, `registerCategoryDiscount`, `registerBulkPurchaseDiscount`, `listAllPromotions`, `listActivePromotions`, `findBestPromotionFor`, `findById` |
| Returns (Requirement 3) | `ReturnRepository`, `ReturnService` | `ReturnRepository.saveAll`, `loadAll`, `toLine`, `fromLine`, inner class `ReturnRecord`; `ReturnService.registerReturn` and its helper methods, `loadAllReturns`, `resolveReturn`, `viewAllReturns`, `viewReturnsByCustomer`, `viewReturnsBySale`, `calculateMonthlySales`, `calculateMonthlyReturns`, `generateMonthlyBalance` (monthly balance) |
| Warranties (Requirement 4) | `WarrantyRepository`, `WarrantyService` | `WarrantyRepository.saveAll`, `loadAll`, `toLine`, `fromLine`, inner class `WarrantyRecord`; `WarrantyService.assignBasicWarranty`, `assignExtendedWarranty`, `findWarrantyByProduct`, `listAllWarranties`, `listActiveWarranties`, `listWarrantiesExpiringSoon`, `cancelWarranties`, `loadAllWarranties`, `resolveWarranty`, `findSaleById`, `generateWarrantyId` |
| Exceptions and validation (Requirement 6) | Use of the exceptions and validators in the services and repositories listed above | Throwing of `InvalidDataException`, `BusinessRuleException`, `ResourceNotFoundException` and `PersistenceException`; calls to `PersonValidator`, `AccessoryValidator` and `PromotionValidator` |
| GRASP correction (Requirement 7) | Classes and methods created or modified by Developer 2 | `AccessoryValidator`, `PromotionValidator`, `ReturnRepository.ReturnRecord`, `Warranty.getTypeCode` and its implementations, the helper methods extracted from `ReturnService.registerReturn`, and the construction of `ReturnRepository` in `Main` |

### Out of scope for this report

Classes owned by other team members are not audited here: `Product` hierarchy, `Accessory` hierarchy, `Promotion` hierarchy and `Warranty` hierarchy (Developer 1); `Sale`, `SaleService`, the menus and `Main` (Technical Leader); and the `exceptions` and `validation` packages themselves (Developer 1). When a problem found while auditing Developer 2's code originates in one of those classes, it is reported to the Technical Leader for the consolidation instead of being audited here.

## 2. Single Responsibility Principle (SRP)

**Verdict: Violation** (4 findings: V-01 to V-04).

### V-01

| Field | Content |
|---|---|
| ID | V-01 |
| Principle | SRP |
| Module | Persons (Workshop 1) |
| Evidence | `PersonDAO`, constants at lines 22-26 (`CUSTOMERS_FILE = "data/customers.txt"` with separator `;`, and `SELLERS_FILE = "data/sellers.csv"` with separator `,`); methods `saveCustomers`/`loadCustomers` (lines 36-84) and `saveSellers`/`loadSellers` (lines 94-154). The seller methods also write and skip a header line (`id,name,phone,employeeCode,workShift`). |
| Explanation | One class owns two independent persistence concerns: the customers file and format, and the sellers file and format. Each one has its own reason to change. |
| Consequence | A change in the seller file format (for example a new column) forces editing the same class that persists customers, and any class that only needs sellers still depends on the customer persistence code. `SaleDAO` receives the whole `PersonDAO` to read both lists. |
| Proposed solution | Split `PersonDAO` into `CustomerDAO` (customers file) and `SellerDAO` (sellers file). `PersonService` would use both, and `SaleDAO` would receive the two DAOs. |
| Code of another member | Yes. `SaleDAO` (lines 30-41, 210 and 219) and `Main` (line 34, construction of `PersonDAO`) belong to the Technical Leader. |

### V-02

| Field | Content |
|---|---|
| ID | V-02 |
| Principle | SRP |
| Module | Returns (Requirement 3) |
| Evidence | `ReturnService.calculateMonthlySales` (lines 296-304), `calculateMonthlyReturns` (lines 306-314) and `generateMonthlyBalance` (lines 316-318). `calculateMonthlySales` iterates `saleService.listSales()` and sums `sale.getFinalTotal()`. |
| Explanation | `ReturnService` is the class that registers and consults returns, but it also computes a financial report that mostly aggregates sales. Reporting is a different responsibility from processing a return. |
| Consequence | A change in how the monthly balance is calculated (a new period, a new discount treatment, a different report format) forces editing the class that registers returns, which risks breaking the return flow. The class also depends on `SaleService` for reporting purposes only. |
| Proposed solution | Extract the three methods to a new `MonthlyBalanceService` in the service package that receives `SaleService` and `ReturnService`. `ReturnService` keeps only return processing. |
| Code of another member | Yes. `ReturnMenu` (lines 144-146) calls these methods and `Main` wires the services; both belong to the Technical Leader. |

### V-03

| Field | Content |
|---|---|
| ID | V-03 |
| Principle | SRP |
| Module | Returns (Requirement 3), GRASP correction (Requirement 7) |
| Evidence | `ReturnService.loadAllReturns` (lines 199-208) and `resolveReturn` (lines 215-241). `resolveReturn` looks up the sale with `saleService.findSaleById`, looks up each product in `productService` and then in `accessoryService`, and rebuilds the `Return`. |
| Explanation | Rebuilding domain objects from persisted records is a mapping responsibility. It lives in the same class as the return business rules (30-day window, product belongs to sale, warranty cancellation, stock restoration). |
| Consequence | A change in how a stored record is turned into a `Return` (for example a new column or a different lookup order) forces editing `ReturnService`, and a change in a business rule forces editing the same class that does the mapping. |
| Proposed solution | Extract `loadAllReturns` and `resolveReturn` to a `ReturnRecordResolver` class in the service package. `ReturnService` would delegate to it. |
| Code of another member | No. The new class is created and used only inside Developer 2's code (`Main` would only change if the resolver were injected, which is analyzed under DIP). |

### V-04

| Field | Content |
|---|---|
| ID | V-04 |
| Principle | SRP |
| Module | Warranties (Requirement 4) |
| Evidence | `WarrantyService.loadAllWarranties`, `resolveWarranty` and the private `findSaleById`. `findSaleById` loops over `saleDAO.findAll()` and compares ids, repeating what `SaleService.findSaleById` already does. |
| Explanation | `WarrantyService` mixes the warranty business rules (assign basic and extended warranties, list active, list expiring, cancel) with the mapping of stored records into `Warranty` objects and with a sale lookup. The lookup is duplicated because `WarrantyService` cannot depend on `SaleService` (circular dependency solved in Requirement 5). |
| Consequence | A change in the record format, in the type discriminator or in the sale lookup forces editing the class that owns the warranty rules. The duplicated lookup must be kept in sync with `SaleService.findSaleById`. |
| Proposed solution | Extract `loadAllWarranties`, `resolveWarranty` and `findSaleById` to a `WarrantyRecordResolver` class in the service package, and keep only the business rules in `WarrantyService`. |
| Code of another member | No. |

### Where the principle is met (correct)

- `Person`, `Customer` and `Seller` (model): each one only holds the data and the invariants of its own concept (`Customer.addPurchase`/`getPurchaseHistory` for the purchase history, `Seller` for employee data).
- `PersonService`: registration and lookup of people; the validation is delegated to `PersonValidator`.
- `AccessoryDAO` and `PromotionDAO`: their only reason to change is the storage format of one entity. `parseLine` is part of that format; the type `if/else` chains are analyzed under OCP.
- `AccessoryService` and `PromotionService`: each one manages one catalog (registration, queries and the rules of that catalog); validation is delegated to `AccessoryValidator` and `PromotionValidator`.
- `ReturnRepository`, `WarrantyRepository`, `ReturnRecord` and `WarrantyRecord`: the repositories only read and write raw records; the records only hold data.
- `AccessoryValidator` and `PromotionValidator`: each one only holds the validation rules of one domain.
- `ReturnService.registerReturn`: after the GRASP correction (V-28) it only orchestrates the steps through private helpers. The class-level findings are V-02 and V-03.

### Reported to the Technical Leader

- `SaleService` (lines 176 and 266-267) reads and updates accessory stock directly through `AccessoryDAO` (`accessory.reduceStock(quantity); accessoryDAO.update(accessory);`) instead of using `AccessoryService.updateStock`. The stock responsibility is therefore split between two services. It is reported for the consolidation.

## 3. Open/Closed Principle (OCP)

**Verdict: Violation** (4 findings: V-05 to V-08).

### V-05

| Field | Content |
|---|---|
| ID | V-05 |
| Principle | OCP |
| Module | Accessories (Workshop 3) |
| Evidence | `AccessoryDAO.parseLine`, lines 118-127: `if (type.equals("CONTROLLER")) {...} else if (type.equals("CABLE")) {...} else if (type.equals("MEMORY")) {...}`. |
| Explanation | The DAO must know every concrete accessory type to rebuild it from a CSV line. Writing is already polymorphic (`toCsvLine()` / `getTypeCode()`), but reading is not. |
| Consequence | Adding a new accessory type (for example a headset) forces modifying `AccessoryDAO.parseLine`, a class that was already tested. Forgetting the new branch makes the new accessories silently unreadable. |
| Proposed solution | Replace the `if/else` chain with a map from type code to a parser (`Map<String, Function<String[], Accessory>>`), registered in one place. A new type adds one entry and does not change `parseLine`. |
| Code of another member | No. The accessory classes belong to Developer 1 but they are not modified; only the DAO changes. |

### V-06

| Field | Content |
|---|---|
| ID | V-06 |
| Principle | OCP |
| Module | Promotions (Workshop 3) |
| Evidence | `PromotionDAO.parseLine`, lines 80-87: `if (type.equals("PERCENTAGE")) {...} else if (type.equals("CATEGORY")) {...} else if (type.equals("BULK")) {...}`. |
| Explanation | Same pattern as V-05: persistence of promotions is closed for writing (`toCsvLine()`) but open to modification for reading. |
| Consequence | A new promotion type forces editing `PromotionDAO.parseLine`. |
| Proposed solution | Same solution as V-05: a map from type code to parser. |
| Code of another member | No. |

### V-07

| Field | Content |
|---|---|
| ID | V-07 |
| Principle | OCP |
| Module | Promotions (Workshop 3), validation |
| Evidence | `PromotionValidator.validateTargetCategory`, lines 38-40: `targetCategory.equalsIgnoreCase("VIDEOGAME") \|\| ... "CONSOLE" \|\| ... "ACCESSORY"`. |
| Explanation | The list of valid categories is written inside the condition. The categories are product kinds that exist elsewhere in the system. |
| Consequence | A new product category forces editing this validator, and a mismatch with the real categories is not detected by the compiler. |
| Proposed solution | Keep the valid categories in one `Set<String>` constant (or an enum) and validate with `contains`. Adding a category changes one constant, not the logic. |
| Code of another member | No. |

### V-08

| Field | Content |
|---|---|
| ID | V-08 |
| Principle | OCP |
| Module | Warranties (Workshop 4) |
| Evidence | `WarrantyService.resolveWarranty`, line 245: `if (EXTENDED_TYPE.equals(record.getType()))`, with `EXTENDED_TYPE = "EXTENDED"` (line 29). Any other value is treated as a basic warranty. |
| Explanation | Rebuilding a warranty depends on a condition over the type string. A third warranty type would fall into the `else` branch and be rebuilt as a basic warranty. |
| Consequence | A new warranty type requires modifying `resolveWarranty`, and if it is forgotten the data is rebuilt incorrectly without any error. |
| Proposed solution | Use a map from type code to factory function, as in V-05, so each type is registered once. |
| Code of another member | No. |

### Where the principle is met (correct)

- Writing records: `Accessory.toCsvLine()`, `Promotion.toCsvLine()` and `Warranty.getTypeCode()` let the DAOs and repositories save any subtype without a type check.
- `AccessoryService.matchesType` uses `accessory.getTypeCode()`, so it does not need to change for a new accessory type.
- `ReturnService.cancelConsoleWarranties` uses `product.requiresWarranty()` instead of checking for `Console`.
- `PromotionService.findBestPromotionFor` works over the `Promotion` abstraction.

### Documented exception

- `ReturnService.restoreStockOf`, line 159: `if (product instanceof Accessory)`. It was kept on purpose in Requirement 7 because only accessories have stock; it is not counted as a violation.
