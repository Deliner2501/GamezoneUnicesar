# Leader GRASP Audit — Deiner Andrés De Luque Navarro

## Scope

As Technical Leader, my audit covers the classes I am responsible for per
`TEAM.md` and the requirement history: `Sale`, `SaleDAO`, `SaleService`,
`MainMenu`, `Main`, and the `restoreStock` method inside `ProductService`
(added for the Returns requirement, Req 3).

---

## 1. `Sale.java`

### Information Expert — Not a Violation

`Sale` owns all the data needed to compute its own total, final total and
receipt (`products`, `total`, `discountAmount`, `additionalCost`), and
exposes `calculateTotal()`, `getFinalTotal()` and `canBeReturned()` as
methods on itself instead of forcing a collaborator to pull its fields out
and compute externally. The class that holds the information performs the
operation.

### Creator — Not Applicable

`Sale` does not create instances of other domain classes; it only holds
references to `Product` objects passed in via `addProduct`. Creator does
not apply here.

### Controller — Not Applicable

`Sale` is a domain/model class, not a use-case entry point. The controller
role for the "register sale" use case belongs to `SaleService`, audited
below.

### Low Coupling — Not a Violation

`Sale` depends only on `Product` (via composition) and standard JDK types
(`LocalDate`, `List`, `ChronoUnit`). It has no dependency on persistence,
service, or UI classes, which keeps it reusable and easy to test in
isolation.

### High Cohesion — Not a Violation

Every method in `Sale` (`addProduct`, `calculateTotal`, `canBeReturned`,
`applyPromotion`, `addAdditionalCost`, `getFinalTotal`, `generateReceipt`)
operates on the sale's own state. There is no unrelated responsibility
mixed in.

### Polymorphism — Not Applicable

`Sale` has no type-varying behavior that branches on type (e.g. no
`if (product instanceof X)` inside `Sale` itself); the `products` list is
handled uniformly via the `Product` type. Polymorphism is not a relevant
concern for this class.

### Pure Fabrication — Not a Violation at class level (see V-01 below)

`Sale` itself is a legitimate domain class (not an artificial one), so
Pure Fabrication does not apply to the class as a whole. However, see
**V-01** below for a method-level concern.

### Indirection — Not Applicable

`Sale` does not mediate between two other components; it is a leaf domain
object. Indirection is not applicable.

### Protected Variations — Violation (V-01)

**V-01 — `generateReceipt()` hard-codes Spanish presentation text inside
the domain model**
- **Pattern:** Protected Variations (related to Pure Fabrication)
- **Module:** `Sale.generateReceipt()` (lines ~190–230, `StringBuilder`
  building a multi-line Spanish receipt with labels like "Cliente:",
  "Cargos adicionales (garantías extendidas)", "TOTAL A PAGAR")
- **Evidence:** The domain class `Sale` is directly responsible for
  formatting human-readable, language- and layout-specific output text.
- **Explanation:** A domain class should represent business data and
  rules, not presentation formatting. By building display strings inside
  `Sale`, any future change to the receipt's wording, layout, or language
  (e.g. supporting English, or switching to HTML/PDF output) forces a
  change to the domain class itself, instead of being isolated behind a
  stable interface.
- **Consequence:** The domain model is not protected from changes in
  presentation concerns — a UI-layer decision (how a receipt looks) leaks
  into the model layer, increasing coupling between layers and making
  `Sale` harder to reuse in a context that needs a different output format
  (e.g. a future REST API or report export).
- **Proposed solution:** Extract receipt formatting into a dedicated Pure
  Fabrication class (e.g. a `SaleReceiptFormatter` in the `ui` or a new
  `report` package) that takes a `Sale` and produces the formatted text,
  leaving `Sale` responsible only for its business data and calculations.
  This keeps `Sale` protected from future presentation changes.
- **Other member's code involved:** No — this is entirely within my own
  scope (`Sale.java`).

  ---

## 2. `SaleDAO.java`

### Information Expert — Not a Violation

