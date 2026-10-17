# Consolidated SOLID Audit — Team

**Audited state:** `develop` at `71539e1` (merge of PR #57). Its `src` tree is
identical to `develop` at `4154b03` (merge of PR #55), the commit audited by
Developer 1 and Developer 2. The Technical Leader audited `221b709`; the
`src` difference between `221b709` and `4154b03` is limited to two files of
Developer 2 (`AccessoryService`: a duplicated call removed and
`restoreStock` now validates through `AccessoryValidator`; `WarrantyService`:
`IllegalArgumentException` replaced by `InvalidDataException`). None of the
Leader's findings cites those lines, and the Leader's evidence was checked
again on `4154b03`. The evidence lines cited in the Developer 1 and
Developer 2 reports were spot-checked against `develop` (for example
`PersonService` line 30, `PromotionValidator` lines 38–40,
`AccessoryDAO.parseLine` line 118, `WarrantyService` line 246, `ProductDAO`
line 20, `Console.getCategory` lines 69–70) and match.

**Process note:** as in Requirement 7, the team did all Phase 1 work on the
single shared branch `feature/solid-audit`, merged through PR #56 and PR #57.
The consolidated audit is delivered in a separate branch
(`feature/solid-audit-consolidated`) and Pull Request, so Phase 1 closes with
three approved and merged Pull Requests. Each member made more than the
minimum of 6 atomic commits on the shared branch (9 for the Leader, 10 for
Developer 1 and 9 for Developer 2).

## Coverage Table

| Class / method | Auditor | Individual report |
|---|---|---|
| `Sale` (except `canBeReturned()`), `SaleDAO`, `SaleService`, `Main` | Technical Leader | `leader-audit.md` |
| `MainMenu`, `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu`, `MenuDialogs`, `SaleReceiptFormatter` | Technical Leader | `leader-audit.md` |
| `ProductService.restoreStock`, `ProductService.reduceStock` | Technical Leader | `leader-audit.md` |
| `Product`, `VideoGame`, `Console`, `ProductCategory`, `ProductRepository`, `ProductService` (except the two stock methods above) | Developer 1 | `developer1-audit.md` |
| `ProductDAO`, `ProductCsvMapper`, `VideoGameCsvMapper`, `ConsoleCsvMapper`, `DataLogger`, `ConsoleDataLogger` | Developer 1 | `developer1-audit.md` |
| `Accessory`, `Controller`, `Cable`, `Memory` | Developer 1 | `developer1-audit.md` |
| `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Developer 1 | `developer1-audit.md` |
| `Return`, `Sale.canBeReturned()` | Developer 1 | `developer1-audit.md` |
| `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Developer 1 | `developer1-audit.md` |
| `exceptions` package, `ProductValidator`, `PersonValidator`, `SaleValidator`, `AccessoryValidator`, `PromotionValidator` | Developer 1 | `developer1-audit.md` |
| `Person`, `Customer`, `Seller`, `PersonDAO`, `PersonService` | Developer 2 | `developer2-audit.md` |
| `AccessoryDAO`, `AccessoryService` | Developer 2 | `developer2-audit.md` |
| `PromotionDAO`, `PromotionService` | Developer 2 | `developer2-audit.md` |
| `ReturnRepository` (with `ReturnRecord`), `ReturnService` (including the monthly balance) | Developer 2 | `developer2-audit.md` |
| `WarrantyRepository` (with `WarrantyRecord`), `WarrantyService` | Developer 2 | `developer2-audit.md` |

**Verification:** every class in `src/main/java` appears in at least one
report. Three items appear in two reports because the responsibility table
assigns them to one member while the GRASP correction (Requirement 7) was
made by another: `AccessoryValidator` and `PromotionValidator` (validation
package of Developer 1, created by Developer 2) and `Warranty.getTypeCode`
(Developer 1's hierarchy, added by Developer 2). The only overlapping finding
is the one on `PromotionValidator.validateTargetCategory`, which both
reports registered; it is merged into a single violation (V-14). No class was
left uncovered.


## Final Team Verdict by Principle

| # | Principle | Team verdict | Violations |
|---|---|---|---|
| 1 | SRP | Violation | V-02, V-07, V-08, V-09, V-15, V-16, V-17, V-18 |
| 2 | OCP | Violation | V-04, V-06, V-10, V-11, V-14, V-19, V-20, V-21 |
| 3 | LSP | Violation | V-12, V-13 |
| 4 | ISP | Violation | V-22 |
| 5 | DIP | Violation | V-01, V-03, V-05, V-23, V-24, V-25, V-26 |

**Summary:** 26 definitive violations (8 SRP, 8 OCP, 2 LSP, 1 ISP, 7 DIP).
The three individual reports registered 27 findings (6 Leader, 8 Developer 1,
13 Developer 2); one fragment was registered twice and is merged (V-14).
Per member, the verdicts were: Leader (SRP, OCP, DIP violations; LSP correct;
ISP not applicable), Developer 1 (SRP, OCP, LSP violations; ISP and DIP
correct) and Developer 2 (SRP, OCP, ISP, DIP violations; LSP correct). The
team verdict is "Violation" whenever at least one member found a violation
with valid evidence. Every principle therefore has violations; LSP and ISP
are the ones with the fewest, and ISP appears only in the warranty/sale
boundary.

## Mapping from individual reports to definitive IDs

| Definitive ID | Source report and ID | Notes |
|---|---|---|
| V-01 | `leader-audit` V-01 | Also reported as boundary finding B-1 by Developer 1 |
| V-02 | `leader-audit` V-02 | |
| V-03 | `leader-audit` V-03 | Also reported by Developer 2 (SaleDAO concrete dependencies) |
| V-04 | `leader-audit` V-04 | Developer 2 also reported the direct use of `AccessoryDAO` for stock |
| V-05 | `leader-audit` V-05 | Also reported by Developer 2 |
| V-06 | `leader-audit` V-06 | |
| V-07 | `developer1-audit` V-01 | |
| V-08 | `developer1-audit` V-02 | |
| V-09 | `developer1-audit` V-03 | |
| V-10 | `developer1-audit` V-04 | |
| V-11 | `developer1-audit` V-05 | |
| V-12 | `developer1-audit` V-06 | |
| V-13 | `developer1-audit` V-07 | |
| V-14 | `developer1-audit` V-08 and `developer2-audit` V-07 | Same fragment, merged |
| V-15 | `developer2-audit` V-01 | |
| V-16 | `developer2-audit` V-02 | |
| V-17 | `developer2-audit` V-03 | |
| V-18 | `developer2-audit` V-04 | |
| V-19 | `developer2-audit` V-05 | |
| V-20 | `developer2-audit` V-06 | |
| V-21 | `developer2-audit` V-08 | |
| V-22 | `developer2-audit` V-09 | |
| V-23 | `developer2-audit` V-10 | |
| V-24 | `developer2-audit` V-11 | |
| V-25 | `developer2-audit` V-12 | |
| V-26 | `developer2-audit` V-13 | |


## Definitive Violation List

Each entry gives the definitive ID, the principle, the module, the evidence
(class, method and quoted fragment or lines on `develop`), the explanation
and consequence, the agreed solution, the source report and the correction
owner. The full evidence of each finding is in the source report. Where the
agreed solution differs from the one proposed in the source report, the
change is stated and justified in "Leader decisions at consolidation".

### Technical Leader's scope

**V-01 — DIP — `Sale.generateReceipt()` (model) depends on the UI layer**
- **Evidence:** `Sale.java` lines 256–258:
  `return com.gamezone.ui.SaleReceiptFormatter.format(this);` and the
  JavaDoc link at line 250.
- **Explanation and consequence:** a `model` class references a concrete
  `ui` class, against the direction `ui → service → persistence → model`; it
  was introduced by the Requirement 7 correction of GRASP V-01. `Sale`
  cannot be compiled, tested or reused without the `ui` package.
- **Agreed solution:** remove `generateReceipt()` from `Sale` and make
  `SaleMenu` (lines 115 and 161) call `SaleReceiptFormatter.format(sale)`.
  Team rule derived from this finding: no `model` class may reference `ui`
  (it also governs V-09).
- **Source:** `leader-audit` V-01; also reported as B-1 in `developer1-audit`.
- **Correction owner:** Technical Leader.

**V-02 — SRP — `SaleDAO` mixes file I/O, line serialization and reference resolution**
- **Evidence:** `SaleDAO` lines 53–95 (file access), 149–207 (`toLine`,
  `fromLine`, field layout), 209–225 (customer/seller lookup through
  `PersonDAO`), and `System.out.println` at line 184 inside `fromLine`.
- **Explanation and consequence:** three axes of change in one class; adding
  a field to a sale edits the same code that opens the file, and the format
  cannot be tested without the disk. `ProductDAO` already separates these
  concerns.
- **Agreed solution:** extract `SaleCsvMapper` (`toLine`/`fromLine`, with the
  reference resolution received as collaborators) and leave `SaleDAO` with
  reading and writing lines. File format and messages unchanged.
- **Source:** `leader-audit` V-02.
- **Correction owner:** Technical Leader.

**V-03 — DIP — `SaleDAO` depends on concrete `PersonDAO`/`ProductDAO`, and its clients on the concrete `SaleDAO`**
- **Evidence:** `SaleDAO` lines 30–31 and 40–43 (`PersonDAO`, `ProductDAO`);
  `SaleService` lines 33 and 48; `WarrantyService` lines 32 and 42;
  `Main` line 38.
- **Explanation and consequence:** services and persistence logic depend on
  file-based implementations; `SaleService` cannot be tested with an
  in-memory repository, and a storage change ripples into the service layer.
- **Agreed solution (changed from the source report):**
  (a) create `SaleReader` (`findAll`) and `SaleRepository extends SaleReader`
  (`save`, `findByCustomer`, `findBySeller`), implemented by `SaleDAO`, and
  make `SaleService` depend on `SaleRepository`; (b) `SaleDAO` (through the
  mapper of V-02) receives `ProductRepository` (already exists),
  `CustomerRepository` and `SellerRepository` (created in V-15) instead of
  `PersonDAO` and `ProductDAO`. The single `PersonRepository` interface
  proposed in the source report is dropped (see decisions). The change in
  `WarrantyService` is V-22.
- **Source:** `leader-audit` V-03; the same dependencies were reported by
  `developer2-audit` (DIP, "Reported to the Technical Leader").
- **Correction owner:** Technical Leader (part (b) after V-15).

**V-04 — OCP (related SRP) — product/accessory branching repeated in `SaleService`**
- **Evidence:** `registerSale` lines 101–103 (two maps by kind),
  `resolveSaleItems` lines 169–187, `buildSale` lines 198–211,
  `updateInventory` lines 258–268; the accessory stock rule at lines 178–182
  duplicates `ProductValidator.validateStockAvailability` (line 171).
- **Explanation and consequence:** a new sellable item kind forces edits in
  four methods, and stock rules for products and accessories are written
  twice. Developer 2 also reported that `SaleService` updates accessory stock
  through `AccessoryDAO` (lines 266–267) instead of `AccessoryService`.
- **Agreed solution:** a small `SaleItemSource` abstraction with two
  implementations: `ProductItemSource` (over `ProductService`) and
  `AccessoryItemSource` (over `AccessoryService`: `findById`,
  `AccessoryValidator.validateStockAvailability`, `updateStock`). `SaleService`
  iterates its sources in the current order (products first, accessories
  second), so item order, Spanish messages and `data/` files stay identical.
- **Source:** `leader-audit` V-04; absorbs the accessory-stock observation of
  `developer2-audit`.
- **Correction owner:** Technical Leader.

**V-05 — DIP — `SaleService` depends on the concrete `AccessoryDAO`**
- **Evidence:** `SaleService` line 35 and constructor lines 48–52; uses at
  lines 176 (`findById`) and 267 (`update`).
- **Explanation and consequence:** a service holds a concrete DAO; sales with
  accessories cannot be tested without the real file.
- **Agreed solution (changed from the source report):** `SaleService`
  stops using `AccessoryDAO` altogether and reaches accessories through
  `AccessoryService`, exactly as it already does for products through
  `ProductService` (GRASP V-34 precedent). No `AccessoryRepository` is
  needed for this violation; the interface of `AccessoryService` itself is
  V-24. The constructor of `SaleService` changes (`AccessoryDAO` replaced by
  `AccessoryService`) and `Main` is adjusted. It is delivered together with
  V-04.
- **Source:** `leader-audit` V-05; also reported by `developer2-audit`.
- **Correction owner:** Technical Leader.

**V-06 — OCP (related DIP) — a new menu area requires editing `MainMenu` in three places**
- **Evidence:** `MainMenu` constructor lines 49–60 (`new ProductMenu(...)` …
  `new WarrantyMenu(...)`, seven services received only to pass them on),
  `options` array lines 69–78, `switch` lines 93–101.
- **Explanation and consequence:** the label position and the `case` number
  are coupled by hand; the high-level class knows every concrete menu.
- **Agreed solution:** `AreaMenu` interface (`getTitle()`, `show()`)
  implemented by the seven menus, built in `Main`, with `MainMenu` receiving
  a `List<AreaMenu>`. Labels, order, exception handling and messages are
  unchanged. It is corrected last because it touches every menu class and
  `Main`.
- **Source:** `leader-audit` V-06.
- **Correction owner:** Technical Leader.

### Developer 1's scope

**V-07 — SRP — `ProductDAO` and the mappers split the knowledge of the CSV layout**
- **Evidence:** `ProductDAO.parseLine` reads the common columns itself
  (`Double.parseDouble(parts[3])`, `Integer.parseInt(parts[4])`) while
  `VideoGameCsvMapper.toCsvLine` and `ConsoleCsvMapper.toCsvLine` write them;
  the DAO also creates the folder and opens the file.
- **Explanation and consequence:** changing the common columns means editing
  the DAO and every mapper.
- **Agreed solution:** each `ProductCsvMapper` owns both directions
  (`toCsvLine` and `parse(String[])`); the DAO keeps only file reading and
  writing.
- **Source:** `developer1-audit` V-01.
- **Correction owner:** Developer 1.

**V-08 — SRP — domain classes know the persistence format**
- **Evidence:** `Accessory.toCsvLine()` with `Controller`, `Cable` and
  `Memory`; `Promotion.toCsvLine()` with the three discounts;
  `Warranty.getTypeCode()` ("the code used when it is stored on disk").
- **Explanation and consequence:** introduced in Requirement 7 to remove
  `instanceof` from the DAOs; products use a mapper instead, so the design is
  inconsistent, and a storage change modifies model classes.
- **Agreed solution:** `AccessoryCsvMapper`, `PromotionCsvMapper` and a
  warranty type mapper own the format in both directions, with a registry of
  type code to parser; the model classes lose the storage methods. It is
  delivered in the three steps of frontier finding FF-07 and also solves
  V-19, V-20 and V-21.
- **Source:** `developer1-audit` V-02.
- **Correction owner:** Developer 1 (Developer 2 switches the DAOs in FF-07).

**V-09 — SRP — `Return` and `Warranty` build presentation text**
- **Evidence:** `Return.generateReturnReceipt()` (`Return.java` line 105) and
  `Warranty.generateWarrantyCertificate()` (`Warranty.java` line 104) build
  Spanish text with `StringBuilder`.
- **Explanation and consequence:** domain rules and presentation in one
  class; a wording change edits the model.
- **Agreed solution:** `ReturnReceiptFormatter` and
  `WarrantyCertificateFormatter` in the `ui` package, next to
  `SaleReceiptFormatter`, with identical output; the model methods are
  removed in the same commit that updates their call sites in `ReturnMenu`
  (lines 84 and 129) and `WarrantyMenu` (lines 74 and 123). The rule of V-01
  applies: no `model` class references `ui`.
- **Source:** `developer1-audit` V-03 (and B-1, B-2).
- **Correction owner:** Developer 1 (call sites belong to the Leader, who
  reviews the PR).

**V-10 — OCP — `ProductService` needs a new method for every product type**
- **Evidence:** `ProductService.registerVideoGame(...)` and
  `registerConsole(...)` each build `new VideoGame(...)`/`new Console(...)`;
  the class imports both subtypes. Called from `ProductMenu` lines 79 and 109.
- **Explanation and consequence:** introduced in Requirement 7 (FF-02) to
  remove `new` from the menu; adding a type modifies a class that works.
- **Agreed solution:** a product factory abstraction with one implementation
  per type, received by `ProductService`; `ProductMenu` call sites are
  adapted in the same commit. The tension with the Creator pattern of
  GRASP V-05 is resolved by keeping `ProductService` as the creator through
  its factories, and it is justified in the refactor log.
- **Source:** `developer1-audit` V-04.
- **Correction owner:** Developer 1 (call sites belong to the Leader, who
  reviews the PR).

**V-11 — OCP — the list of supported product types is fixed inside `ProductDAO`**
- **Evidence:** `ProductDAO` line 20:
  `List.of(new VideoGameCsvMapper(), new ConsoleCsvMapper())`.
- **Explanation and consequence:** a new product type requires editing the
  DAO besides adding its mapper.
- **Agreed solution:** receive the mapper list in the constructor, keeping the
  no-argument constructor with the default list so `Main` still compiles.
- **Source:** `developer1-audit` V-05.
- **Correction owner:** Developer 1.

**V-12 — LSP — `Console.getCategory()` reports `VIDEOGAME`**
- **Evidence:** `Console.java` lines 69–70 return `ProductCategory.VIDEOGAME`
  (same value as `VideoGame`); `ProductDAO.writeAll` (lines 130–136) selects
  the mapper by `getCategory()`, so a console goes to `VideoGameCsvMapper`
  and `(VideoGame) product` throws `ClassCastException`.
- **Explanation and consequence:** a `Console` used as a `Product` breaks the
  contract of `getCategory()`; saving a console fails and a `CONSOLE`
  promotion never matches. The error was introduced by GRASP V-13
  (commit `1d908d5`).
- **Agreed solution:** return `ProductCategory.CONSOLE`. This is the first
  correction to be made. It is the only one that changes what the user
  observes (registering a console stops failing and restores the behavior
  before Requirement 7); it is recorded as a documented exception in the
  refactor log, and the baseline records the failure explicitly.
- **Source:** `developer1-audit` V-06.
- **Correction owner:** Developer 1.

**V-13 — LSP — the mappers narrow the precondition of `toCsvLine(Product)`**
- **Evidence:** `VideoGameCsvMapper.toCsvLine` starts with
  `VideoGame vg = (VideoGame) product;` and `ConsoleCsvMapper.toCsvLine` with
  `Console c = (Console) product;`, while the interface accepts any `Product`.
- **Explanation and consequence:** each implementation works for one subclass
  only; it is safe only because the DAO filters by category first (and V-12
  shows what happens when that filter is wrong).
- **Agreed solution:** make the mapper interface generic
  (`ProductCsvMapper<T extends Product>`) or let each mapper decide whether
  it supports the product before converting; Developer 1 chooses, keeping the
  CSV output identical.
- **Source:** `developer1-audit` V-07.
- **Correction owner:** Developer 1.


### Merged finding (Developer 1 and Developer 2)

**V-14 — OCP — `PromotionValidator.validateTargetCategory` repeats the list of valid categories**
- **Evidence:** `PromotionValidator.java` lines 38–40:
  `targetCategory.equalsIgnoreCase("VIDEOGAME") || ... "CONSOLE" || ... "ACCESSORY"`.
  The same knowledge exists in `ProductCategory.fromText`, which the
  constructor of `CategoryDiscount` already uses.
- **Explanation and consequence:** the valid categories are written in two
  places; adding a category (for example `BUNDLE`) requires editing the enum
  and this validator, or the new category is rejected when registering a
  promotion.
- **Agreed solution:** validate with `ProductCategory.fromText(targetCategory)`
  (the solution of Developer 1) instead of a new `Set` constant (the
  alternative of Developer 2), because it reuses the enum that already defines
  the categories. Before applying it, the owner checks that `fromText` accepts
  exactly the same inputs (the three values, case-insensitive) so the accepted
  and rejected values and the Spanish message stay identical.
- **Source:** `developer1-audit` V-08 and `developer2-audit` V-07 (same
  fragment, found independently by both).
- **Correction owner:** Developer 1 (owner of the validation package and of
  `ProductCategory`); Developer 2 reviews because he created the class in
  Requirement 7.

### Developer 2's scope

**V-15 — SRP — `PersonDAO` owns two independent persistence concerns**
- **Evidence:** `PersonDAO` lines 22–26 (`CUSTOMERS_FILE` with `;`,
  `SELLERS_FILE` with `,` and a header line) and the methods
  `saveCustomers`/`loadCustomers` (lines 36–84) and
  `saveSellers`/`loadSellers` (lines 94–154).
- **Explanation and consequence:** the customers file and the sellers file
  have independent reasons to change; a client that needs only sellers still
  depends on the customer code, and `SaleDAO` receives the whole `PersonDAO`.
- **Agreed solution:** split into `CustomerDAO` and `SellerDAO`, each one
  implementing a new interface (`CustomerRepository` with
  `saveCustomers`/`loadCustomers`, `SellerRepository` with
  `saveSellers`/`loadSellers`). File names, formats and header lines are
  unchanged. `PersonService` uses both (V-23), `SaleDAO` receives both
  (V-03), and `Main` builds them.
- **Source:** `developer2-audit` V-01.
- **Correction owner:** Developer 2 (prerequisite of V-03 part (b)).

**V-16 — SRP — `ReturnService` also computes the monthly financial balance**
- **Evidence:** `ReturnService.calculateMonthlySales` (line 296),
  `calculateMonthlyReturns` and `generateMonthlyBalance`
  (lines 296–318); the first one iterates `saleService.listSales()`.
- **Explanation and consequence:** reporting is a different responsibility
  from processing a return; a change in the balance edits the class that
  registers returns, and `ReturnService` depends on `SaleService` for
  reporting only. `ReturnMenu` also depends on the whole `ReturnService` to
  call these three methods.
- **Agreed solution:** extract the three methods to `MonthlyBalanceService`
  (receiving `SaleService` and `ReturnService`). `ReturnMenu` (lines 144–146)
  and `Main` are adapted in the same commit; output unchanged.
- **Source:** `developer2-audit` V-02.
- **Correction owner:** Developer 2 (call site belongs to the Leader, who
  reviews the PR).

**V-17 — SRP — `ReturnService` rebuilds domain objects from stored records**
- **Evidence:** `ReturnService.loadAllReturns` (lines 199–208) and
  `resolveReturn` (lines 215–241): look up the sale, then each product and
  accessory, and rebuild the `Return`.
- **Explanation and consequence:** a mapping responsibility lives next to the
  business rules (30-day window, stock restoration, warranty cancellation); a
  change in the stored record edits the same class as a business rule.
- **Agreed solution:** extract both methods to `ReturnRecordResolver`; the
  service delegates to it.
- **Source:** `developer2-audit` V-03.
- **Correction owner:** Developer 2.

**V-18 — SRP — `WarrantyService` mixes warranty rules, record mapping and a sale lookup**
- **Evidence:** `WarrantyService.loadAllWarranties`, `resolveWarranty`
  (line 246 onward) and the private `findSaleById` (loops over
  `saleDAO.findAll()`, line 253), which repeats `SaleService.findSaleById`.
- **Explanation and consequence:** a change in the record format, the type
  discriminator or the sale lookup edits the class that owns the warranty
  rules; the duplicated lookup must be kept in sync.
- **Agreed solution:** extract the three methods to `WarrantyRecordResolver`;
  its sale lookup uses the `SaleReader` abstraction of V-22.
- **Source:** `developer2-audit` V-04.
- **Correction owner:** Developer 2.

**V-19 — OCP — `AccessoryDAO.parseLine` branches on the accessory type**
- **Evidence:** `AccessoryDAO.java` lines 118–127:
  `if (type.equals("CONTROLLER")) ... else if ("CABLE") ... else if ("MEMORY")`.
- **Explanation and consequence:** writing is polymorphic but reading is not;
  a new accessory type (for example a headset) forces editing the DAO, and a
  forgotten branch makes the new accessories silently unreadable.
- **Agreed solution:** replace the chain with a registry of type code to
  parser, supplied by the accessory mapper created in V-08 (FF-07).
- **Source:** `developer2-audit` V-05.
- **Correction owner:** Developer 2.

**V-20 — OCP — `PromotionDAO.parseLine` branches on the promotion type**
- **Evidence:** `PromotionDAO.java` lines 80–87:
  `if (type.equals("PERCENTAGE")) ... "CATEGORY" ... "BULK"`.
- **Explanation and consequence:** same pattern as V-19; a new promotion type
  forces editing `parseLine`.
- **Agreed solution:** same registry as V-19, supplied by the promotion
  mapper of V-08 (FF-07).
- **Source:** `developer2-audit` V-06.
- **Correction owner:** Developer 2.

**V-21 — OCP — `WarrantyService.resolveWarranty` branches on a type string**
- **Evidence:** `WarrantyService.java` line 246:
  `if (EXTENDED_TYPE.equals(record.getType()))`, with every other value
  rebuilt as a basic warranty.
- **Explanation and consequence:** a third warranty type would be silently
  rebuilt as a basic one.
- **Agreed solution:** registry of type code to factory, supplied by the
  warranty type mapper of V-08 (FF-07); it ends up inside the resolver
  of V-18.
- **Source:** `developer2-audit` V-08.
- **Correction owner:** Developer 2.

**V-22 — ISP — `WarrantyService` depends on the whole `SaleDAO` to read one list**
- **Evidence:** `WarrantyService` constructor line 42
  (`WarrantyService(WarrantyRepository, SaleDAO, ProductService)`); the only
  use is the private `findSaleById`, which calls `saleDAO.findAll()` (line 253).
- **Explanation and consequence:** the service is forced to depend on methods
  it does not use and cannot be tested with a small fake.
- **Agreed solution:** `WarrantyService` receives the `SaleReader` interface
  (`findAll`) created by the Leader in V-03 part (a).
- **Source:** `developer2-audit` V-09.
- **Correction owner:** Developer 2 (after the Leader's V-03 part (a)).

**V-23 — DIP — `PersonService` creates its own concrete `PersonDAO`**
- **Evidence:** `PersonService.java` line 30:
  `this.personDAO = new PersonDAO();`; `Main` line 42 calls
  `new PersonService()`.
- **Explanation and consequence:** the service instantiates a low-level
  concrete class, cannot be tested with a fake and reads the real files when
  created.
- **Agreed solution (changed from the source report):** after V-15,
  `PersonService(CustomerRepository, SellerRepository)` receives the two
  interfaces by constructor; `Main` (line 42) passes them.
- **Source:** `developer2-audit` V-10.
- **Correction owner:** Developer 2 (after V-15).

**V-24 — DIP — `AccessoryService` depends on the concrete `AccessoryDAO`**
- **Evidence:** `AccessoryService.java` line 20:
  `AccessoryService(AccessoryDAO accessoryDAO)`; `Main` line 36 and 44.
- **Explanation and consequence:** the service is tied to the CSV
  implementation; `ProductService` already receives `ProductRepository`.
- **Agreed solution:** `AccessoryRepository` interface implemented by
  `AccessoryDAO`; the service depends on it. After V-05 no class of the
  Leader depends on `AccessoryDAO`.
- **Source:** `developer2-audit` V-11.
- **Correction owner:** Developer 2.

**V-25 — DIP — `PromotionService` depends on the concrete `PromotionDAO`**
- **Evidence:** `PromotionService.java` line 22:
  `PromotionService(PromotionDAO promotionDAO)`; `Main` lines 37 and 45.
- **Explanation and consequence:** same as V-24.
- **Agreed solution:** `PromotionRepository` interface implemented by
  `PromotionDAO`; the service depends on it.
- **Source:** `developer2-audit` V-12.
- **Correction owner:** Developer 2.

**V-26 — DIP — `ReturnService` and `WarrantyService` depend on concrete CSV classes**
- **Evidence:** `ReturnService.java` line 41 (`ReturnRepository`) and
  `WarrantyService.java` line 42 (`WarrantyRepository`, `SaleDAO`); both
  repository classes are concrete despite their names; `Main` lines 47–51.
- **Explanation and consequence:** neither service can be tested without real
  files, and the names suggest an abstraction that does not exist.
- **Agreed solution:** rename the concrete classes to `CsvReturnRepository`
  and `CsvWarrantyRepository`, create the interfaces `ReturnRepository` and
  `WarrantyRepository`, and use the `SaleReader` of V-22 for the sale lookup
  (the `SaleDAO` part is not counted twice). `Main` is the only
  other file affected.
- **Source:** `developer2-audit` V-13.
- **Correction owner:** Developer 2.


## Frontier Findings

A frontier finding is a violation whose correction needs a change, or a
stable contract, in code of more than one member. Each one names the
prerequisite (what must be on `develop` first), the dependent change and the
agreed split. Rule for call sites: the member who removes or changes a public
API updates its callers in the same commit, so the build never breaks, and the
owner of the caller reviews that Pull Request.

| ID | Violations | Prerequisite (owner) | Dependent change (owner) | Agreed split |
|---|---|---|---|---|
| FF-01 | V-15, V-23, V-03 | Developer 2 splits `PersonDAO` into `CustomerDAO` and `SellerDAO` with the interfaces `CustomerRepository` and `SellerRepository` (V-15), and makes `PersonService` receive them (V-23) | Leader: `SaleDAO`/`SaleCsvMapper` receive those interfaces instead of `PersonDAO` (V-03 part (b)); `Main` wiring (both) | Developer 2 first; the Leader applies V-03 part (b) after it is on `develop` |
| FF-02 | V-03, V-22, V-26, V-18 | Leader creates `SaleReader` (`findAll`) and `SaleRepository extends SaleReader`, implemented by `SaleDAO` (V-03 part (a)) | Developer 2: `WarrantyService` and the resolver/repositories receive `SaleReader` (V-22, V-18, V-26) | Leader first (small PR, interfaces only); Developer 2 applies V-22, V-18 and V-26 after it |
| FF-03 | V-04, V-05, V-24 | None: Developer 2 keeps the public contract of `AccessoryService` (`findById`, `updateStock`, same exceptions and Spanish messages) | Leader: `AccessoryItemSource` and `SaleService` use `AccessoryService` instead of `AccessoryDAO` | No change is requested to Developer 2's classes; if V-24 changes the constructor of `AccessoryService`, only `Main` is affected |
| FF-04 | V-01, V-09 | None (rule): no `model` class may reference `ui`; formatters live in `ui` | Leader removes `Sale.generateReceipt()` and updates `SaleMenu` (V-01); Developer 1 creates the two formatters, removes the model methods and updates `ReturnMenu` and `WarrantyMenu` in the same commit (V-09) | Independent of each other; the Leader reviews Developer 1's PR |
| FF-05 | V-10, V-16 | Developer 1 changes the registration API of `ProductService` (V-10); Developer 2 creates `MonthlyBalanceService` (V-16) | They update `ProductMenu` (V-10) and `ReturnMenu` and `Main` (V-16) in the same commit | The Leader reviews both PRs; V-06 is applied last, on top of the final menu code |
| FF-06 | V-08, V-19, V-20, V-21 | Developer 1 creates the three mappers with both directions and a registry (additive; the model methods are still in place) | Developer 2 switches `AccessoryDAO`, `PromotionDAO` and the warranty resolver to the registries (V-19, V-20, V-21); then Developer 1 removes the storage methods from the model (V-08) | Three steps in this order, each one a small PR; the build compiles after each |

**Frontier findings from the individual reports, all consolidated:**
B-1 and B-2 of `developer1-audit` are FF-04 and FF-05; the "Reported to the
Technical Leader" items of `developer2-audit` are FF-03 (accessory stock),
FF-01/FF-02 (SaleDAO and PersonDAO) and V-03/V-05; the "Code of another
member = Yes" fields of V-01, V-02, V-09, V-10, V-11, V-12 and V-13 of
`developer2-audit` are covered by FF-01, FF-05 and FF-02. No frontier finding
was dropped.