# Leader SOLID Audit — Deiner Andrés De Luque Navarro

**Audited commit:** `develop` at `221b709` (GRASP refactor from Requirement 7
already integrated).

## Scope

As Technical Leader, my audit covers the classes I am responsible for per
`TEAM.md` and the requirement history. Class names in the Requirement 8
responsibility table map to the current repository as follows:

| Requirement 8 name | Class in the repository | Layer |
|---|---|---|
| `Sale` | `Sale` | model |
| `SaleRepository` | `SaleDAO` | persistence |
| `SaleService` | `SaleService` (including the accessory, promotion and warranty integrations) | service |
| `ConsoleMenu` | `MainMenu` plus the classes extracted from it in Requirement 7: `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu`, `MenuDialogs`, `SaleReceiptFormatter` | ui |
| `Main` | `Main` | root |
| `ProductService.restoreStock` | `ProductService.restoreStock` and `ProductService.reduceStock` (added by me in Requirement 7) | service |

Classes I created or modified during the GRASP correction (Requirement 7)
are included above: `SaleReceiptFormatter`, `MenuDialogs`, the seven menu
classes and the helper methods extracted from `SaleService.registerSale`.

Everything else belongs to Developer 1 or Developer 2 and is audited in
their reports. Problems that originate in their code are reported in the
"frontier findings" of the consolidated audit and are not audited here.

## Criteria used

| Verdict | Criterion |
|---|---|
| **Violation** | A concrete, checkable symptom of the principle being broken (class, method and quoted code from `develop`), with its real consequence. |
| **Correct** | The class and the place where the principle is met are named, with a one-line justification. |
| **Not applicable** | The class has no point of application for the principle, and the reason is stated (for example, "no inheritance relationship exists for LSP"). |

Principle symptoms searched for:

- **SRP:** a class or method with more than one reason to change.
- **OCP:** adding a new kind of item or area requires editing existing code
  (type-based branching, hard-coded lists).
- **LSP:** a subtype cannot replace its base type, or it throws or changes
  the contract.
- **ISP:** a client depends on methods it does not use.
- **DIP:** a high-level class depends on a concrete low-level class or on a
  layer above it, instead of an abstraction.

  
---

## 1. `Sale.java`

### SRP — Correct

`Sale` has one reason to change: the business data and calculations of a
sale. Its methods all work on its own state: `addProduct` (line 69),
`calculateTotal` (line 83), `applyPromotion` (line 172), `addAdditionalCost`
(line 230), `getDiscountRate` (line 208) and `getFinalTotal` (line 217). The
receipt text was already moved out to `SaleReceiptFormatter` in
Requirement 7; the only receipt-related code left in the class is the
delegating method `generateReceipt()` (line 256), whose problem is a
dependency direction issue and is reported under DIP (V-01), not SRP.
The 30-day return window `canBeReturned()` (lines 146–161) is audited by
Developer 1, as assigned in the responsibility table.

### OCP — Correct

`calculateTotal()` (lines 83–90) sums `product.getPrice()` over a
`List<Product>`. It never checks the concrete type of an item, so a new
kind of sellable item (a new `Product` or `Accessory` subtype) is added
without modifying `Sale`. The promotion is received already calculated
through `applyPromotion(String, double)` (line 172), so `Sale` is also
closed to new promotion types.

### LSP — Not applicable

`Sale` is a leaf class: `public class Sale` (line 15) has no `extends`, and
nothing extends it, so there is no substitution relationship to audit in
this class. As a client of the `Product` hierarchy, `Sale` only uses
`getPrice()` and `getId()`, which every subtype (including `Accessory`)
honors, so substitution of subtypes inside `Sale.products` is safe. The
hierarchy itself is audited by Developer 1.

### ISP — Not applicable

`Sale` does not implement or declare any interface (line 15), so there is
no interface whose methods a client could be forced to depend on. Its
clients (`SaleReceiptFormatter`, `SaleDAO`, `SaleService`) call only the
getters they need.

### DIP — Violation (V-01)

