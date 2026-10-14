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

---

## 2. `PersonDAO.java`

### Information Expert — Not a Violation

`PersonDAO` holds the file paths and field separators for both customers
and sellers, so it is the right expert to perform saving and loading for
both. No other class has this information.

### Creator — Not a Violation

`loadCustomers()` and `loadSellers()` create `Customer`/`Seller`
instances from the exact data they just parsed from each line. Per
Creator's criteria, the class holding the initializing data is a
reasonable place to create the object.

### Controller — Not Applicable

`PersonDAO` is a persistence-layer class, not a use-case entry point.

### Low Coupling — Not a Violation

`PersonDAO` depends only on `Customer`, `Seller` and the project's own
exception types — all necessary for its persistence responsibility. It
has no dependency on the service or UI layers.

### High Cohesion — Not a Violation

All four public methods (`saveCustomers`, `loadCustomers`, `saveSellers`,
`loadSellers`) are about one responsibility: reading and writing people
data to text files. Nothing unrelated is mixed in.

### Polymorphism — Not Applicable

`PersonDAO` has no behavior that varies by the concrete type of a
`Person`; it handles `Customer` and `Seller` through separate, explicit
methods rather than branching on type, so there is no conditional to
replace with polymorphism.

### Pure Fabrication — Not a Violation

`PersonDAO` has no direct real-world counterpart; it exists purely to
keep file I/O out of the domain classes and out of `PersonService`. This
is a correct, intentional use of Pure Fabrication.

### Indirection — Not a Violation

`PersonDAO` mediates between the `Person` hierarchy and the file system,
so `PersonService` never touches file I/O directly.

### Protected Variations — Violation (V-02)

**V-02 — Dead `throws IOException` declarations misstate the real failure
contract**
- **Pattern:** Protected Variations
- **Module:** All four public methods of `PersonDAO`
  (`saveCustomers`, `loadCustomers`, `saveSellers`, `loadSellers`) still
  declare `throws IOException` in their signature.
- **Evidence:** Every `catch (IOException e)` block inside these methods
  now wraps the exception and throws `PersistenceException` (unchecked)
  instead of letting `IOException` propagate — e.g. `saveCustomers`:
  `catch (IOException e) { throw new PersistenceException(...); }`. No
  statement in any of these method bodies can still throw a checked
  `IOException` out to the caller.
- **Explanation:** Protected Variations asks that callers be shielded
  behind a stable, accurate interface. A `throws IOException` clause
  that can never actually fire is a stale artifact of the pre-Requirement-6
  implementation: it advertises a checked-exception contract that no
  longer reflects what the method can really do, while the real failure
  mode (`PersistenceException`) is a different, unchecked type that
  callers must discover by reading the body, not the signature.
- **Consequence:** `PersonService` and `MainMenu` are still forced to
  declare/catch `IOException` for calls that can never throw it, adding
  dead exception-handling code and obscuring the exception type that can
  actually occur (`PersistenceException`). A developer reading only the
  method signatures would reasonably — and incorrectly — assume
  `IOException` is still the real failure mode.
- **Proposed solution:** Remove `throws IOException` from all four
  `PersonDAO` methods now that the checked exception is fully wrapped,
  and propagate that signature change to `PersonService` (see finding
  below) and to the corresponding `MainMenu` catch blocks.
- **Other member's code involved:** **Yes.** The matching `MainMenu`
  catch blocks for these calls are the Líder Técnico's scope
  (`ConsoleMenu`/`MainMenu` per TEAM.md); I will flag this so the two
  changes are coordinated together.


---

## 3. `PersonService.java`

### Information Expert — Not a Violation

`PersonService` does not duplicate information it is not entitled to: it
only operates on its own `customers`/`sellers` lists (loaded once via
`PersonDAO`) and delegates data validation to `PersonValidator`, which
owns the validation rules.

### Creator — Not a Violation

`registerCustomer` creates the `Customer` instance after gathering and
validating the exact data needed to initialize it (`name`, `id`,
`phone`, `email`) from its own parameters, and immediately records it in
the list it owns (`customers.add(newCustomer)`). This matches Creator's
criteria: the class that aggregates and records the object is a
reasonable place to create it.

### Controller — Not a Violation

`PersonService` acts as the use-case controller for "register a
customer" and for the person-lookup queries (`findCustomerById`,
`findSellerById`, `listCustomers`, `listSellers`), receiving requests
from `MainMenu` and coordinating `PersonValidator`/`PersonDAO` without
being a UI or persistence class itself.

### Low Coupling — Not a Violation

`PersonService` depends only on `PersonDAO` and `PersonValidator`, both
necessary and directly required to register and validate people. No
dependency on unrelated services or on persistence details beyond the
DAO.

### High Cohesion — Not a Violation

Every method in `PersonService` (`registerCustomer`, `listCustomers`,
`listSellers`, `findCustomerById`, `findSellerById`) is about managing
customers and sellers — the two subtypes of the same `Person` hierarchy
the Taller groups together as one module. No unrelated responsibility is
present.

### Polymorphism — Not Applicable

`PersonService` has no behavior that branches on the concrete type of a
`Person`; `Customer` and `Seller` are handled through separate,
explicitly-typed methods rather than a shared polymorphic call that
would need to vary by type.

### Pure Fabrication — Not Applicable

`PersonService` is itself a Pure Fabrication (a service-layer class with
no direct real-world counterpart), which is appropriate and already
covered under Controller/Low Coupling above; there is no separate
violation to report.

### Indirection — Not a Violation

`PersonService` mediates between `MainMenu` and `PersonDAO`, so
`MainMenu` never touches file persistence directly for person-related
operations.

### Protected Variations — Violation (continues V-02)

`registerCustomer(String, String, String, String)` still declares
`throws IOException`, purely because it calls `personDAO.saveCustomers`,
which carries the same dead declaration described in **V-02** above. The
cause, evidence and consequence are identical — this method cannot
actually throw a checked `IOException` either, since the only internal
source of it (`PersonDAO`) already wraps it as `PersistenceException`.
Removing `throws IOException` from `PersonDAO` (V-02's proposed
solution) must be applied here too, since this signature only exists to
satisfy the one below it.