`SaleDAO` is the class that holds the file path, field separators and
serialization format (`toLine`/`fromLine`), so it is the right expert to
perform both saving and loading of `Sale` data. No other class has the
information needed to do this job.

### Creator — Not a Violation

`fromLine(String)` creates `Sale` instances from the persisted data it
alone possesses (the raw line and the parsed fields). Per Creator's
criteria, a class that contains the initializing data for an object is a
reasonable place to create it.

### Controller — Not Applicable

`SaleDAO` is a persistence-layer class, not a use-case entry point; it has
no controller responsibility.

### Low Coupling — Not a Violation

`SaleDAO` depends on `PersonDAO` and `ProductDAO`, both needed to resolve
customer, seller and product references when deserializing a line. These
are necessary, direct collaborations for its persistence responsibility
and do not couple it to service or UI layers.

### High Cohesion — Not a Violation

All public and private members (`save`, `findAll`, `findByCustomer`,
`findBySeller`, `toLine`, `fromLine`, `findCustomerById`,
`findSellerById`) are about one responsibility: reading and writing
`Sale` records to the text file. Nothing unrelated is mixed in.

### Polymorphism — Not Applicable

`SaleDAO` has no type-varying behavior driven by conditionals on type; its
only type-based branching is the use of `Product`/`Accessory` resolution
discussed under V-02 below, which is a missing collaboration, not a
polymorphism misuse.

### Pure Fabrication — Not a Violation

`SaleDAO` has no direct real-world counterpart in the problem domain; it
exists purely to keep persistence concerns (file I/O, serialization
format) out of the domain class `Sale` and the service class
`SaleService`. This is a correct, intentional use of Pure Fabrication to
achieve Low Coupling and High Cohesion elsewhere.

### Indirection — Not a Violation

`SaleDAO` mediates between `Sale` (domain) and the file system, so
`SaleService` never touches file I/O directly. This is a correct use of
Indirection to decouple the service layer from the persistence mechanism.

### Protected Variations — Violation (V-02)

**V-02 — Inconsistent exception abstraction between `SaleDAO` methods**
- **Pattern:** Protected Variations
- **Module:** `SaleDAO.findByCustomer(String)` and
  `SaleDAO.findBySeller(String)` declare `throws IOException` directly,
  while `SaleDAO.save(Sale)` and `SaleDAO.findAll()` catch `IOException`
  and rethrow it wrapped as `PersistenceException` (per the Req 6
  exception hierarchy).
- **Evidence:** Compare the `throws` clauses and catch blocks of the four
  methods in `SaleDAO.java`: two wrap the checked exception behind the
  stable `GameZoneException` hierarchy, two do not.
- **Explanation:** Protected Variations asks that callers be shielded
  from a component's internal implementation details behind a stable
  interface. Here, two methods expose the raw `java.io.IOException` (an
  implementation detail of using file-based storage) directly to their
  callers, while the other two correctly hide it behind the project's own
  exception abstraction.
- **Consequence:** Callers of `findByCustomer`/`findBySeller` (currently
  `MainMenu`) must handle a different exception type than callers of
  `save`/`findAll`, even though all four methods fail for the same
  underlying reason (a file I/O problem). If the storage mechanism ever
  changes (e.g. to a database), every caller of these two methods would
  need to change, whereas callers of the other two would not.
- **Proposed solution:** Wrap the `IOException` in `findByCustomer` and
  `findBySeller` as `PersistenceException`, exactly as `save` and
  `findAll` already do, so all four methods present the same stable
  exception contract to their callers.
- **Other member's code involved:** No — all four methods are within
  `SaleDAO.java`, fully in my own scope.

### Note — functional defect identified (out of scope for this requirement)

While auditing `fromLine(String)`, I found that it resolves every item id
in a persisted sale **only** through `productDAO.findById(productId)`,
with no fallback to `AccessoryDAO`. Since `SaleService.registerSale`
allows a sale to contain accessories (resolved via `AccessoryDAO` when the
id is not found as a `Product`), a sale containing an accessory, once
saved and reloaded from `data/sales.txt`, would silently lose that
accessory line item.