**V-01 — The domain class `Sale` depends on the UI layer through
`generateReceipt()`**
- **Principle:** Dependency Inversion Principle
- **Module:** `Sale.generateReceipt()`, `Sale.java` lines 256–258:
```java
  public String generateReceipt() {
      return com.gamezone.ui.SaleReceiptFormatter.format(this);
  }
```
- **Evidence:** A `model` class references the concrete class
  `com.gamezone.ui.SaleReceiptFormatter` (fully qualified, so there is no
  import). `grep` for `import com.gamezone.ui` in `model`, `service` and
  `persistence` returns nothing, because this reference is the only one and
  it is written inline. The dependency also appears in the JavaDoc link at
  line 250. The formatter in turn depends on `Sale`
  (`SaleReceiptFormatter.format(Sale)`), so `model` and `ui` depend on each
  other.
- **Explanation:** The model is the highest-level, most stable layer of the
  system and must not depend on a concrete detail of the presentation
  layer. The required layer direction is `ui → service → persistence →
  model`; this call goes the opposite way. It was introduced when V-01 of
  the GRASP audit (receipt text inside `Sale`) was corrected by moving the
  text to `SaleReceiptFormatter` but leaving a delegating method in the
  model.
- **Consequence:** `Sale` cannot be compiled, tested or reused without the
  `ui` package. Any change in the formatter's package or signature breaks
  the domain class, and the model can never be shipped to a context without
  a UI.
- **Proposed solution:** Remove `generateReceipt()` from `Sale` and make its
  only two callers call the formatter directly: `SaleMenu.registerSale()`
  (line 115) and `SaleMenu.printSales()` (line 161) would use
  `SaleReceiptFormatter.format(sale)`. The receipt text and every message
  shown to the user stay identical.
- **Code of another member:** No — `Sale`, `SaleMenu` and
  `SaleReceiptFormatter` are all in my own scope. `grep` shows no other
  class calls `generateReceipt()`.

### Note — functional defect found (out of scope for this requirement)

`Sale.getDiscountRate()` (line 209) returns `discountAmount / total`
without checking `total == 0`, while its JavaDoc (lines 205–206) promises
`0.0` when the subtotal is zero. This is a mismatch between contract and
code, not a SOLID violation, and fixing it would change observable
behavior (`Return` has its own guard today, so the practical impact is
limited). It is recorded as an **out-of-scope functional defect** for the
consolidated audit and is not corrected in Phase 2. Developer 1 already
reported the same difference when applying V-09 of Requirement 7, so it is
not a new finding for that requirement.


---

## 2. `SaleDAO.java` (`SaleRepository` in the Requirement 8 table)

### SRP — Violation (V-02)

**V-02 — `SaleDAO` mixes file I/O, line serialization and cross-DAO reference
resolution**
- **Principle:** Single Responsibility Principle
- **Module:** `SaleDAO`, whole class (225 lines). Three groups of members
  with different reasons to change:
  1. File access: `save` (lines 53–64) and `findAll` (lines 75–95) open
     `data/sales.txt` and read/write it.
  2. Serialization format: `toLine` (lines 149–168) and `fromLine`
     (lines 174–207) define the field layout
     `id;date;customerId;sellerId;productIds;promotion;discount` using
     `FIELD_SEPARATOR` and `PRODUCT_SEPARATOR` (lines 27–28).
  3. Reference resolution: `findCustomerById` (lines 209–216) and
     `findSellerById` (lines 218–225) scan `personDAO.loadCustomers()` /
     `loadSellers()`, and `fromLine` line 193 queries `productDAO.findById`.
- **Evidence:** `fromLine` (a serialization method) calls `System.out.println`
  (line 184) and three different collaborators, so changing the file format,
  the storage medium or the way ids are resolved all force edits in the same
  class. In Developer 1's `ProductDAO` the same concerns are already split
  (`ProductCsvMapper` for the format, `DataLogger` for messages), so this DAO
  is inconsistent with the rest of the persistence layer.
- **Explanation:** A class must have a single reason to change. Here the
  format of a sale line, the storage mechanism and the reference-resolution
  strategy are three independent axes of change.
