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


---

## 4. `AccessoryDAO.java`

### Information Expert — Not a Violation

`AccessoryDAO` holds the file path and the CSV format for the three
accessory subtypes, so it is the right expert to perform saving and
loading. No other class has this information.

### Creator — Not a Violation

`parseLine` creates `Controller`/`Cable`/`Memory` instances from the
exact fields it just parsed from the line, matching Creator's criteria.

### Controller — Not Applicable

`AccessoryDAO` is a persistence-layer class, not a use-case entry point.

### Low Coupling — Not a Violation

`AccessoryDAO` depends on `Accessory` and its three subtypes
(`Controller`, `Cable`, `Memory`), all necessary to read and write the
CSV format correctly. It has no dependency on the service or UI layers.

### High Cohesion — Not a Violation

All public and private members (`save`, `findAll`, `findById`, `update`,
`writeAll`, `parseLine`, `applyCompatibleConsoles`) are about one
responsibility: reading and writing accessory records.

### Polymorphism — Violation (V-03)

**V-03 — `writeAll()` uses `instanceof` checks instead of polymorphic
serialization**
- **Pattern:** Polymorphism
- **Module:** `AccessoryDAO.writeAll(List<Accessory>)`
- **Evidence:** `if (a instanceof Controller) {...} else if (a instanceof
  Cable) {...} else if (a instanceof Memory) {...}`, each branch manually
  building the CSV line for that subtype.
- **Explanation:** At this point `writeAll` already has a real `Accessory`
  object in hand (not just raw text), so the subtype-specific formatting
  is exactly the kind of type-varying behavior Polymorphism says should
  be handled by a method on the type hierarchy, not by a conditional in
  the client class.
- **Consequence:** Every time a new accessory subtype is added, this
  method must be edited with another `instanceof` branch, and
  `AccessoryDAO` must know the internal fields of every subtype
  (`Controller`, `Cable`, `Memory`) instead of depending only on the
  `Accessory` abstraction.
- **Proposed solution:** Add a method such as `toCsvLine()` to
  `Accessory` (abstract), overridden by each subtype to produce its own
  comma-separated fields, including the type discriminator. `writeAll`
  would then just call `a.toCsvLine()` for every accessory, with no
  `instanceof` check.
- **Other member's code involved:** **Yes.** `Accessory`, `Controller`,
  `Cable` and `Memory` belong to Developer 1's scope, so adding this
  method needs to be coordinated with them; the call site in
  `AccessoryDAO.writeAll` is my own responsibility.

Note: `parseLine`'s type-string check (`"CONTROLLER"`/`"CABLE"`/`"MEMORY"`)
is **not** flagged here — at that point there is no `Accessory` object
yet to dispatch on, so a discriminated factory step is unavoidable when
parsing raw text. This is a legitimate use of Creator-style construction,
not a Polymorphism violation.

### Pure Fabrication — Not a Violation

`AccessoryDAO` has no direct real-world counterpart; it exists purely to
keep file I/O out of the domain and service layers.

### Indirection — Not a Violation

`AccessoryDAO` mediates between the `Accessory` hierarchy and the file
system, so `AccessoryService` never touches file I/O directly.

### Protected Variations — Violation (V-04)

**V-04 — `AccessoryDAO` silently swallows `IOException` instead of using
the project's exception hierarchy**
- **Pattern:** Protected Variations
- **Module:** `findAll()` (both the inner per-line `catch (Exception e)`
  and the outer `catch (IOException e)`) and `writeAll()`'s
  `catch (IOException e)`.
- **Evidence:** All three catch blocks call
  `System.out.println(...)` and continue, instead of raising an
  exception — e.g. `catch (IOException e) { System.out.println("Error
  reading accessories: " + e.getMessage()); }`.
- **Explanation:** As of Requirement 6, `ProductDAO`, `PersonDAO` and
  `SaleDAO` all wrap `IOException` as the project's own
  `PersistenceException`, giving every caller one stable, typed failure
  contract regardless of which DAO it calls. `AccessoryDAO` was not
  included in that refactor and still exposes the old behavior: a read
  or write failure is silently printed to the console and the caller
  receives an empty or partial list with no indication anything went
  wrong.
- **Consequence:** `AccessoryService`/`MainMenu` cannot detect or react
  to a persistence failure for accessories the way they can for
  products, people or sales — the system's own exception contract is
  inconsistent across DAOs, breaking the "callers depend on one stable
  interface" guarantee Protected Variations asks for.
- **Proposed solution:** Apply the same pattern used in `ProductDAO`:
  wrap each `catch (IOException e)` to throw
  `new PersistenceException("...", e)` instead of printing, with file
  path and operation (read/write) in the message. The inner corrupted-line
  catch can keep skipping that one line (that part is a reasonable
  resilience choice), but should still report the skip through a
  mechanism callers can observe if needed, rather than only `System.out`.
- **Other member's code involved:** No — entirely within
  `AccessoryDAO.java`.


---

## 5. `AccessoryService.java`

### Information Expert — Not a Violation

`AccessoryService` does not duplicate data; it only uses the id/price
fields it directly receives, and delegates all persistence to
`AccessoryDAO`.

### Creator — Not a Violation

`registerController`, `registerCable` and `registerMemory` each create
their respective `Accessory` subtype from exactly the data passed in as
parameters, and immediately record it via `accessoryDAO.save(...)`.
This matches Creator's criteria.

