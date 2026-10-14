# Developer 1 GRASP Audit - Ronald

## Scope

**Audited state.** Branch `feature/grasp-audit` at commit `a377cfb`. The `src` tree is identical to `develop` at `d669eed` (`git diff d669eed HEAD --stat -- src` returns no differences), so all the evidence below describes the current state of `develop`.

**Classes and methods audited**, by module and requirement (responsibility table, section 4 of the requirement):

| Module / requirement | Classes and methods |
|---|---|
| Products (base workshop) | `Product`, `VideoGame`, `Console`, `ProductDAO`, `ProductService` (every method except `restoreStock`, which the Technical Leader audits) |
| Accessories (Req. 1) | `Accessory`, `Controller`, `Cable`, `Memory` |
| Promotions (Req. 2) | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount`, including adjustment A1 in `CategoryDiscount` |
| Returns (Req. 3) | `Return` (including adjustment A5) and `Sale.canBeReturned()` (both overloads) |
| Warranties (Req. 4) | `Warranty`, `BasicWarranty`, `ExtendedWarranty` |
| Exceptions layer (Req. 6) | `GameZoneException`, `ResourceNotFoundException`, `BusinessRuleException`, `InvalidDataException`, `PersistenceException` |
| Validation layer (Req. 6) | `ProductValidator`, `PersonValidator`, `SaleValidator` |

Problems whose origin is in another member's code are not audited here. They are listed in the section "Boundary findings" and were reported to the Technical Leader.