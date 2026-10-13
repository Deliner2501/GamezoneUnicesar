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