This is **not** logged as a GRASP violation here because fixing it would
change observable behavior (accessories would now actually survive a
reload, instead of silently disappearing), and this requirement's scope
is limited to zero-behavior-change refactors. Per the requirement's rules,
this is recorded as an **out-of-scope functional defect** for the
consolidated audit, not something I will correct in Phase 2.

---

## 3. `SaleService.java`

### Information Expert — Not a Violation

`SaleService` does not compute business data it is not entitled to; it
delegates to the class that actually owns each piece of information:
customer/seller resolution to `PersonService`, product/accessory stock
checks to `ProductDAO`/`AccessoryDAO`, discount calculation to
`PromotionService`, and warranty assignment/cost to `WarrantyService`.
It only uses the ids and quantities it directly receives as parameters to
coordinate these calls.

### Creator — Not a Violation

`SaleService.registerSale` creates the `Sale` instance after gathering the
data needed to initialize it (id, date, resolved customer, resolved
seller) from its own parameters and collaborators. Since it aggregates and
owns this initializing data for the use case, it is a reasonable Creator
for `Sale`.

### Controller — Not a Violation

`SaleService` acts as the use-case controller for "register a sale" and
related sale queries: it receives the request (ids, quantities), delegates
to the appropriate experts, and coordinates the overall operation without
being a UI or persistence class itself. This matches GRASP's "use-case
controller" variant and keeps `MainMenu` free of business logic.

### Low Coupling — Not a Violation

`SaleService` depends on six collaborator types (`SaleDAO`, `ProductDAO`,
`AccessoryDAO`, `PromotionService`, `WarrantyService`, `PersonService`),
all of which are necessary to perform the "register sale" use case — none
is an incidental or avoidable dependency. Notably, `WarrantyService` is
injected via a setter (`setWarrantyService`) instead of the constructor,
specifically to break a circular dependency with `WarrantyRepository`
(which itself depends on `SaleService`). This is evidence of deliberate,
well-managed coupling rather than accidental coupling.

### High Cohesion — Violation (V-03)

**V-03 — `registerSale` concentrates too many responsibilities in one
method**
- **Pattern:** High Cohesion
- **Module:** `SaleService.registerSale(saleId, date, customerId,
  sellerId, productQuantities, productIdsWithExtendedWarranty)`
- **Evidence:** In a single method body, `registerSale` (1) resolves the
  customer and seller, (2) resolves and validates stock for every product
  and accessory id, (3) runs `SaleValidator.validateSaleData`, (4) builds
  the `Sale` and adds its items, (5) finds and applies the best
  `Promotion`, (6) assigns basic/extended warranties and sums their cost,
  (7) adds that cost to the sale, (8) updates product/accessory stock,
  and (9) persists the sale and updates the customer's purchase history —
  nine distinct steps in one method.
- **Explanation:** High Cohesion asks that a unit's responsibilities be
  strongly related and limited in number. While all nine steps belong to
  the same use case, packing all of them into a single method makes the
  method's responsibility too broad: it mixes orchestration of six
  different collaborators and read poorly as "one thing the method does."
- **Consequence:** The method is harder to read, test in isolation (each
  step cannot be unit-tested on its own), and extend — adding a new step
  to the sale workflow (e.g. a loyalty-points calculation) means editing
  an already large method rather than composing a new step.
- **Proposed solution:** Extract each logical step into a small private
  helper method within `SaleService` (e.g. `resolveParties`,
  `resolveSaleItems`, `applyBestPromotion`, `assignWarranties`), and have
  `registerSale` read as a short sequence of calls to those helpers. This
  keeps the same public behavior and signature while raising the
  method-level cohesion.
- **Other member's code involved:** No — entirely within
  `SaleService.java`.

### Polymorphism — Violation (V-04)

**V-04 — Explicit `instanceof Console` check to decide warranty
assignment**
- **Pattern:** Polymorphism
- **Module:** `SaleService.registerSale`, the warranty-assignment step
  (iterates the sale's products and checks whether each one is a
  `Console` before assigning a warranty through `WarrantyService`)
