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


---

## 3. `SaleService.java`

### SRP — Correct

`SaleService` has one reason to change: the workflow of the "register a
sale" use case and the sale queries. `registerSale` (lines 92–124) reads as
a sequence of calls to single-purpose helpers (`resolveCustomer` line 131,
`resolveSeller` line 144, `resolveSaleItems` line 158, `buildSale` line 195,
`applyBestPromotion` line 220, `assignWarranties` line 235,
`updateInventory` line 257), each delegating the real work to the class
that owns it (`PersonService`, `PromotionService`, `WarrantyService`,
`ProductService`). The accessory-specific stock logic that does live in this
class is reported as part of V-04, since it is the same code region.

### OCP — Violation (V-04)

**V-04 — Item-kind branching (product vs. accessory) is repeated in three
methods, so a new kind of sellable item forces edits in `SaleService`**
- **Principle:** Open/Closed Principle (related to SRP: accessory stock
  handling inside the sales service)
- **Module:** `SaleService.registerSale`, `resolveSaleItems`, `buildSale`
  and `updateInventory`:
  - `registerSale` lines 101–103 keep two parallel maps,
    `Map<Product, Integer> resolvedProducts` and
    `Map<Accessory, Integer> resolvedAccessories`.
  - `resolveSaleItems` lines 169–187 first tries
    `productService.findProductById(itemId)` (lines 169–174) and then
    `accessoryDAO.findById(itemId)` (lines 176–185), with its own stock rule
    for accessories (`accessory.getStock() < quantity`, lines 178–182) that
    duplicates `ProductValidator.validateStockAvailability` (line 171).
  - `buildSale` lines 198–211 has one loop per map.
  - `updateInventory` lines 258–268 has one loop per map: products through
    `productService.reduceStock` (line 261), accessories through
    `accessory.reduceStock` plus `accessoryDAO.update` (lines 266–267).
- **Evidence:** the quoted lines. The sale workflow knows the two concrete
  item kinds and how each one validates stock and persists its inventory.
- **Explanation:** The class is not closed for modification: supporting a
  third catalog (for example, bundles or gift cards) means editing four
  methods of `SaleService` in a consistent way, and the accessory stock rule
  and its persistence live in the sales service instead of next to the
  accessory (SRP smell). This is the same root cause as one fragment, so it
  is registered once, under the principle whose fix solves it.
- **Consequence:** Every new item kind risks breaking a sale that works
  today, and the stock rules of products and accessories can drift apart
  (the two messages and checks are already written twice).
- **Proposed solution:** Introduce a small abstraction for "where sale items
  come from", for example `SaleItemSource` with `find(String id)`,
  `validateStock(item, quantity)` and `reduceStock(item, quantity)`, with
  two implementations (`ProductItemSource` over `ProductService`, and
  `AccessoryItemSource` over the accessory repository). `SaleService`
  iterates its list of sources in the same order as today (products first,
  accessories second), so the sale item order, the Spanish messages and the
  data files stay identical.
- **Code of another member:** No for the OCP correction itself (the new
  classes live in my `service` package and use the existing
  `ProductService`). `AccessoryItemSource` becomes cleaner once Developer 2
  provides an `AccessoryRepository` interface (see V-05).

### LSP — Correct

`SaleService` treats `Accessory` as a `Product` without breaking anything:
`sale.addProduct(accessory)` (line 209) and the `Product` API used on
accessories (`getStock`, `getTitle`, `reduceStock`, lines 178–181 and 266)
behave according to the `Product` contract. The `requiresWarranty()` check
(line 239) is a polymorphic call that every `Product` subtype answers
without special cases, so no subtype needs to be treated differently from
its base type.

### ISP — Not applicable

`SaleService` neither declares nor implements an interface (line 31), so no
client is forced to depend on a "fat" interface. It does use only a small
part of each collaborator's API (`ProductService`: `findProductById` and
`reduceStock`; `PersonService`: the two finders; `PromotionService`:
`findBestPromotionFor`; `WarrantyService`: the two `assign*` methods).
Those collaborators are same-layer service classes with a single
implementation and no expected variation, so narrow interfaces for them
would add indirection without a real axis of change; the team did not
consider that a violation.

