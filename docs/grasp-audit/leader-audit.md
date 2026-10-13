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