- **Evidence:** The warranty-assignment logic branches explicitly on the
  product's concrete type (`Console` vs. other `Product` subtypes) instead
  of asking the product itself whether it requires a warranty.
- **Explanation:** Polymorphism states that when behavior varies by type,
  that variation should be handled by polymorphic methods on the type
  hierarchy, not by explicit type-checking conditionals in a client class.
  Here, `SaleService` needs to know about the existence and behavior of
  the `Console` subtype directly.
- **Consequence:** Every time a new product type that also needs
  warranties is introduced (or an existing type's warranty eligibility
  changes), `SaleService` itself has to be modified, instead of the change
  being isolated to the `Product` hierarchy. This also makes `SaleService`
  depend on a concrete subtype (`Console`) rather than only on the
  `Product` abstraction, weakening the abstraction boundary with
  Developer 1's class hierarchy.
- **Proposed solution:** Add a method such as `requiresWarranty()` (or
  `isWarrantyEligible()`) to `Product`, overridden to return `true` in
  `Console` and `false` elsewhere (or, if other product types later need
  this, overridden per subtype). `SaleService` would then call
  `product.requiresWarranty()` instead of checking `instanceof Console`.
- **Other member's code involved:** **Yes.** `Product` and `Console` are
  part of Developer 1's audit scope (per `TEAM.md`), so this fix requires
  Developer 1's class hierarchy to add the new method. I will flag this to
  them so it is coordinated, but the call site being corrected
  (`SaleService`) is my own responsibility.

### Pure Fabrication — Not Applicable

`SaleService` is itself a Pure Fabrication (a service-layer class with no
direct real-world counterpart), which is appropriate and already covered
under Controller/Low Coupling above; there is no separate violation to
report here.

### Indirection — Not a Violation

`SaleService` mediates between `MainMenu` and the lower layers
(`SaleDAO`, `ProductDAO`, `AccessoryDAO`, `PromotionService`,
`WarrantyService`, `PersonService`), so `MainMenu` never talks to
persistence or other services directly for sale-related operations. This
is a correct use of Indirection.

### Protected Variations — Not a Violation

Callers of `SaleService` depend only on its public method signatures and
on the typed exception hierarchy (`ResourceNotFoundException`,
`BusinessRuleException`, `InvalidDataException`), not on the internal
details of how promotions or warranties are computed. Changes to those
internals do not ripple out to `MainMenu`.

---

## 4. `MainMenu.java`

### Information Expert — Not a Violation

`MainMenu` never computes business results itself; it only collects raw
input (strings from `askInput`) and hands them to the appropriate service
(`personService`, `productService`, `accessoryService`, `promotionService`,
`warrantyService`, `returnService`, `saleService`). It does not duplicate
logic that already belongs to those experts.

### Creator — Violation (V-05)

**V-05 — UI layer directly instantiates domain objects**
- **Pattern:** Creator
- **Module:** `MainMenu.registerVideoGame()` and
  `MainMenu.registerConsole()` — both build the domain object with
  `new VideoGame(...)` / `new Console(...)` directly, then pass it to
  `productService.registerProduct(...)`.
- **Evidence:** `Product videoGame = new VideoGame(id, title, price,
  stock, platform, genre, ageRating); productService.registerProduct(
  videoGame);` and the equivalent for `Console`.
- **Explanation:** Creator says a class B should create instances of
  class A when B aggregates, contains, or records A — none of which is
  true for `MainMenu` with respect to `VideoGame`/`Console`. The class
  that actually owns and records products is `ProductService` (via
  `ProductDAO`), so it is the more appropriate Creator.
- **Consequence:** The UI layer is coupled to the exact construction
  signature of every product subtype. Any change to how a `VideoGame` or
  `Console` is built (new required field, validation moved to the
  constructor, etc.) forces a change in `MainMenu`, a layer that should
  only be concerned with collecting input and displaying output.
