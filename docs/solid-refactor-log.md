# SOLID Refactor Log — Phase 2

## No-Regression Verification Strategy

Defined by the Technical Leader before any correction began, as described in
the answer to orienting question 4 in `docs/solid-analysis.md`. It
complements the per-member checks with the full-system checklist that the
Leader runs as baseline and as final verification.

1. **Baseline B0 on `develop`.** Before any Phase 2 correction is merged,
   every menu operation is exercised on `develop` (commit `cb242c5`, Phase 1
   merged, no Phase 2 change) and the observed result is recorded as the
   reference.
2. **Documented exception, reference B1.** The audit found that
   `Console.getCategory()` returns the wrong category (V-12), so saving a
   console fails with `ClassCastException`: the console flows cannot pass on
   `develop`, and B0 records that failure as it is. V-12 restores the
   behavior that existed before Requirement 7 and is the first correction.
   Once it is merged, the checklist is run again on `develop` and recorded as
   **B1**, the reference for every other correction. V-12 is the only
   expected difference between B0 and B1.
3. **One violation per commit.** Each correction is an atomic `refactor:`
   commit tagged with its violation ID, so a regression can be traced to one
   change and reverted alone. The project must compile (`mvn clean compile`)
   after each commit.
4. **Re-check on every Pull Request.** Before a Pull Request is opened, the
   rows of the checklist for the operations the correction touches are run
   again and compared with B1: same inputs, same results, same messages shown
   to the user. The evidence is added to the Pull Request description.
5. **Data files are part of the behavior.** Each scenario is run on a copy of
   the `data/` folder and the result is compared with the baseline (for
   example `git diff -- data/`); only lines created by the scenario itself
   may differ, and existing lines must be unchanged.
6. **Persistence flows are re-run after a restart.** Corrections that touch
   persistence (V-02, V-03, V-07, V-08, V-15, V-19 to V-21, V-26) are checked
   by closing the application and opening it again: the listings must show the
   same data.
7. **Out-of-scope defects stay untouched.** The defects recorded in the
   Requirement 7 and Requirement 8 consolidated audits are not fixed, even
   when a correction touches the same code, so "the same behavior" stays well
   defined.
8. **No new violations.** A correction cannot introduce a new SOLID violation
   nor bring back a corrected GRASP violation. If a principle or pattern has
   to be sacrificed for another, the decision is justified in this log.
9. **Final full-system run.** When every Phase 2 Pull Request is merged, the
   Technical Leader runs this whole checklist once more on `develop` and
   records the final column as the closing evidence for the oral defense.

## Verification Checklist

Run on a fresh copy of the `data/` folder. Result values: `Pass`, `Fail`
(with the observed message in the notes) or `n/a`.

