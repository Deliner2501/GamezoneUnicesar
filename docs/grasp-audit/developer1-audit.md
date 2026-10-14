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


<!-- COMMIT-BREAK -->

## 1. Information Expert - Verdict: Violation

Two violations were found (V-01 and V-02). The pattern is applied correctly in the places listed at the end of this section.

### V-01

- **ID:** V-01
- **Pattern:** Information Expert
- **Module:** Products (base workshop) and validation layer (Req. 6)
- **Evidence:** the rule "there is enough stock for a quantity" exists in three classes. `Product.reduceStock`, line 32, applies it inside the class that owns the stock: `if (quantity > this.stock) {`. The same rule is repeated outside the owner in `ProductService.checkStock`, line 57: `return product.getStock() >= quantity;` and in `ProductValidator.validateStockAvailability`, line 53: `if (product.getStock() < requestedQuantity) {`.
- **Explanation:** `Product` owns `stock` and already decides whether a quantity is available. `ProductService` and `ProductValidator` read the value through a getter and take the same decision again, instead of asking the product.
- **Consequence:** if the rule changes (for example, a minimum reserve stock), three places must be edited and they can diverge: the validator could accept a quantity that `reduceStock` later rejects with a different exception type.
- **Proposed solution:** add `Product.hasEnoughStock(int quantity)` as the single source of the rule, and make `reduceStock`, `checkStock` and `validateStockAvailability` delegate to it (Information Expert).
- **Code of another member:** No.

### V-02

- **ID:** V-02
- **Pattern:** Information Expert
- **Module:** Returns (Req. 3), adjustment A5
- **Evidence:** `Return.calculateRefundAmount` computes `double subtotal = originalSale.getTotal();` and then `discountRate = originalSale.getDiscountAmount() / subtotal;`. The same block is repeated in `Return.generateReturnReceipt`. The per-item formula is also written twice in different forms: `product.getPrice() * (1 - discountRate)` and `listPrice - proportionalDiscount`.
- **Explanation:** the discount rate is a fact about the sale, and `Sale` owns both the subtotal and the discount amount. `Return` pulls two fields out of `Sale` and does the arithmetic itself, twice.
- **Consequence:** if the definition of the discount changes (for example, if it is calculated over the subtotal plus warranties), `Return` must be edited in two places and any other class that needs the rate would copy the formula again.
- **Proposed solution:** add `Sale.getDiscountRate()` and use it from `Return`; extract a private helper in `Return` for the refund of a single item so the receipt and the total use the same formula.
- **Code of another member:** Yes, the Technical Leader (`Sale`). The change is an additive method.

### Where the pattern is correctly applied

- `Warranty.isActive` and `Promotion.isActive`: each class owns its dates and answers whether it is active.
- `Accessory.isCompatibleWith`: `Accessory` owns the compatibility list and answers the question.
- `Sale.canBeReturned` (lines 146-161): `Sale` owns the sale date and decides whether the 30-day window is still open.
- `Return.calculateRefundAmount` sums the prices of the products that the return itself holds.

## 2. Creator - Verdict: Correct

- `ProductDAO.parseLine` (lines 124 and 126) creates `VideoGame` and `Console` from a CSV record. The DAO holds the data needed to initialize them, which is the Creator criterion.
- The `Accessory` constructor creates the `ArrayList` for the compatibility list it contains.
- The validators create the exception that matches the problem at the exact point where the problem is detected.
- No other class in the audited scope instantiates domain objects. Instantiation in the user interface is reported in the section "Boundary findings" (B-2).

## 3. Controller - Verdict: Correct

`ProductService` is the entry point of the product use cases. `registerProduct`, `listAvailableProducts`, `checkStock` and `findProductById` receive the operation from the upper layer and delegate to `ProductValidator` and `ProductDAO`. They contain no user-interface code and no domain formulas. The remaining audited classes (model, validators, exceptions) are not entry points of system events, so the pattern is not applicable to them.

## 4. Low Coupling - Verdict: Violation

### V-03

- **ID:** V-03
- **Pattern:** Low Coupling
- **Module:** Products (base workshop)
- **Evidence:** `ProductService`, line 14 `private ProductDAO productDAO;` and lines 16-17, constructor `public ProductService(ProductDAO productDAO)`, depend on the concrete class. `ProductDAO` fixes the storage in the code: line 17 `private static final String FILE_PATH = "data/products.csv";`, line 40 `File file = new File(FILE_PATH);` and line 95 `new PrintWriter(new FileWriter(FILE_PATH))`. The object graph is built in `Main`, line 38 `ProductDAO productDAO = new ProductDAO();` and line 46 `new ProductService(productDAO)`.
- **Explanation:** the service is tied to one implementation (a CSV file at a fixed path). Neither the storage nor the path can be replaced without editing the service or the code that builds it.
- **Consequence:** the service cannot be tested without touching the real file, and a change of storage (another format, a database) reaches `ProductService` and `Main`.
- **Proposed solution:** introduce a `ProductRepository` interface implemented by `ProductDAO` (which keeps its name and default constructor), make `ProductService` depend on the interface, and receive the file path through the constructor with `data/products.csv` as default.
- **Code of another member:** No, if the change is additive (`ProductDAO` keeps its name and default constructor, so `Main` still compiles).

