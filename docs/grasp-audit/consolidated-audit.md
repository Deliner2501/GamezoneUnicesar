# Consolidated GRASP Audit — Team

**Audited state:** branch `feature/grasp-audit`, which at the time of this
consolidation has not yet been merged to `develop` (`develop` remains at
`d669eed`). All three individual reports confirm their audited `src` tree
is identical to `develop` at that commit, so every finding below describes
the current state of `develop`.

**Process note:** per the requirement, this phase should produce a
minimum of 3 approved and merged Pull Requests (one per member). As a
team we chose to keep all Phase 1 work on the single shared
`feature/grasp-audit` branch instead. This consolidated audit assumes
that branch will be merged as a single PR; the team is aware this does
not satisfy the 3-PR minimum and has accepted that risk.

## Coverage Table

| Class / method | Auditor | Individual report |
|---|---|---|
| `Sale`, `SaleDAO`, `SaleService`, `MainMenu`, `Main`, `ProductService.restoreStock` | Technical Leader | `leader-audit.md` |
| `Product`, `VideoGame`, `Console`, `ProductDAO`, `ProductService` (except `restoreStock`) | Developer 1 | `developer1-audit.md` |
| `Accessory`, `Controller`, `Cable`, `Memory` | Developer 1 | `developer1-audit.md` |
| `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` | Developer 1 | `developer1-audit.md` |
| `Return`, `Sale.canBeReturned()` | Developer 1 | `developer1-audit.md` |
| `Warranty`, `BasicWarranty`, `ExtendedWarranty` | Developer 1 | `developer1-audit.md` |
| `GameZoneException` hierarchy, `ProductValidator`, `PersonValidator`, `SaleValidator` | Developer 1 | `developer1-audit.md` |
| `Person`, `Customer`, `Seller`, `PersonDAO`, `PersonService` | Developer 2 | `developer2-audit.md` |
| `AccessoryDAO`, `AccessoryService` | Developer 2 | `developer2-audit.md` |
| `PromotionDAO`, `PromotionService` | Developer 2 | `developer2-audit.md` |
| `ReturnRepository`, `ReturnService` (incl. monthly balance) | Developer 2 | `developer2-audit.md` |
| `WarrantyRepository`, `WarrantyService` | Developer 2 | `developer2-audit.md` |

**Verification:** every class listed in `TEAM.md`'s responsibility table
appears in exactly one of the three reports; no class is audited twice or
left uncovered. `developer2-audit.md` does not include its own closing
"Boundary findings," "Out-of-scope findings" or "Summary of verdicts"
sections — the Technical Leader identified its cross-member and
out-of-scope items directly while reading the individual report, and
those are captured in this consolidated document instead (see below).

## Final Team Verdict by Pattern

| # | Pattern | Team Verdict | Violations |
|---|---|---|---|
| 1 | Information Expert | Violation | V-08, V-09 |
| 2 | Creator | Violation | V-05, V-34 |
| 3 | Controller | Correct | — |
| 4 | Low Coupling | Violation | V-10, V-26, V-34 |
| 5 | High Cohesion | Violation | V-03, V-06, V-11, V-28 |
| 6 | Polymorphism | Violation | V-04, V-07, V-12, V-13, V-17, V-19, V-22, V-29, V-31 |
| 7 | Pure Fabrication | Violation | V-20, V-24 |
| 8 | Indirection | Correct (one limitation recorded under V-10) | — |
| 9 | Protected Variations | Violation | V-01, V-02, V-14, V-15, V-16, V-18, V-21, V-23, V-25, V-27, V-30, V-32, V-33 |