| # | Flow | Steps | Expected result | B0 (`develop` `cb242c5`) | B1 (after V-12) | Final |
|---|---|---|---|---|---|---|
| 1 | Main menu navigation | Open each of the seven areas and return; choose "Salir" | Every area opens, "Volver" returns to the main menu, "Salir" shows the farewell dialog | Not run | | |
| 2 | Register video game | Products > register a video game with valid data | Success dialog; appears in the product listing | Pass (service level) | | |
| 3 | Register console | Products > register a console with valid data | Success dialog; appears in the product listing. **B0 is expected to fail (V-12)** | **Fail** (V-12): `ClassCastException`, `Console` cannot be cast to `VideoGame` | | |
| 4 | List customers and sellers | People > list customers; list sellers | Correct lists, same text as before | Not run | | |
| 5 | Register customer | People > register a customer with valid data | Success dialog; appears in the customer listing | Pass (service level) | | |
| 6 | Register sale without console | Sales > register a sale with one video game and one accessory | Success dialog; receipt shows subtotal, discount (if any) and final total; stock of both items reduced | Pass (service level): final total 212500 with "Descuento de Septiembre" | | |
| 7 | Register full sale | Sales > register a sale with a console, a video game and an accessory, best promotion applied, extended warranty accepted for the console | Receipt shows subtotal, warranty additional cost, discount and final total. **B0 is expected to fail (V-12)** | **Fail** (V-12): no console can be registered | | |
| 8 | List all sales | Sales > list all sales | Receipt-formatted list, same text as before | Pass (service level); the reloaded sale lacks the accessory (known defect F-01 of Requirement 7) | | |
| 9 | Sales by customer / by seller | Sales > purchase history of a customer; sales attended by a seller | The sale of step 6 or 7 appears in both | Pass (service level) for the customer query; seller query not run | | |
| 10 | Register accessory | Accessories > register a controller and mark it compatible with a console | Success dialog; appears in the accessory listing | Pass (service level) for the registration; compatibility not run | | |
| 11 | List accessories | Accessories > list by type; list those compatible with a console | Correct filtered lists | Not run | | |
| 12 | Register promotions | Promotions > register a percentage, a category (`VIDEOGAME`, `CONSOLE`, `ACCESSORY`) and a bulk discount | Success dialog each; appear in the listing | Pass (service level) for the `CONSOLE` category promotion; the others not run | | |
| 13 | Category validation | Promotions > register a category promotion with an invalid category and a percentage promotion with 150% | Each shows the corresponding error dialog; nothing is saved | Pass (service level): both rejected with `InvalidDataException` ("debe ser VIDEOGAME, CONSOLE o ACCESSORY" and "debe estar entre 0 y 100") | | |
| 14 | List active promotions | Promotions > list active promotions | Only active promotions shown | Not run | | |
| 15 | Register return | Returns > register a return for the sale of step 6 or 7 inside the 30-day window | Receipt with the proportional discount refund; stock restored; warranty of a console canceled | Pass (service level): refund 12500 (accessory missing after reload, F-01 of Requirement 7); console warranty n/a | | |
| 16 | View returns | Returns > all returns; by customer; by sale | The return of step 15 appears in all three | Not run | | |
| 17 | Monthly balance | Returns > monthly balance for the current month | Total sales, total returns and net balance consistent with steps 6/7 and 15 | Pass (service level): sales 12500, returns 12500 (same F-01 effect) | | |
| 18 | Warranty queries | Warranties > by product; by sale; list all; list active; list expiring soon | Basic and extended warranty appear with the correct status | n/a: warranties exist only for consoles and none can be registered (V-12) | | |
| 19 | Invalid-data paths | Register a product with a non-numeric price; a sale with an unknown customer id; a sale with an unknown item id | Each shows its error dialog; nothing is saved | Pass (service level) for the unknown customer (`ResourceNotFoundException`); the other two not run | | |
| 20 | Restart persistence | Close the application and open it again; repeat the listings of products, accessories, promotions, sales, returns and warranties | The same data as before the restart | Not run | | |
| 21 | Data file diff | `git diff -- data/` after the whole scenario | Only the lines created by this run are added; no existing line changed or disappeared | Not run | | |

**How B0 was obtained.** The rows marked "service level" were run by calling the
services and DAOs of `develop` (`cb242c5`) from a program on a copy of
`data/`, without the Swing menus. The menu-level run (dialogs, navigation and
restart) is pending and must be completed and recorded here before the
first Phase 2 Pull Request is merged. Rows marked "Not run" have no B0 result yet.

**Build check (B0):** all sources of `develop` at `cb242c5` compile with `javac`
(verified without Maven). `mvn clean compile`: BUILD SUCCESS.

**Build check (final):** `mvn clean compile` on `develop` after the last Phase 2
Pull Request: _pending_.

## Corrections by Technical Leader

| Class | Change made | Violation ID | Principle applied | Commit/PR | Responsible |
|---|---|---|---|---|---|
| `Sale`, `SaleMenu` | Removed `Sale.generateReceipt()`, which pointed the model at `ui.SaleReceiptFormatter`; `SaleMenu` now calls `SaleReceiptFormatter.format(sale)` directly. The receipt text is unchanged | V-01 | DIP | refactor: remove model dependency on ui by dropping Sale.generateReceipt (V-01) | Technical Leader |
| `SaleDAO`, `SaleCsvMapper` (new) | Moved `toLine`, `fromLine` and the customer/seller lookups into the new `SaleCsvMapper`; `SaleDAO` keeps only the file reading and writing and delegates the line format to the mapper. File format and messages are unchanged | V-02 | SRP | refactor: extract SaleCsvMapper so SaleDAO only handles file access (V-02) | Technical Leader |
| `SaleReader` (new), `SaleRepository` (new), `SaleDAO`, `SaleService` | Added the read-only `SaleReader` and the full `SaleRepository` interfaces; `SaleDAO` implements them and `SaleService` now depends on `SaleRepository` instead of the concrete `SaleDAO`. Part (a) of V-03; part (b), the person repositories, comes after Developer 2's V-15 | V-03 | DIP | refactor: add SaleReader and SaleRepository so SaleService depends on abstractions (V-03) | Technical Leader |
| `SaleService`, `SaleItemSource` (new), `ProductItemSource` (new), `AccessoryItemSource` (new) | Replaced the product/accessory branching in `resolveSaleItems`, `buildSale` and `updateInventory` with a list of `SaleItemSource`; each kind of item owns its lookup, stock check and stock reduction. Order of items, Spanish messages and data files are unchanged | V-04 | OCP | refactor: add SaleItemSource and use AccessoryService in SaleService (V-04, V-05) | Technical Leader |
| `SaleService`, `Main` | `SaleService` no longer uses `AccessoryDAO`: it receives `AccessoryService` (constructor changed, `Main` adjusted) and reaches accessory stock through `AccessoryItemSource` | V-05 | DIP | refactor: add SaleItemSource and use AccessoryService in SaleService (V-04, V-05) | Technical Leader |