- **Consequence:** Adding a field to a sale (for example, the warranty cost)
  means editing the same methods that open the file, and the
  serialization cannot be tested without touching the disk.
- **Proposed solution:** Extract the line format into a dedicated
  `SaleCsvMapper` (`toLine(Sale)` / `fromLine(String)`, with the id
  resolution received as collaborators), following the `ProductCsvMapper`
  precedent, and leave `SaleDAO` responsible only for reading and writing
  lines. The file format and the console message stay identical.
- **Code of another member:** No — the mapper is a new class in the
  persistence package and only `SaleDAO` (mine) changes.

### OCP — Correct

`findAll`, `findByCustomer` and `findBySeller` (lines 75–143) filter by id
and contain no type-based branching (`instanceof`, switch on kind), so new
queries or new sale attributes are added without modifying the existing
methods; format changes are confined to `toLine`/`fromLine`. The fact that
`fromLine` resolves items only through `ProductDAO` (line 193) is a
functional defect, not an extension-point problem, and is recorded below as
out of scope.

### LSP — Not applicable

`public class SaleDAO` (line 24) neither extends a class nor implements an
interface, and nothing extends it, so there is no substitution relationship.

### ISP — Not applicable

`SaleDAO` implements no interface, so no client is forced to depend on
unused interface methods. Its current clients use disjoint subsets of its
API: `SaleService` uses `save`, `findAll`, `findByCustomer` and
`findBySeller`; `WarrantyService` uses it only to look up sales. This will
become relevant after V-03 introduces an abstraction (see below).

### DIP — Violation (V-03)

**V-03 — `SaleDAO` depends on concrete `PersonDAO` and `ProductDAO`, and
its clients depend on the concrete `SaleDAO`**
- **Principle:** Dependency Inversion Principle
- **Module:** `SaleDAO` lines 30–31 and 40–43:
```java
  private PersonDAO personDAO;
  private ProductDAO productDAO;
  public SaleDAO(PersonDAO personDAO, ProductDAO productDAO) { ... }
```
  and the consumers `SaleService` (line 33 `private SaleDAO saleDAO`,
  constructor line 48) and `WarrantyService` (line 32
  `private SaleDAO saleDAO`, constructor line 42), wired in `Main` line 38.
- **Evidence:** Developer 1 already created the abstraction
  `ProductRepository` (`save`, `findAll`, `findById`, `update`) and
  `ProductService` depends on it, but `SaleDAO` still receives the concrete
  `ProductDAO`. `PersonDAO` has no abstraction at all, and `SaleDAO` itself
  has none: `SaleService` and `WarrantyService` depend on the concrete class.
- **Explanation:** Higher-level code (the service layer and the sale
  persistence logic) depends on concrete file-based implementations instead
  of abstractions that both sides can depend on.
- **Consequence:** `SaleService` cannot be tested with an in-memory
  repository and any change of storage (database, another file format)
  forces changes in the service layer.
- **Proposed solution:**
  1. `SaleDAO` receives `ProductRepository` instead of `ProductDAO`
     (the interface already exists).
  2. Create a `PersonRepository` interface (`loadCustomers`,
     `loadSellers`) implemented by `PersonDAO`, and have `SaleDAO` receive
     it.
  3. Create a `SaleRepository` interface (`save`, `findAll`,
     `findByCustomer`, `findBySeller`) implemented by `SaleDAO`, and have
     `SaleService` receive it.
  Behavior and data files do not change.
- **Code of another member:** **Yes.** `PersonDAO` is Developer 2's class
  (it must implement the new `PersonRepository` interface) and
  `WarrantyService` (Developer 2) must depend on `SaleRepository` instead of
  `SaleDAO`. The changes in `SaleDAO`, `SaleService` and `Main` are mine.
  Frontier finding, to be consolidated with Developer 2.

### Note — functional defect (already recorded in Requirement 7)

`SaleDAO.fromLine` (line 193) resolves every item id only through
`productDAO.findById`, so accessories in a persisted sale are lost when it
is reloaded. This was already registered as an out-of-scope defect in the
Requirement 7 audit (`docs/grasp-audit/leader-audit.md`), so it is **not
registered again** here and remains uncorrected.