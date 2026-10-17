\# Developer 1 SOLID Audit - Ronald



\## Scope



\*\*Audited state.\*\* `develop` at commit `4154b03` (merge of PR #55), which already includes the GRASP correction of Requirement 7.



| Module / requirement | Classes and methods |

|---|---|

| Products (base workshop) | `Product`, `VideoGame`, `Console`, `ProductRepository`, `ProductService` (every method except `restoreStock` and `reduceStock(String, int)`, which belong to the Technical Leader) |

| Accessories (Req. 1) | `Accessory`, `Controller`, `Cable`, `Memory` |

| Promotions (Req. 2) | `Promotion`, `PercentageDiscount`, `CategoryDiscount`, `BulkPurchaseDiscount` |

| Returns (Req. 3) | `Return`, `Sale.canBeReturned()` |

| Warranties (Req. 4) | `Warranty`, `BasicWarranty`, `ExtendedWarranty` |

| Exceptions and validation (Req. 6) | `exceptions` and `validation` packages |

| GRASP correction (Req. 7) | `ProductCategory`, `DataLogger`, `ConsoleDataLogger`, `ProductCsvMapper`, `VideoGameCsvMapper`, `ConsoleCsvMapper`, `ProductDAO`, and the methods `requiresWarranty`, `getCategory`, `hasEnoughStock`, `toCsvLine`, `getTypeCode` that I added to the model classes |



Problems whose origin is in another member's code are not audited here; they are listed in "Boundary findings" and were reported to the Technical Leader.