**Summary:** 34 violations total across the team (7 Leader + 7 Developer 1
+ 19 Developer 2 + 1 new finding — V-34 — identified by Developer 1 in the
Leader's code and not originally self-reported). Only Controller comes
back fully clean across the whole system; Indirection has one minor,
already-tracked limitation. The dominant failure modes are Protected
Variations (13 occurrences — mostly inconsistent exception wrapping
across DAOs/services not yet migrated to the Req. 6 hierarchy) and
Polymorphism (9 occurrences — `instanceof` checks instead of type
discriminator methods, repeated independently across almost every DAO and
several services).

---

## Definitive Violation List

Each ID below is the consolidated identifier. "Origin" points back to the
individual report and original ID so the full evidence/explanation can be
traced. "Responsible" is who corrects it in Phase 2, per code ownership in
`TEAM.md` — not necessarily who found it.

| ID | Pattern | Class / Module | Summary | Responsible | Origin |
|---|---|---|---|---|---|
| V-01 | Protected Variations | `Sale.generateReceipt()` | Domain class hard-codes Spanish presentation text | Leader | leader-audit V-01 |
| V-02 | Protected Variations | `SaleDAO` | `findByCustomer`/`findBySeller` leak raw `IOException`, unlike `save`/`findAll` | Leader | leader-audit V-02 |
| V-03 | High Cohesion | `SaleService.registerSale` | 9-step, 6-collaborator method, too many responsibilities | Leader | leader-audit V-03 |
| V-04 | Polymorphism | `SaleService.registerSale` | `instanceof Console` decides warranty assignment | Leader + Dev 1 | leader-audit V-04 |
| V-05 | Creator | `MainMenu.registerVideoGame/registerConsole` | UI directly instantiates `VideoGame`/`Console` | Leader + Dev 1 | leader-audit V-05 (confirmed independently as developer1-audit B-2) |
| V-06 | High Cohesion | `MainMenu` (whole class) | Single class holds UI for 7 unrelated feature areas | Leader | leader-audit V-06 |
| V-07 | Polymorphism | `MainMenu.registerSale` | `instanceof Console` decides whether to offer extended warranty | Leader + Dev 1 | leader-audit V-07 |
| V-08 | Information Expert | `Product`, `ProductService.checkStock`, `ProductValidator` | "Enough stock" rule duplicated in 3 places | Dev 1 | developer1-audit V-01 |
| V-09 | Information Expert | `Return.calculateRefundAmount`/`generateReturnReceipt` | Discount-rate formula pulled from `Sale` and recomputed twice | Dev 1 + Leader | developer1-audit V-02 |
| V-10 | Low Coupling | `ProductService` | Depends on concrete `ProductDAO`, no interface | Dev 1 | developer1-audit V-03 |
| V-11 | High Cohesion | `ProductDAO.findAll` | Prints to console and swallows every `Exception` | Dev 1 | developer1-audit V-04 |
| V-12 | Polymorphism | `ProductDAO.writeAll/parseLine` | `instanceof VideoGame/Console` branching; `writeAll` silently drops unknown types | Dev 1 | developer1-audit V-05 |
| V-13 | Polymorphism | `CategoryDiscount` | Category match via `instanceof` + string literals | Dev 1 | developer1-audit V-06 |
| V-14 | Protected Variations | `Accessory`, `Return` | Getters expose live internal lists (no unmodifiable wrapper) | Dev 1 + Dev 2 (to verify call sites) | developer1-audit V-07 |
| V-15 | Protected Variations | `Customer.getPurchaseHistory()` | Returns mutable internal list directly | Dev 2 | developer2-audit V-01 |
| V-16 | Protected Variations | `PersonDAO` | Dead `throws IOException` on 4 methods that now always wrap as `PersistenceException` | Dev 2 + Leader | developer2-audit V-02 |
| V-17 | Polymorphism | `AccessoryDAO.writeAll` | `instanceof Controller/Cable/Memory` for serialization | Dev 2 + Dev 1 | developer2-audit V-03 |
| V-18 | Protected Variations | `AccessoryDAO` | Swallows `IOException`, prints and continues | Dev 2 | developer2-audit V-04 |
| V-19 | Polymorphism | `AccessoryService.matchesType` | Type-string switch with `instanceof` | Dev 2 + Dev 1 | developer2-audit V-05 |
| V-20 | Pure Fabrication | `AccessoryService.validateAccessory` | Validation kept as private method, unlike other domains | Dev 2 | developer2-audit V-06 |
| V-21 | Protected Variations | `AccessoryService` | Throws raw `IllegalArgumentException`, not the typed hierarchy | Dev 2 | developer2-audit V-07 |
| V-22 | Polymorphism | `PromotionDAO.saveAll` | `instanceof` branching for serialization | Dev 2 + Dev 1 | developer2-audit V-08 |
| V-23 | Protected Variations | `PromotionDAO` | Swallows `IOException`, prints and continues | Dev 2 | developer2-audit V-09 |
| V-24 | Pure Fabrication | `PromotionService` | Validation kept as private methods | Dev 2 | developer2-audit V-10 |
| V-25 | Protected Variations | `PromotionService` | Throws raw `IllegalArgumentException` | Dev 2 | developer2-audit V-11 |
| V-26 | Low Coupling | `ReturnRepository` | Persistence class depends on 3 service-layer classes (inverted layering) | Dev 2 | developer2-audit V-12 |
| V-27 | Protected Variations | `ReturnRepository.fromLine` | Silently skips unresolvable returns via `println` | Dev 2 | developer2-audit V-13 |
| V-28 | High Cohesion | `ReturnService.registerReturn` | 9-step god method, same shape as V-03 | Dev 2 | developer2-audit V-14 |
| V-29 | Polymorphism | `ReturnService.registerReturn` | `instanceof Console`/`instanceof Accessory` routing | Dev 2 + Dev 1 | developer2-audit V-15 |
| V-30 | Protected Variations | `ReturnService` | Throws raw `IllegalArgumentException` throughout | Dev 2 | developer2-audit V-16 |
| V-31 | Polymorphism | `WarrantyRepository.toLine` | `instanceof ExtendedWarranty` instead of `getWarrantyType()` | Dev 2 | developer2-audit V-17 |
| V-32 | Protected Variations | `WarrantyRepository` | Raw `IOException` propagated, not wrapped | Dev 2 | developer2-audit V-18 |
| V-33 | Protected Variations | `WarrantyService` | Raw `IllegalArgumentException` + silent `println` skips | Dev 2 | developer2-audit V-19 |
| V-34 | Low Coupling | `SaleService`, `SaleDAO` | Depend directly on concrete `ProductDAO` instead of `ProductService` | Leader | developer1-audit B-1 (not self-reported in leader-audit; added here) |