- **Proposed solution:** Add a factory-style registration method to
  `ProductService` (e.g. `registerVideoGame(id, title, price, stock,
  platform, genre, ageRating)` and `registerConsole(...)`) that builds the
  object internally and registers it, so `MainMenu` only passes raw field
  values and never constructs the domain object itself.
- **Other member's code involved:** **Yes.** `ProductService` belongs to
  Developer 1's scope, so adding these factory methods needs to be
  coordinated with them; the call-site change in `MainMenu` is mine.

### Controller — Not a Violation

`MainMenu.start()` and each submenu method act as the UI-level
controller, receiving the user's menu choice and delegating immediately
to the service layer. In this project's simple layered architecture
(no separate MVC framework), it is expected and consistent for the
console/Swing class to also play this role.

### Low Coupling — Not a Violation

`MainMenu` depends on all seven services (`PersonService`,
`ProductService`, `AccessoryService`, `PromotionService`,
`WarrantyService`, `ReturnService`, `SaleService`). This is necessary and
expected, since `MainMenu` is the single UI entry point that must expose
every feature of the system; it is not coupled to anything beyond what
each feature requires (no direct DAO or persistence dependency anywhere).

### High Cohesion — Violation (V-06)

**V-06 — `MainMenu` concentrates the UI for all seven unrelated feature
areas in a single class**
- **Pattern:** High Cohesion
- **Module:** `MainMenu.java` as a whole (1100+ lines): products,
  people, sales, accessories, promotions, returns and warranties each
  have their own full submenu, input-collection and listing methods, all
  inside one class.
- **Evidence:** Seven independent `*Menu()` methods (`productMenu`,
  `personMenu`, `saleMenu`, `accessoryMenu`, `promotionMenu`,
  `returnMenu`, `warrantyMenu`) plus ~35 supporting private methods, all
  members of the same class.
- **Explanation:** High Cohesion asks a class's responsibilities to be
  strongly related. While every method here is "UI-related," the actual
  business areas they serve (inventory, people, sales, accessories,
  promotions, returns, warranties) are largely independent of each other.
  Grouping all of them into one class mixes seven loosely related
  responsibilities under a single name.
- **Consequence:** The class is hard to navigate and maintain (1100+
  lines), merge conflicts between team members become more likely since
  everyone edits the same file, and a change to one feature's menu (e.g.
  promotions) carries some risk of accidentally affecting unrelated code
  in the same class.
- **Proposed solution:** Split `MainMenu` into one menu class per feature
  area (e.g. `ProductMenu`, `PersonMenu`, `SaleMenu`,
  `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu`), each
  depending only on the service(s) it needs, with a slim `MainMenu` left
  to show the top-level menu and delegate to the right one. The shared
  helpers (`askInput`, `showList`, `formatEntries`) would move to a small
  shared UI-helper class.
- **Other member's code involved:** No — this split is entirely within
  `MainMenu.java`, which is fully my own responsibility.

### Polymorphism — Violation (V-07, related to V-04)

**V-07 — Explicit `instanceof Console` check to decide whether to offer
an extended warranty**
- **Pattern:** Polymorphism
- **Module:** `MainMenu.registerSale()`, inside the item-collection loop:
  `Product product = productService.findProductById(productId); if
  (product instanceof Console) { /* ask about extended warranty */ }`
- **Evidence:** The UI branches on the product's concrete type to decide
  whether to show the "¿Agregar garantía extendida?" confirmation dialog.
- **Explanation:** This is the same root issue as **V-04** in
  `SaleService`: the system asks "is this a `Console`?" via `instanceof`
  instead of asking the product itself whether it supports warranties.
- **Consequence:** Same as V-04 — adding a new product type that should
  also offer warranties requires editing this UI code directly, and
  `MainMenu` depends on the concrete `Console` subtype instead of only on
  the `Product` abstraction.
