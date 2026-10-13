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