### Where coupling is correctly low

- The validators depend only on the model and on the exceptions, never on services or persistence.
- `Promotion`, `Warranty` and `Return` depend on `Sale` and `Product` only because the requirements make those relationships part of their definition.
- The exception classes have no dependencies on other layers.

## 5. High Cohesion - Verdict: Violation

### V-04

- **ID:** V-04
- **Pattern:** High Cohesion
- **Module:** Products (base workshop), exception handling refactor (Req. 6)
- **Evidence:** `ProductDAO.findAll`, lines 49-50: `} catch (Exception e) {` followed by `System.out.println("Skipping corrupted line: " + line);`.
- **Explanation:** a persistence class writes directly to the console, which is a presentation responsibility, and it catches every `Exception`, hiding the cause of the failure.
- **Consequence:** the DAO is tied to a console, the message is technical and in English (the Req. 6 goal was that technical messages do not reach the user), and the upper layers can never find out that data was corrupted.
- **Proposed solution:** replace `println` with a logging collaborator injected into the DAO that prints the same message (so the visible behavior is preserved), and catch only the parsing exceptions (`NumberFormatException`, `ArrayIndexOutOfBoundsException`). The logger is a Pure Fabrication.
- **Code of another member:** No.

### Where cohesion is correct

- `Product`, `Accessory`, `Promotion` and `Warranty` group state and the operations that depend on that state, and nothing else.
- Each validator covers one module; each exception class has one purpose.
- `Return.generateReturnReceipt` and `Warranty.generateWarrantyCertificate` format text, but the requirements explicitly demand those methods, they use only the entity's own data and they are short. They are noted here and not counted as violations.

## 6. Polymorphism - Verdict: Violation

### V-05

- **ID:** V-05
- **Pattern:** Polymorphism
- **Module:** Products (base workshop)
- **Evidence:** `ProductDAO.writeAll`, line 97 `if (p instanceof VideoGame) {` and line 102 `} else if (p instanceof Console) {`. `ProductDAO.parseLine`, line 123 `if (type.equals("VIDEOGAME")) {` and line 125 `} else if (type.equals("CONSOLE")) {`.
- **Explanation:** the DAO branches on the concrete type of the product in two methods instead of delegating the behavior to the type.
- **Consequence:** a new product type requires editing both methods. In addition, `writeAll` has no final `else`, so a `Product` of any other type is silently skipped and lost on the next write.
- **Proposed solution:** one CSV mapper per product type, found through the discriminator, so that adding a type means adding a class (Polymorphism with Pure Fabrication).
- **Code of another member:** No.

### V-06

- **ID:** V-06
- **Pattern:** Polymorphism
- **Module:** Promotions (Req. 2), adjustment A1
- **Evidence:** `CategoryDiscount.matchesCategory` is a chain of `targetCategory.equalsIgnoreCase("VIDEOGAME")` / `"CONSOLE"` / `"ACCESSORY"` returning `product instanceof VideoGame`, `product instanceof Console` and `product instanceof Accessory`. The same three literals are validated in the constructor of `CategoryDiscount`. Adjustment A1 itself shows the cost: adding `ACCESSORY` required changes in `CategoryDiscount` (commit `e97aeec`), `PromotionService` (commit `53f06d4`), the menu prompt (commit `bae5473`) and `data/promotions.csv`.
- **Explanation:** the promotion decides the category of a product through a type test and a text code, instead of asking the product.
- **Consequence:** a new category of product means editing the constructor check, `matchesCategory`, the service validation, the menu and the data file.
- **Proposed solution:** an enum `ProductCategory` and an abstract `Product.getCategory()` implemented by `VideoGame`, `Console` and `Accessory`; `CategoryDiscount` compares categories and the validation uses the enum.
- **Code of another member:** Possibly Developer 2 (`PromotionService`, `PromotionDAO`) if the constructor signature changes. It can be avoided by keeping the `String` constructor and converting internally.

### Where the pattern is correctly applied

- `Product.getFullDescription()` is abstract and implemented by `VideoGame`, `Console`, `Controller`, `Cable` and `Memory`.
- `Promotion.calculateDiscount(Sale)` is abstract and each subclass defines its own strategy.
- `Warranty` declares `getDurationInMonths`, `getWarrantyType` and `getAdditionalCost` as abstract methods, and its constructor obtains `endDate` through `getDurationInMonths()` without knowing the concrete type.