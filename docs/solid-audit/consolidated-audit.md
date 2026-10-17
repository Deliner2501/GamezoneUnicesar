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