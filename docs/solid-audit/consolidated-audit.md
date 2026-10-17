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