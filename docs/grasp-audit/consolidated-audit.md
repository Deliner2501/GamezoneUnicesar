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