## Corrections by Developer 1

| Class | Change made | Violation ID | Principle applied | Commit/PR | Responsible |
|---|---|---|---|---|---|
| `Console` | `getCategory()` now returns `ProductCategory.CONSOLE` instead of `VIDEOGAME`. Documented exception: it restores console saving, which failed since GRASP V-13; every other behavior is unchanged | V-12 | LSP | `refactor: return CONSOLE category in Console.getCategory (V-12)` | Developer 1 |
| `PromotionValidator` | `validateTargetCategory` validates with `ProductCategory.fromText`, same accepted values and same Spanish message | V-14 | OCP | `refactor: validate target category with ProductCategory.fromText (V-14)` | Developer 1 |
| `ProductDAO` | The mapper list is received in a new constructor; the existing constructors keep the default list | V-11 | OCP | `refactor: receive product mappers in ProductDAO constructor (V-11)` | Developer 1 |
| `ProductCsvMapper`, `VideoGameCsvMapper`, `ConsoleCsvMapper` | Added `supports(Product)`; `toCsvLine` checks it before converting, so the mapper no longer assumes a precondition the interface does not state | V-13 | LSP | `refactor: let product mappers check support before converting (V-13)` | Developer 1 |
| `ProductService`, `ProductFactory`, `VideoGameFactory`, `ConsoleFactory` (new), `ProductMenu`, `Main` | `registerVideoGame` and `registerConsole` replaced by `registerNewProduct`, which uses one factory per category received by the constructor; call sites adapted in the same commit. `ProductService` remains the creator through its factories (tension with GRASP V-05 resolved as agreed) | V-10 | OCP | `refactor: register products through per-type factories in ProductService (V-10)` | Developer 1 |
| `Return`, `Warranty`, `ReturnReceiptFormatter`, `WarrantyCertificateFormatter` (new), `ReturnMenu`, `WarrantyMenu` | Moved the receipt and certificate text to formatters in `ui` with identical output; model methods removed and call sites adapted in the same commit. `Return.getOriginalDiscountRate()` is now public so the discount-rate rule stays in `Return` | V-09 | SRP | `refactor: move return and warranty text generation to ui formatters (V-09)` | Developer 1 |
| `AccessoryCsvMapper`, `PromotionCsvMapper`, `WarrantyTypeMapper` (new) | Step 1 of V-08: mappers that own the storage format in both directions, with type-code registries; additive, nothing uses them yet | V-08 (step 1) | SRP | `refactor: add accessory, promotion and warranty CSV mappers with type registries (V-08)` | Developer 1 |

## Corrections by Developer 2

