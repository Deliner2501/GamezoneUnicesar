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

## 4. Liskov Substitution Principle (LSP)

**Verdict: Correct** (no violations found).

### Analysis

| Hierarchy | Subtypes | Result |
|---|---|---|
| `Person` | `Customer`, `Seller` | Correct. Both subclasses only add data (`purchaseHistory`, `employeeCode`, `workShift`) and override `toString()` (`Customer` line 59, `Seller` line 47; `Person` line 68). They do not strengthen preconditions: the constructor validations of `Person` (lines 27-33, blank `name`, `id` and `phone` rejected) are inherited and not changed. They do not weaken postconditions or throw new exceptions in overridden methods. |
| `Customer.getPurchaseHistory()` | not applicable | It returns an unmodifiable list, so no caller can break the invariant of the subtype. |

### Evidence of substitutability

- `PersonService` and `PersonValidator` work with `Customer` and `Seller` through their `Person` fields (`getId()`, `getName()`, `getPhone()`) and never check the concrete class.
- The only `instanceof` in the project is `ReturnService.restoreStockOf` (line 159, `product instanceof Accessory`). It does not test a `Person` subtype and does not break substitution; it is the documented exception of Requirement 7.
- No subclass in this scope overrides a method with an empty body or an `UnsupportedOperationException`.

### Not applicable

- The `Product`, `Accessory`, `Promotion` and `Warranty` hierarchies and the exception hierarchy belong to Developer 1 (see Scope), so their LSP analysis is left to the corresponding audit.

### Reported to the Technical Leader

- None.

## 5. Interface Segregation Principle (ISP)

**Verdict: Violation** (1 finding: V-09).

### V-09

| Field | Content |
|---|---|
| ID | V-09 |
| Principle | ISP |
| Module | Warranties (Workshop 4) |
| Evidence | `WarrantyService` constructor, line 42: `WarrantyService(WarrantyRepository warrantyRepository, SaleDAO saleDAO, ProductService productService)`. The only use of `saleDAO` is the private `findSaleById` (around line 251), which calls `saleDAO.findAll()` and compares ids. |
| Explanation | `WarrantyService` depends on the whole `SaleDAO` (save, find, update, load of people through `PersonDAO`, etc.) but it only needs to read the list of sales. It is forced to depend on methods it does not use. |
| Consequence | Any change in the `SaleDAO` signature or behavior it does not use can still force recompiling or adapting `WarrantyService`, and the service cannot be tested with a small fake that only returns sales. |
| Proposed solution | Define a small interface `SaleReader` with a single method (for example `List<Sale> findAll()`), make `SaleDAO` implement it, and make `WarrantyService` receive `SaleReader`. |
| Code of another member | Yes. `SaleDAO` belongs to the Technical Leader (it would only add `implements SaleReader`), and `Main` wires `WarrantyService`. |

### Where the principle is met (correct)

- `ReturnService`, `AccessoryService`, `PromotionService` and `PersonService` depend on concrete classes, but each one uses all or almost all of the methods it receives, so no unused dependency is forced on them.
- There are no interfaces in the scope of Developer 2, so no fat interface exists. The existing interfaces (`ProductRepository`, `ProductCsvMapper`, `DataLogger`) belong to other members.
- The abstract classes of the model (`Person`, `Promotion`, `Warranty`) declare only methods that every subclass implements and uses (`calculateDiscount`, `toCsvLine`, `getTypeCode`, etc.), so no subclass has to implement an empty method.

### Related finding already reported

- `ReturnMenu` depends on the whole `ReturnService` only to call the monthly balance methods. This is the same problem as V-02 and is solved by extracting `MonthlyBalanceService`.

### Reported to the Technical Leader

- V-09 touches `SaleDAO` and `Main` (see the field "Code of another member").

## 6. Dependency Inversion Principle (DIP)

**Verdict: Violation** (4 findings: V-10 to V-13).

### V-10

| Field | Content |
|---|---|
| ID | V-10 |
| Principle | DIP |
| Module | Persons (Workshop 1) |
| Evidence | `PersonService` constructor, lines 29-30: `this.personDAO = new PersonDAO();`. The service creates its own concrete DAO and takes no parameter. `Main`, line 42, calls `new PersonService()`. |
| Explanation | A high-level class (the service) depends on and instantiates a low-level concrete class (the CSV DAO). The other services of the project already receive their dependencies in the constructor. |
| Consequence | `PersonService` cannot be tested with a fake or in-memory repository, and changing the storage means editing the service. It also reads the real files `data/customers.txt` and `data/sellers.csv` whenever it is created. |
| Proposed solution | Add a `PersonRepository` interface implemented by `PersonDAO`, and change the constructor to `PersonService(PersonRepository personRepository)`. |
| Code of another member | Yes. `Main` (line 42) must pass the repository to the constructor. |