- **Proposed solution:** Once `Product.requiresWarranty()` is added (per
  V-04's solution), replace `product instanceof Console` here with
  `product.requiresWarranty()`. Both V-04 and V-07 share this single fix
  on the `Product` hierarchy.
- **Other member's code involved:** **Yes**, same as V-04 — depends on
  Developer 1 adding the method to `Product`/`Console`.

### Pure Fabrication — Not Applicable

`MainMenu` is a legitimate UI/view class with a real counterpart in the
problem (the system's user interface), not an artificial class invented
purely to centralize an unrelated responsibility. Pure Fabrication does
not apply as a judgment here.

### Indirection — Not a Violation

`MainMenu` never accesses persistence directly; every operation goes
through a service. It correctly mediates only between user input/output
and the service layer.

### Protected Variations — Not a Violation

`MainMenu.start()` catches the stable `GameZoneException` hierarchy
(`ResourceNotFoundException`, `BusinessRuleException`,
`InvalidDataException`, `PersistenceException`, with a generic
`GameZoneException` fallback) rather than reacting to specific internal
causes. This correctly shields the UI from needing to know about every
possible internal failure mode, and a new exception subtype added later
would still be caught by the generic handler without changing `MainMenu`.

---

## 5. `Main.java`

`Main` is the application's composition root: it builds every DAO and
service, wires their dependencies (including the `setWarrantyService`
setter-injection that breaks the `SaleService`/`WarrantyRepository`
circular dependency), and starts `MainMenu` via
`SwingUtilities.invokeLater`.

### Information Expert — Not Applicable
`Main` holds no business data; it only assembles other objects.

### Creator — Not a Violation
`Main` is the single place responsible for creating the full object graph
(DAOs → services → `MainMenu`). Centralizing this in one composition-root
class is the expected, standard way to wire a layered application without
a DI framework, and keeps every other class free of construction logic
for its own dependencies.

### Controller — Not Applicable
`Main.main()` is the application entry point, not a use-case controller.

### Low Coupling — Not a Violation
`Main` necessarily references nearly every class in the system, but this
is intrinsic to its one and only responsibility (wiring); no other class
in the project is coupled to this many types.

### High Cohesion — Not a Violation
`main()` does exactly one thing: build the dependency graph and launch the
UI. There is no unrelated logic mixed in.

### Polymorphism — Not Applicable
No type-varying behavior is present.

### Pure Fabrication — Not a Violation
`Main` is itself a Pure Fabrication (a composition root with no real-world
counterpart), and this is the correct, intentional use of the pattern to
keep construction logic out of every domain/service/UI class.

### Indirection — Not Applicable
`Main` runs once at startup; it is not a runtime mediator between two
other components.

### Protected Variations — Not a Violation
`Main` catches the single checked exception its DAO constructors declare
(`IOException`) and shows a generic fatal-error dialog via `JOptionPane`,
without depending on any DAO's internal failure details.

**Conclusion: no violations found in `Main.java`.**

---

## 6. `ProductService.restoreStock(String productId, int quantity)`

Scope note: only this method is mine to audit; the rest of
`ProductService` belongs to Developer 1 per `TEAM.md`.

```java
public void restoreStock(String productId, int quantity) {
    Product product = productDAO.findById(productId);
    ProductValidator.validateProductExists(product, productId);
    product.increaseStock(quantity);
    productDAO.update(product);
}
```

### Information Expert — Not a Violation
Stock is actually changed by `product.increaseStock(quantity)` — the
`Product` itself, which owns the stock field, performs the mutation.
`ProductService` only coordinates: fetch, validate, delegate, persist.

### Creator — Not Applicable
No object is created in this method.

### Controller — Not a Violation
`restoreStock` is a reasonable, small service-layer operation for the
"restore stock after a return" step of the Returns use case.

### Low Coupling — Not a Violation
Depends only on `productDAO` and `ProductValidator`, both necessary and
already used consistently elsewhere in the class.

### High Cohesion — Not a Violation
The method does one thing (restore a product's stock) in four short,
related steps.

### Polymorphism — Not Applicable
No type-based branching.

### Pure Fabrication — Not Applicable
Not relevant to a single method's audit; `ProductService` as a whole is
Developer 1's scope.

### Indirection — Not a Violation
Mediates between the Returns workflow (`ReturnRepository`/`ReturnService`)
and `ProductDAO`, so the returns flow never touches persistence directly
for this operation.

### Protected Variations — Not a Violation
Uses `ProductValidator.validateProductExists`, consistent with the
typed-validation pattern already established (Req 6) across the codebase,
instead of an ad hoc null check.

**Conclusion: no violations found in `ProductService.restoreStock`.**

---

## Coverage Summary

| Module                              | Patterns audited | Violations found |
|--------------------------------------|:-----------------:|:-----------------:|
| `Sale.java`                          | 9/9                | 1 (V-01)           |
| `SaleDAO.java`                       | 9/9                | 1 (V-02)           |
| `SaleService.java`                   | 9/9                | 2 (V-03, V-04)     |
| `MainMenu.java`                      | 9/9                | 3 (V-05, V-06, V-07)|
| `Main.java`                          | 9/9                | 0                  |
| `ProductService.restoreStock`        | 9/9                | 0                  |
| **Total**                            | **—**              | **7**              |

## Verdict Table by GRASP Pattern (Leader's scope only)

| Pattern              | `Sale` | `SaleDAO` | `SaleService` | `MainMenu` | `Main` | `restoreStock` |
|-----------------------|:------:|:---------:|:-------------:|:----------:|:------:|:---------------:|
| Information Expert     | Correct | Correct | Correct | Correct | N/A | Correct |
| Creator                | N/A | Correct | Correct | **Violation (V-05)** | Correct | N/A |
| Controller              | N/A | N/A | Correct | Correct | N/A | Correct |
| Low Coupling            | Correct | Correct | Correct | Correct | Correct | Correct |
| High Cohesion           | Correct | Correct | **Violation (V-03)** | **Violation (V-06)** | Correct | Correct |
| Polymorphism            | N/A | N/A | **Violation (V-04)** | **Violation (V-07)** | N/A | N/A |
| Pure Fabrication        | Correct (class-level); Violation at method-level, see V-01 | Correct | N/A | N/A | Correct | N/A |
| Indirection              | N/A | Correct | Correct | Correct | N/A | Correct |
| Protected Variations     | **Violation (V-01)** | **Violation (V-02)** | Correct | Correct | Correct | Correct |

## Violations Requiring Another Member's Code

Per the audit above, 3 of the 7 violations found in my scope cannot be
fully corrected without a change in Developer 1's code:

| ID   | Module (mine)     | Needs change in            | Owner        |
|------|--------------------|------------------------------|--------------|
| V-04 | `SaleService`       | `Product`/`Console` (add `requiresWarranty()`) | Developer 1 |
| V-05 | `MainMenu`          | `ProductService` (add factory-style registration methods) | Developer 1 |
| V-07 | `MainMenu`          | `Product`/`Console` (same method as V-04) | Developer 1 |

These three are flagged for the consolidated audit's "frontier findings"
section, to be coordinated with Developer 1 before Phase 2 correction
starts on my side.

## Out-of-Scope Functional Defects

| Found in       | Description                                                                 |
|------------------|-------------------------------------------------------------------------------|
| `SaleDAO.fromLine` | Resolves sale items only via `ProductDAO`, with no `AccessoryDAO` fallback. A sale containing an accessory silently loses that item when reloaded from `data/sales.txt`. Not corrected here because fixing it changes observable behavior, which is out of scope for a zero-behavior-change GRASP refactor. |

## Summary

My audit covers 5 classes and 1 method (`Sale`, `SaleDAO`, `SaleService`,
`MainMenu`, `Main`, `ProductService.restoreStock`), all 9 GRASP patterns
applied to each. I found **7 violations** (V-01 through V-07) and **1
out-of-scope functional defect**. `Main.java` and
`ProductService.restoreStock` had no violations. 3 of the 7 violations
require coordinated changes in Developer 1's code and are flagged above
for the team's consolidated audit.