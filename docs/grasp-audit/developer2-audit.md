# Developer 2 - GRASP Audit

## Scope

This audit covers the classes and methods I developed across the project,
organized by module and requirement, following the responsibility table
in TEAM.md:

- **People (Workshop 1):** `Person`, `Customer`, `Seller`, `PersonRepository`, `PersonService`
- **Accessories (Requirement 1):** `AccessoryRepository`, `AccessoryService`
- **Promotions (Requirement 2):** `PromotionRepository`, `PromotionService`
- **Returns (Requirement 3):** `ReturnRepository`, `ReturnService` (including the monthly balance method)
- **Warranties (Requirement 4):** `WarrantyRepository`, `WarrantyService`
- **Exceptions and validations (Requirement 6):** use of custom exceptions and validators across the services and repositories listed above

---

## 1. `Person` / `Customer` / `Seller` hierarchy

### Information Expert — Not a Violation

Each class manages only the data it owns: `Person` exposes `name`, `id`
and `phone`; `Customer` adds `email` and its own `purchaseHistory`;
`Seller` adds `employeeCode` and `workShift`. No class reaches into
another to compute something it is not responsible for.

### Creator — Not Applicable

Neither `Person`, `Customer` nor `Seller` creates instances of other
domain classes (Customer only holds `Sale` references passed in via
`addPurchase`). Creator does not apply to this hierarchy itself; who
creates `Customer`/`Seller` instances is addressed in the `PersonDAO`
and `PersonService` sections below.

### Controller — Not Applicable

These are domain/model classes, not use-case entry points.

### Low Coupling — Not a Violation

`Person` has no outside dependencies. `Customer` depends only on `Sale`
(a necessary association for purchase history). `Seller` has no outside
dependencies. None of these couple the hierarchy to persistence, service
or UI layers.

### High Cohesion — Not a Violation

Every method in each class operates on that class's own state
(`getName`/`getId`/`getPhone` in `Person`; `getEmail`/`addPurchase` in
`Customer`; `getEmployeeCode`/`getWorkShift` in `Seller`). No unrelated
responsibility is mixed in.

### Polymorphism — Not a Violation

`Customer` and `Seller` each override `toString()` to add their own
fields on top of `super.toString()`, instead of `Person` using an
`instanceof` check or a type flag to decide what to print. This is a
correct use of polymorphic overriding.

### Pure Fabrication — Not Applicable

All three are legitimate domain classes with a direct real-world
counterpart (a person, a customer, a seller).

### Indirection — Not Applicable

These are leaf domain objects; they do not mediate between two other
components.

### Protected Variations — Violation (V-01)

**V-01 — `Customer.getPurchaseHistory()` exposes the live internal list**
- **Pattern:** Protected Variations
- **Module:** `Customer.getPurchaseHistory()`
- **Evidence:** The method returns the field `purchaseHistory` directly:
  `public List<Sale> getPurchaseHistory() { return purchaseHistory; }`,
  with no defensive copy or unmodifiable wrapper — unlike
  `PersonService.listCustomers()`/`listSellers()`, which correctly return
  `Collections.unmodifiableList(...)`.
- **Explanation:** Protected Variations asks that an object's internal
  representation be shielded from external changes. By returning the
  actual mutable list instance, any caller holding the reference can add,
  remove or clear sale entries directly (`customer.getPurchaseHistory().clear()`),
  bypassing `addPurchase` entirely and breaking the class's own invariant
  that purchases are only added through that method.
- **Consequence:** A customer's purchase history can be silently
  corrupted from outside the class, with no way for `Customer` to detect
  or prevent it. This also makes the class harder to reason about in
  isolation, since its state is not actually protected by its own API.
- **Proposed solution:** Return an unmodifiable view instead, consistent
  with the rest of the project:
  `return Collections.unmodifiableList(purchaseHistory);`
- **Other member's code involved:** No — entirely within my own scope
  (`Customer.java`).