### Controller — Not a Violation

`AccessoryService` is the use-case controller for every accessory
operation (register, check/update/restore stock, register compatibility,
list and search), receiving requests from `MainMenu`/`SaleService` and
coordinating `AccessoryDAO` without being a UI or persistence class.

### Low Coupling — Not a Violation

`AccessoryService` depends only on `AccessoryDAO` and the `Accessory`
hierarchy, all necessary for its responsibilities.

### High Cohesion — Not a Violation

Every method concerns managing accessories (registration, stock,
compatibility, queries). No unrelated responsibility is present.

### Polymorphism — Violation (V-05, related to V-03)

**V-05 — `matchesType()` uses a type-string switch with `instanceof`
instead of asking the accessory its own type**
- **Pattern:** Polymorphism
- **Module:** `AccessoryService.matchesType(Accessory, String)`, used by
  `listAccessoriesByType`
- **Evidence:** `switch (type.toUpperCase()) { case "CONTROLLER" ->
  accessory instanceof Controller; case "CABLE" -> accessory instanceof
  Cable; case "MEMORY" -> accessory instanceof Memory; ... }`
- **Explanation:** Same root issue as **V-03**: behavior that depends on
  an accessory's concrete type is handled by a conditional in the
  service instead of a polymorphic method on the `Accessory` hierarchy.
- **Consequence:** Adding a new accessory subtype requires editing this
  method as well as `AccessoryDAO.writeAll` (V-03), doubling the places
  that must change together and risking them drifting out of sync.
- **Proposed solution:** Once `Accessory` exposes a type discriminator
  (e.g. a `getTypeCode()` method returning `"CONTROLLER"`/`"CABLE"`/
  `"MEMORY"`, the same value used in V-03's `toCsvLine()`), replace this
  switch with `accessory.getTypeCode().equalsIgnoreCase(type)`.
- **Other member's code involved:** **Yes**, same as V-03 — depends on
  Developer 1 adding the type discriminator to the `Accessory` hierarchy.

### Pure Fabrication — Violation (V-06)

**V-06 — Accessory validation is a private method instead of a reusable
`AccessoryValidator`**
- **Pattern:** Pure Fabrication
- **Module:** `AccessoryService.validateAccessory(Accessory)` (private)
- **Evidence:** The project already has dedicated validator classes for
  every other domain (`ProductValidator`, `PersonValidator`,
  `SaleValidator`), each a Pure Fabrication with no private-method
  equivalent inside its service. `AccessoryService` instead keeps its
  validation logic as a private method mixed into the service itself.
- **Explanation:** Pure Fabrication recommends placing a
  cross-cutting responsibility like validation in its own class when
  keeping it inside the service would reduce cohesion or reusability.
  The rest of the project already follows this pattern; `AccessoryService`
  is the odd one out.
- **Consequence:** `validateAccessory`'s rules cannot be reused or unit
  tested independently of `AccessoryService`, and the inconsistency makes
  the codebase harder to navigate — a developer looking for accessory
  validation rules in `validation.AccessoryValidator` (by analogy with
  the other domains) would not find it.
- **Proposed solution:** Extract `validateAccessory`'s logic into a new
  `AccessoryValidator` class in `com.gamezone.validation`, following the
  same static-utility-class shape as `ProductValidator`.
- **Other member's code involved:** No — this is a new class I would
  introduce myself; it does not require changes outside my own scope.

### Indirection — Not a Violation

`AccessoryService` mediates between its callers and `AccessoryDAO`, so
callers never touch file persistence directly.

### Protected Variations — Violation (V-07)

**V-07 — `AccessoryService` exposes raw `IllegalArgumentException`
instead of the project's typed exception hierarchy**
- **Pattern:** Protected Variations
- **Module:** `registerController`/`registerCable`/`registerMemory`
  (via `validateAccessory`), `registerCompatibility`, `updateStock`,
  `restoreStock` — all throw plain `IllegalArgumentException`.
- **Evidence:** E.g. `updateStock`: `throw new
  IllegalArgumentException("Accessory not found: " + accessoryId);`
  compare to `ProductService.restoreStock`, which throws
  `ResourceNotFoundException`/`InvalidDataException` from the
  `com.gamezone.exceptions` hierarchy introduced in Requirement 6.
- **Explanation:** The rest of the service layer was migrated to a
  stable, typed exception contract (`ResourceNotFoundException`,
  `InvalidDataException`, `BusinessRuleException`) specifically so
  callers (`MainMenu`) could depend on one consistent hierarchy instead
  of generic JDK exceptions. `AccessoryService` was left out of that
  migration.
- **Consequence:** `MainMenu`'s differentiated catch blocks (built for
  `GameZoneException` and its subtypes) do not apply to accessory
  operations; a caller must still catch generic
  `IllegalArgumentException` separately for this one service, breaking
  the uniform error-handling contract the rest of the system now relies
  on.
- **Proposed solution:** Replace each `IllegalArgumentException` with
  the matching typed exception: `ResourceNotFoundException("accesorio",
  accessoryId)` where an accessory is not found, `InvalidDataException`
  for invalid input (id, price, quantity). This pairs naturally with
  V-06, since the new `AccessoryValidator` would be the class that
  throws these.
- **Other member's code involved:** No — entirely within
  `AccessoryService.java`.



