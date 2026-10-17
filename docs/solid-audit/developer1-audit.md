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



\## 1. Single Responsibility Principle - Verdict: Violation



\### V-01

\- \*\*ID:\*\* V-01

\- \*\*Principle:\*\* SRP

\- \*\*Module:\*\* Products (base workshop) and GRASP correction (Req. 7)

\- \*\*Evidence:\*\* `ProductDAO.parseLine` reads the common columns itself (`double price = Double.parseDouble(parts\[3]); int stock = Integer.parseInt(parts\[4]);`) while `VideoGameCsvMapper.toCsvLine` and `ConsoleCsvMapper.toCsvLine` write those same columns (`"VIDEOGAME," + vg.getId() + "," + vg.getTitle() + "," + vg.getPrice() + ...`). `ProductDAO` also creates the folder and opens the file (`new File(filePath).getParentFile()`, `new PrintWriter(new FileWriter(filePath))`).

\- \*\*Explanation:\*\* the knowledge of the CSV layout is split between the DAO and the mappers, so the DAO has two reasons to change: file access and the column layout.

\- \*\*Consequence:\*\* changing the order or number of common columns requires editing `ProductDAO` and every mapper.

\- \*\*Proposed solution:\*\* make each `ProductCsvMapper` own both directions (`toCsvLine` and `parse(String\[] parts)`), leaving the DAO only with file reading and writing.

\- \*\*Code of another member:\*\* No.



\### V-02

\- \*\*ID:\*\* V-02

\- \*\*Principle:\*\* SRP

\- \*\*Module:\*\* Accessories (Req. 1), Promotions (Req. 2), Warranties (Req. 4), GRASP correction (Req. 7)

\- \*\*Evidence:\*\* `Accessory.toCsvLine()` (abstract) with `Controller.toCsvLine`, `Cable.toCsvLine`, `Memory.toCsvLine` and `Accessory.compatibleConsolesAsCsv()`; `Promotion.toCsvLine()` (abstract) with `PercentageDiscount`, `CategoryDiscount` and `BulkPurchaseDiscount`; `Warranty.getTypeCode()` documented as "the code used to identify this warranty type when it is stored on disk". Example: `return "CONTROLLER," + getId() + "," + getTitle() + "," + ...`.

\- \*\*Explanation:\*\* domain classes know the persistence format. I introduced these methods in the GRASP correction (V-17, V-19, V-22) to remove `instanceof` from the DAOs. Products use a mapper (`ProductCsvMapper`) instead, so the design is inconsistent.

\- \*\*Consequence:\*\* a change of storage format (another file layout, JSON, a database) modifies the model classes, which should change only for business reasons.

\- \*\*Proposed solution:\*\* move the format to `AccessoryCsvMapper`, `PromotionCsvMapper` and a warranty type mapper, following `ProductCsvMapper`.

\- \*\*Code of another member:\*\* Yes, Developer 2 (`AccessoryDAO`, `PromotionDAO`, `WarrantyRepository`, which call these methods).



\### V-03

\- \*\*ID:\*\* V-03

\- \*\*Principle:\*\* SRP

\- \*\*Module:\*\* Returns (Req. 3) and Warranties (Req. 4)

\- \*\*Evidence:\*\* `Return.generateReturnReceipt()` builds Spanish text with `StringBuilder` (`receipt.append("Recibo de devolución ")...`); `Warranty.generateWarrantyCertificate()` does the same (`certificate.append("Certificado de garantía ")...`). The Leader moved the equivalent method of `Sale` to `SaleReceiptFormatter` (GRASP V-01).

\- \*\*Explanation:\*\* `Return` and `Warranty` hold both domain rules (refund, coverage) and presentation text.

\- \*\*Consequence:\*\* a change in the wording or layout of a receipt modifies domain classes.

\- \*\*Proposed solution:\*\* one formatter per document (`ReturnReceiptFormatter`, `WarrantyCertificateFormatter`) with the same output, placed in the layer that respects the dependency direction (see B-1).

\- \*\*Code of another member:\*\* Yes, the Technical Leader (callers in the menus).



\### Where the principle is correctly applied

\- `Product` groups identity, price and stock with the operations over its own stock (`reduceStock`, `increaseStock`, `hasEnoughStock`).

\- `ProductService` coordinates product use cases and delegates validation to `ProductValidator` and storage to `ProductRepository`.

\- `DataLogger` and `ConsoleDataLogger` each have a single purpose.



\## 2. Open/Closed Principle - Verdict: Violation



\### V-04

\- \*\*ID:\*\* V-04

\- \*\*Principle:\*\* OCP

\- \*\*Module:\*\* Products (base workshop) and GRASP correction (Req. 7)

\- \*\*Evidence:\*\* `ProductService.registerVideoGame(...)` and `registerConsole(...)` each do `registerProduct(new VideoGame(...))` / `registerProduct(new Console(...))`, and the class imports `VideoGame` and `Console`.

\- \*\*Explanation:\*\* every new product type needs a new `registerXxx` method in the service. I added these methods in the GRASP correction (FF-02) to remove `new` from the menu.

\- \*\*Consequence:\*\* adding a product type modifies `ProductService`, which already works.

\- \*\*Proposed solution:\*\* a product factory abstraction, one implementation per type, that the service receives and uses.

\- \*\*Code of another member:\*\* Yes, the Technical Leader (`MainMenu` and its sub-menus call the two methods).



\### V-05

\- \*\*ID:\*\* V-05

\- \*\*Principle:\*\* OCP

\- \*\*Module:\*\* GRASP correction (Req. 7)

\- \*\*Evidence:\*\* `ProductDAO`: `private final List<ProductCsvMapper> mappers = List.of(new VideoGameCsvMapper(), new ConsoleCsvMapper());`.

\- \*\*Explanation:\*\* the list of supported types is fixed inside the DAO.

\- \*\*Consequence:\*\* a new product type requires editing `ProductDAO` besides adding its mapper.

\- \*\*Proposed solution:\*\* receive the mapper list through the constructor (default list kept in the no-argument constructor so `Main` still compiles).

\- \*\*Code of another member:\*\* No.



\### Where the principle is correctly applied

\- `Promotion.calculateDiscount(Sale)` is abstract: a new promotion type is a new subclass without changing the existing ones.

\- `Warranty` declares `getDurationInMonths`, `getWarrantyType` and `getAdditionalCost` as abstract; a new warranty type does not change `Warranty`.

\- `Accessory` subclasses implement `getFullDescription`, `getTypeCode` and `getCategory` through the base class.

