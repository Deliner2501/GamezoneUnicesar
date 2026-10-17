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
