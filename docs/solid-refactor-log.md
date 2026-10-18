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

## Corrections by Developer 1

| Class | Change made | Violation ID | Principle applied | Commit/PR | Responsible |
|---|---|---|---|---|---|

## Corrections by Developer 2

| Class | Change made | Violation ID | Principle applied | Commit/PR | Responsible |
|---|---|---|---|---|---|

## Out-of-scope findings and justifications

Violations of the consolidated audit that are not corrected in this phase are
listed here with their justification. _None so far._