### DIP — Violation (V-05)

**V-05 — `SaleService` depends on the concrete persistence class
`AccessoryDAO`**
- **Principle:** Dependency Inversion Principle
- **Module:** `SaleService` field and constructor, lines 35 and 48–52
  (`private AccessoryDAO accessoryDAO`), used in `resolveSaleItems` line 176
  (`accessoryDAO.findById`) and `updateInventory` line 267
  (`accessoryDAO.update`).
- **Evidence:** A service-layer class holds a concrete DAO and calls its
  persistence methods directly. For products the same project already solved
  this: `ProductService` depends on the `ProductRepository` interface
  (`ProductService` line 16) and `SaleService` reaches products only through
  `ProductService`. Accessories have no equivalent abstraction. The
  `SaleDAO` side of the same problem is registered in V-03.
- **Explanation:** The use-case class (high level) depends on a file-based
  implementation (low level) instead of an abstraction.
- **Consequence:** The sale registration with accessories cannot be tested
  with an in-memory repository, and a change in `AccessoryDAO` (constructor,
  storage) reaches `SaleService`.
- **Proposed solution:** Create an `AccessoryRepository` interface
  (`findById`, `update`, and the other methods its clients use) implemented
  by `AccessoryDAO`, and have `SaleService` receive it. Behavior is
  unchanged. The other collaborators (`ProductService`, `PersonService`,
  `PromotionService`, `WarrantyService`) are same-layer services and are
  intentionally left as they are; `setWarrantyService` stays because of the
  circular dependency.
- **Code of another member:** **Yes.** `AccessoryDAO` belongs to
  Developer 2 and must implement the new interface (the interface itself can
  be created by Developer 2 or by me in the same PR, as agreed at
  consolidation). The change in `SaleService` and `Main` is mine. Frontier
  finding.
  
---

## 4. UI layer: `MainMenu`, the seven area menus, `MenuDialogs`, `SaleReceiptFormatter`

The area menus are `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`,
`PromotionMenu`, `ReturnMenu` and `WarrantyMenu`. They share the same shape
(constructor receiving one service, a public `show()`, an options array and
a switch), so they are audited together; findings that apply to a specific
one name the class and line.

### 4.1 `MainMenu`

#### SRP — Correct

`MainMenu` has one reason to change: the top-level navigation of the
application. After Requirement 7 it only shows the main options (lines
69–91), delegates to the area menus (lines 94–100) and routes exceptions to
`MenuDialogs` (lines 103–116); the area logic and the dialog code live
elsewhere.

#### OCP — Violation (V-06)

**V-06 — Adding a new menu area requires editing `MainMenu` in three
places (and its constructor builds concrete menus)**
- **Principle:** Open/Closed Principle (related to DIP)
- **Module:** `MainMenu`:
  - Constructor lines 49–60: `new ProductMenu(productService)`,
    `new PersonMenu(personService)`, `new SaleMenu(saleService,
    productService)`, `new AccessoryMenu(...)`, `new PromotionMenu(...)`,
    `new ReturnMenu(...)`, `new WarrantyMenu(...)`; it receives the seven
    services (lines 49–52) only to pass them on, it uses none of them.
  - `options` array, lines 69–78 (labels in a fixed order).
  - `switch (choice)`, lines 93–101: `case 0 -> productMenuView.show()` …
    `case 6 -> warrantyMenuView.show()`, `case 7, -1 -> running = false`.
- **Evidence:** the quoted lines. The position of a label in the array and
  the number of its `case` are coupled by hand, and the high-level class
  knows every concrete menu and every service needed to build it.
- **Explanation:** The class is not closed for modification: a new area
  (for example "Reports") means changing the constructor, the array and the
  switch, and inserting an item in the middle would silently shift every
  later `case`. Because `MainMenu` also instantiates the concrete menus, it
  is a high-level class tied to the details of all of them (DIP smell). It is
  registered once, because a single change solves both.
- **Consequence:** Each new area touches the main navigation class and risks
  breaking the existing ones; the class also cannot be built in a test
  without the whole service layer.