### V-11

| Field | Content |
|---|---|
| ID | V-11 |
| Principle | DIP |
| Module | Accessories (Workshop 3) |
| Evidence | `AccessoryService` constructor, line 20: `AccessoryService(AccessoryDAO accessoryDAO)`. `Main`, line 36, creates `new AccessoryDAO()` and passes it. |
| Explanation | The service receives the dependency from outside (good) but depends on the concrete class and not on an abstraction. `ProductService` in the same project already receives the interface `ProductRepository`. |
| Consequence | The accessory service is tied to the CSV implementation and cannot be tested with a fake. |
| Proposed solution | Add an `AccessoryRepository` interface implemented by `AccessoryDAO`, and make `AccessoryService` depend on it. |
| Code of another member | Yes. `Main` (line 44) and `SaleService` (constructor line 48 receives `AccessoryDAO` directly) belong to the Technical Leader. |

### V-12

| Field | Content |
|---|---|
| ID | V-12 |
| Principle | DIP |
| Module | Promotions (Workshop 3) |
| Evidence | `PromotionService` constructor, line 22: `PromotionService(PromotionDAO promotionDAO)`. `Main`, line 37, creates `new PromotionDAO()`. |
| Explanation | Same pattern as V-11: injected, but concrete. |
| Consequence | The service cannot be replaced or tested without the real CSV file. |
| Proposed solution | Add a `PromotionRepository` interface implemented by `PromotionDAO`, and make `PromotionService` depend on it. |
| Code of another member | Yes. `Main` (line 45). |

### V-13

| Field | Content |
|---|---|
| ID | V-13 |
| Principle | DIP |
| Module | Returns (Requirement 3) and Warranties (Workshop 4) |
| Evidence | `ReturnService` constructor, line 41: `ReturnService(ReturnRepository returnRepository, ...)`; `WarrantyService` constructor, line 42: `WarrantyService(WarrantyRepository warrantyRepository, SaleDAO saleDAO, ...)`. `ReturnRepository` and `WarrantyRepository` are concrete classes (despite the name) and `SaleDAO` is concrete. `Main` (lines 47-51) creates all of them. |
| Explanation | The services depend on concrete CSV classes. The names `ReturnRepository` and `WarrantyRepository` suggest an abstraction that does not exist. |
| Consequence | Neither service can be tested without reading and writing real files, and changing the storage format forces changing the services. |
| Proposed solution | Rename the concrete classes to `CsvReturnRepository` and `CsvWarrantyRepository`, and create the interfaces `ReturnRepository` and `WarrantyRepository`. For the sale lookup, use the `SaleReader` interface proposed in V-09. |
| Code of another member | Yes. `Main` (lines 47-51) and `SaleDAO` (V-09). |

### Where the principle is met (correct)

- `ReturnService` receives `SaleService`, `ProductService`, `AccessoryService` and `WarrantyService` in the constructor instead of creating them.
- `WarrantyService` receives its repository and `ProductService` in the constructor, and the circular dependency with `SaleService` is solved with a setter (`setWarrantyService`) in Requirement 5.
- The services do not depend on the user interface, and the persistence classes do not depend on the services (raw records `ReturnRecord` and `WarrantyRecord`, Requirement 7).
- Validators are static utility classes with no state, so they do not hide infrastructure dependencies.

### Reported to the Technical Leader

- `SaleDAO` (line 40) receives the concrete `PersonDAO` and `ProductDAO`, and `SaleService` (line 48) receives the concrete `AccessoryDAO`. The Leader should consider the same abstractions.

## 7. Summary

| Principle | Verdict | Findings |
|---|---|---|
| SRP | Violation | V-01, V-02, V-03, V-04 |
| OCP | Violation | V-05, V-06, V-07, V-08 |
| LSP | Correct | None |
| ISP | Violation | V-09 |
| DIP | Violation | V-10, V-11, V-12, V-13 |

**Total: 13 violations.** Violations that touch code of another member (Technical Leader): V-01, V-02, V-09, V-10, V-11, V-12, V-13.