| Class | Change made | Violation ID | Principle applied | Commit/PR | Responsible |
|---|---|---|---|---|---|
| `AccessoryRepository` (new), `AccessoryDAO`, `AccessoryService` | Added the `AccessoryRepository` interface (`save`, `findAll`, `findById`, `update`); `AccessoryDAO` implements it and `AccessoryService` now receives the interface instead of the concrete DAO. `Main` is unchanged because it already passes an `AccessoryDAO`. Behavior, messages and `accessories.csv` are unchanged | V-24 | DIP | refactor: make AccessoryService depend on AccessoryRepository (V-24) | Developer 2 |
| `PromotionRepository` (new), `PromotionDAO`, `PromotionService` | Added the `PromotionRepository` interface (`saveAll`, `loadAll`); `PromotionDAO` implements it and `PromotionService` now receives the interface instead of the concrete DAO. `Main` is unchanged because it already passes a `PromotionDAO`. Behavior, messages and `promotions.csv` are unchanged | V-25 | DIP | refactor: make PromotionService depend on PromotionRepository (V-25) | Developer 2 |
| `ReturnRecordResolver` (new), `ReturnService`, `Main` | Extracted `loadAllReturns` and `resolveReturn` to the new `ReturnRecordResolver`, which rebuilds `Return` objects from the stored records. `ReturnService` receives the resolver in its constructor and delegates to it; `Main` creates the resolver and passes it. The lookup logic is unchanged, so listings, messages and `returns.csv` stay the same | V-17 | SRP | refactor: extract ReturnRecordResolver from ReturnService (V-17) | Developer 2 |
| `WarrantyRecordResolver` (new), `WarrantyService`, `Main` | Extracted `loadAllWarranties`, `resolveWarranty` and the private `findSaleById` to the new `WarrantyRecordResolver`, which rebuilds `Warranty` objects from the stored records. `WarrantyService` receives the resolver in its constructor and delegates to it; `Main` creates the resolver and passes it. The orphan-record skips and the type rule are unchanged, so warranty listings, messages and `warranties.csv` stay the same | V-18 | SRP | refactor: extract WarrantyRecordResolver and depend on SaleReader (V-18, V-22) | Developer 2 |
| `WarrantyRecordResolver`, `WarrantyService` | The sale lookup now goes through the read-only `SaleReader` interface (`findAll`) instead of the concrete `SaleDAO`, and `WarrantyService` no longer depends on `SaleDAO` at all. Same result as before; only the dependency type changed | V-22 | ISP | refactor: extract WarrantyRecordResolver and depend on SaleReader (V-18, V-22) | Developer 2 |
| `CustomerRepository`, `SellerRepository`, `CustomerDAO`, `SellerDAO` (new), `PersonDAO` (removed), `SaleDAO`, `SaleCsvMapper`, `Main` | Split `PersonDAO` into `CustomerDAO` and `SellerDAO`, each one implementing its own interface (`CustomerRepository`, `SellerRepository`). The file names, formats and the sellers header line are unchanged. `SaleDAO` and `SaleCsvMapper` now receive the two interfaces instead of `PersonDAO`, and `Main` builds the two DAOs. Behavior, messages, `customers.txt` and `sellers.csv` are unchanged | V-15 | SRP | refactor: split PersonDAO into CustomerDAO and SellerDAO behind repository interfaces (V-15, V-23) | Developer 2 |
| `PersonService`, `Main` | `PersonService` no longer creates its own DAO: it receives `CustomerRepository` and `SellerRepository` in its constructor, and `Main` passes them. It still loads the customers and sellers at start-up and saves customers the same way | V-23 | DIP | refactor: split PersonDAO into CustomerDAO and SellerDAO behind repository interfaces (V-15, V-23) | Developer 2 |
| `ReturnRepository`, `WarrantyRepository` (now interfaces), `CsvReturnRepository`, `CsvWarrantyRepository` (new), `Main` | Turned `ReturnRepository` and `WarrantyRepository` into interfaces (`saveAll`, `loadAll`) that also hold the raw records `ReturnRecord` and `WarrantyRecord`. The CSV code moved to `CsvReturnRepository` and `CsvWarrantyRepository`, which implement them, and `Main` creates the concrete classes. `ReturnService`, `WarrantyService` and the resolvers already used the interface names, so they did not change. The sale lookup part of V-26 was already solved by `SaleReader` (V-22). Behavior, messages, `returns.csv` and `warranties.csv` are unchanged | V-26 | DIP | refactor: add ReturnRepository and WarrantyRepository interfaces with Csv implementations (V-26) | Developer 2 |
| `MonthlyBalanceService` (new), `ReturnService`, `ReturnMenu`, `MainMenu`, `Main` | Moved `calculateMonthlySales`, `calculateMonthlyReturns` and `generateMonthlyBalance` out of `ReturnService` into the new `MonthlyBalanceService`, which receives `SaleService` and `ReturnService`. `ReturnMenu` now calls it for the monthly balance, and `MainMenu` and `Main` pass it along. The calculations and the dialog text are unchanged, so the same totals are shown | V-16 | SRP | refactor: extract MonthlyBalanceService from ReturnService (V-16) | Developer 2 |

## Out-of-scope findings and justifications

Violations of the consolidated audit that are not corrected in this phase are
listed here with their justification. _None so far._