- **Proposed solution:** Create a small interface in the `ui` package (for
  example `AreaMenu` with `String getTitle()` and `void show()`), make the
  seven menus implement it, build them in `Main` (the composition root) and
  pass a `List<AreaMenu>` to `MainMenu`. `MainMenu` builds the option labels
  from the titles, appends "Salir" and opens the selected menu by index.
  Labels, order, exception handling and messages stay identical.
- **Code of another member:** No — `MainMenu`, the seven menus (all
  mine since Requirement 7) and `Main` are in my scope.

#### LSP — Not applicable

`public class MainMenu` (line 27) has no `extends`/`implements` and is not
extended, so there is no substitution relationship.

#### ISP — Not applicable

`MainMenu` declares and implements no interface. After V-06 the new
`AreaMenu` interface has only two methods and every implementer uses both.

#### DIP — Correct (the construction problem is covered by V-06)

Apart from the instantiation reported in V-06, `MainMenu` depends on the
service layer only through the menus (it never touches a DAO or a model
class) and respects the direction `ui → service`.

### 4.2 The seven area menus

#### SRP — Correct

Each menu handles one feature area: for example `SaleMenu` only registers
and lists sales (`registerSale` line 70 to `printSales` line 153), and
`ProductMenu`, `PersonMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`
and `WarrantyMenu` are built the same way (single area per class, constructor
with its own service). Dialog code is delegated to `MenuDialogs`.

#### OCP — Correct

Adding an operation to an area means editing only that area's class (its
options array and switch, for example `SaleMenu` lines 41–67), which is the
one class that is responsible for that area; no other class is touched. The
warranty question is not type-based: `SaleMenu` line 100 asks
`product.requiresWarranty()` instead of checking `instanceof Console`.

#### LSP — Not applicable

None of the seven menus extends a class or is extended (each declared as
`public class XxxMenu` without `extends`), so there is nothing to substitute.
After V-06 they will implement `AreaMenu` identically (`show()` and
`getTitle()`).

#### ISP — Not applicable

They declare and implement no interface today. `SaleMenu` uses a single
method of `ProductService` (`findProductById`, line 99), but `ProductService`
is a class, not an interface, so there is no fat interface to segregate.
After V-06 the seven menus implement `AreaMenu`, which has two methods that
every implementer uses.

#### DIP — Correct

Each menu depends on exactly the service(s) of its own area
(`ProductMenu(ProductService)` line 24, `SaleMenu(SaleService,
ProductService)` line 32, and so on), in the allowed direction
`ui → service`, and none reaches a DAO or a persistence detail. Unlike
`SaleService` (V-05), a menu has one cohesive dependency and no persistence
detail, so no abstraction is needed to protect it.

### 4.3 `MenuDialogs`

- **SRP — Correct.** All members are Swing dialog helpers (`askInput` line
  89, `showList` line 100, `formatEntries` line 120, and five `showXxx`
  error dialogs, lines 34–79). They change for one reason: how dialogs look
  and read to the user.
- **OCP — Correct.** `MainMenu` can receive a new `GameZoneException`
  subtype without edits because `showGenericError` (line 76) and the
  `catch (GameZoneException e)` (MainMenu line 111) already cover it.
- **LSP — Not applicable.** `public final class` (line 20), private
  constructor, no inheritance.
- **ISP — Not applicable.** No interface is declared or implemented.
- **DIP — Correct.** It is a UI helper that uses Swing (`JOptionPane`),
  which is the UI technology itself; it depends on no service,
  persistence or model class, only on the exception types it translates.

### 4.4 `SaleReceiptFormatter`

- **SRP — Correct.** One responsibility: build the receipt text
  (`format(Sale)`, lines 25–44).
- **OCP — Correct.** `Sale` stays closed for presentation changes; the
  receipt layout is changed only here.
- **LSP — Not applicable.** `public final class` (line 11), private
  constructor, no inheritance.
- **ISP — Not applicable.** No interface; it uses only getters of `Sale`.
- **DIP — Correct.** It depends on the model (`Sale`), which is the
  allowed direction `ui → model`. The inverse dependency, `Sale` →
  `SaleReceiptFormatter`, is reported in V-01.