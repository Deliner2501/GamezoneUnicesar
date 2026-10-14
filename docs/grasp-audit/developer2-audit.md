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


---

## 6. `PromotionDAO.java`

### Information Expert — Not a Violation

`PromotionDAO` holds the file path and the CSV format for the three
promotion subtypes, so it is the right expert to perform saving and
loading.

### Creator — Not a Violation

`parseLine` creates `PercentageDiscount`/`CategoryDiscount`/
`BulkPurchaseDiscount` instances from the exact fields it just parsed,
matching Creator's criteria.

### Controller — Not Applicable

`PromotionDAO` is a persistence-layer class, not a use-case entry point.

### Low Coupling — Not a Violation

`PromotionDAO` depends on `Promotion` and its three subtypes, all
necessary to read and write the CSV format correctly.

### High Cohesion — Not a Violation

`saveAll`, `loadAll` and `parseLine` are all about one responsibility:
reading and writing promotion records.

### Polymorphism — Violation (V-08, same root cause as V-03)

**V-08 — `saveAll()` uses `instanceof` checks instead of polymorphic
serialization**
- **Pattern:** Polymorphism
- **Module:** `PromotionDAO.saveAll(List<Promotion>)`
- **Evidence:** `if (p instanceof PercentageDiscount) {...} else if (p
  instanceof CategoryDiscount) {...} else if (p instanceof
  BulkPurchaseDiscount) {...}`, each branch manually building the CSV
  line.
- **Explanation:** Same issue as V-03 in `AccessoryDAO`: `saveAll`
  already has a real `Promotion` object, so subtype-specific formatting
  should be handled by a polymorphic method on the hierarchy rather than
  a type-check conditional in the DAO.
- **Consequence:** Every new promotion type requires another
  `instanceof` branch here, and `PromotionDAO` must know every subtype's
  internal fields instead of depending only on `Promotion`.
- **Proposed solution:** Add a `toCsvLine()` method to `Promotion`
  (abstract), overridden by each subtype, so `saveAll` just calls
  `p.toCsvLine()` for every promotion.
- **Other member's code involved:** **Yes.** `Promotion`,
  `PercentageDiscount`, `CategoryDiscount` and `BulkPurchaseDiscount`
  belong to Developer 1's scope; the call site in
  `PromotionDAO.saveAll` is my own responsibility.

As with V-03, `parseLine`'s type-string check is not flagged — no
`Promotion` object exists yet to dispatch on when parsing raw text.

### Pure Fabrication — Not a Violation

`PromotionDAO` has no direct real-world counterpart; it exists purely to
keep file I/O out of the domain and service layers.

### Indirection — Not a Violation

`PromotionDAO` mediates between the `Promotion` hierarchy and the file
system, so `PromotionService` never touches file I/O directly.

### Protected Variations — Violation (V-09, same root cause as V-04)

**V-09 — `PromotionDAO` silently swallows `IOException` instead of using
the project's exception hierarchy**
- **Pattern:** Protected Variations
- **Module:** `saveAll()`'s `catch (IOException e)`, and `loadAll()`'s
  inner per-line `catch (Exception e)` and outer `catch (IOException e)`.
- **Evidence:** `catch (IOException e) { System.out.println("Error
  saving promotions: " + e.getMessage()); }`, with no exception
  propagated.
- **Explanation:** Same gap as V-04: `ProductDAO`/`PersonDAO`/`SaleDAO`
  wrap `IOException` as `PersistenceException` since Requirement 6,
  giving every caller one stable, typed contract. `PromotionDAO` was not
  included in that migration and still fails silently.
- **Consequence:** `PromotionService`/`MainMenu` cannot detect a
  persistence failure for promotions; a save or load error is invisible
  to the rest of the system.
- **Proposed solution:** Apply the same pattern as `ProductDAO`: wrap
  each `catch (IOException e)` to throw
  `new PersistenceException("...", e)` with file path and operation
  context.
- **Other member's code involved:** No — entirely within
  `PromotionDAO.java`.


---

## 7. `PromotionService.java`

### Information Expert — Not a Violation

`PromotionService` does not duplicate data it is not entitled to: it
uses the fields passed in as parameters and delegates persistence to
`PromotionDAO`, and each `Promotion` subtype computes its own discount
(`p.calculateDiscount(sale)`, `p.isActive(today)`) rather than
`PromotionService` reaching into a promotion's fields to compute it
externally.

### Creator — Not a Violation

`registerPercentageDiscount`, `registerCategoryDiscount` and
`registerBulkPurchaseDiscount` each create their respective `Promotion`
subtype from exactly the parameters they receive, then record it via
`promotionDAO.saveAll(...)`. This matches Creator's criteria.

### Controller — Not a Violation

`PromotionService` is the use-case controller for every promotion
operation (registration, listing, and selecting the best applicable
promotion for a sale), coordinating `PromotionDAO` without being a UI or
persistence class.

### Low Coupling — Not a Violation

`PromotionService` depends only on `PromotionDAO`, the `Promotion`
hierarchy and `Sale` (needed to evaluate discounts) — all necessary,
direct collaborations.

### High Cohesion — Not a Violation

Every method concerns promotion business rules (registration, listing,
best-promotion selection). No unrelated responsibility is present.

### Polymorphism — Not a Violation

`findBestPromotionFor` iterates the active promotions and calls
`p.calculateDiscount(sale)` and `p.isActive(today)` uniformly through the
`Promotion` abstraction, with no `instanceof` check to decide how to
compute each discount. This is a correct use of polymorphism: the
variation in discount calculation between `PercentageDiscount`,
`CategoryDiscount` and `BulkPurchaseDiscount` is handled entirely by
each subtype's own override.

### Pure Fabrication — Violation (V-10, same root cause as V-06)

**V-10 — Promotion validation is a private method instead of a reusable
`PromotionValidator`**
- **Pattern:** Pure Fabrication
- **Module:** `PromotionService.validatePercentage(double)` and
  `validateTargetCategory(String)` (both private)
- **Evidence:** Same gap as V-06: the project already has
  `ProductValidator`, `PersonValidator` and `SaleValidator` as dedicated
  Pure Fabrications; `PromotionService` keeps its validation logic as
  private methods instead.
- **Explanation:** Keeping validation inside the service reduces
  reusability and testability, and breaks the pattern the rest of the
  project follows for this exact kind of cross-cutting responsibility.
- **Consequence:** `validatePercentage`/`validateTargetCategory` cannot
  be reused or tested independently of `PromotionService`, and a
  developer looking for promotion validation rules in a
  `validation.PromotionValidator` (by analogy with the other domains)
  would not find it.
- **Proposed solution:** Extract both methods into a new
  `PromotionValidator` class in `com.gamezone.validation`, following the
  same static-utility-class shape as `ProductValidator`.
- **Other member's code involved:** No — a new class fully within my own
  scope.

### Indirection — Not a Violation

`PromotionService` mediates between its callers and `PromotionDAO`, so
callers never touch file persistence directly.

### Protected Variations — Violation (V-11, same root cause as V-07)

**V-11 — `PromotionService` exposes raw `IllegalArgumentException`
instead of the project's typed exception hierarchy**
- **Pattern:** Protected Variations
- **Module:** `validatePercentage` and `validateTargetCategory`, both
  throwing plain `IllegalArgumentException`.
- **Evidence:** `throw new IllegalArgumentException("Percentage must be
  between 0 and 100");` — compare to `ProductValidator`'s equivalent
  checks, which throw `InvalidDataException`.
- **Explanation:** Same gap as V-07: the service layer was migrated to
  the typed `GameZoneException` hierarchy in Requirement 6 specifically
  so callers could rely on one stable contract; `PromotionService` was
  left out.
- **Consequence:** `MainMenu`'s differentiated catch blocks for
  `GameZoneException` subtypes do not apply to promotion registration —
  a caller must still catch generic `IllegalArgumentException`
  separately for this one service.
- **Proposed solution:** Replace both with `InvalidDataException`,
  naturally paired with V-10, since the new `PromotionValidator` would
  be the class that throws them.
- **Other member's code involved:** No — entirely within
  `PromotionService.java`.


---

## 8. `ReturnRepository.java`

### Information Expert — Not a Violation

`ReturnRepository` holds the file path, separators and serialization
format (`toLine`/`fromLine`), so it is the right expert to perform
saving and loading of `Return` data.

### Creator — Not a Violation

`fromLine` creates `Return` instances from the data it resolves and
parses itself, matching Creator's criteria.

### Controller — Not Applicable

`ReturnRepository` is a persistence-layer class, not a use-case entry
point.

### Low Coupling — Violation (V-12)

**V-12 — `ReturnRepository` (persistence layer) depends directly on
three service-layer classes, inverting the project's required layering**
- **Pattern:** Low Coupling
- **Module:** `ReturnRepository`'s constructor and fields:
  `SaleService saleService`, `ProductService productService`,
  `AccessoryService accessoryService`.
- **Evidence:** `fromLine(String)` calls `saleService.findSaleById(saleId)`,
  `productService.findProductById(productId)` and
  `accessoryService.findById(productId)` to resolve references while
  deserializing a return.
- **Explanation:** The project's own architecture rule (section 6 of the
  requirement, `ui → service → persistence → model`) requires the
  persistence layer to depend only downward, toward the model — never
  upward into the service layer. `ReturnRepository` depends on three
  service classes instead, the exact circular-dependency shape that was
  already identified and fixed for `WarrantyRepository` (ajuste A2,
  `fix/warranty-circular-dependency`) by introducing a raw, id-only
  `WarrantyRecord` DTO and moving resolution logic into `WarrantyService`.
  `ReturnRepository` was never updated to follow the same fix.
- **Consequence:** `ReturnRepository` cannot be instantiated or tested
  without also wiring up `SaleService`, `ProductService` and
  `AccessoryService`, coupling a low-level persistence class to the
  entire service graph. It also creates the same risk of a circular
  dependency the A2 fix was meant to eliminate project-wide (here,
  `ReturnService` depends on `ReturnRepository`, which depends back on
  `SaleService`/`ProductService`/`AccessoryService`).
- **Proposed solution:** Apply the same DTO pattern used for
  `WarrantyRecord`: `ReturnRepository` persists and loads a raw
  `ReturnRecord` (id, date, sale id, product ids, reason, refund amount,
  warranty refund — no resolved objects), and `ReturnService` — which
  already depends on `SaleService`/`ProductService`/`AccessoryService` —
  performs the resolution into real `Sale`/`Product`/`Accessory`
  references.
- **Other member's code involved:** No — `SaleService`, `ProductService`
  and `AccessoryService` are only consumed here, not modified; moving the
  resolution logic into `ReturnService` is entirely within my own scope.

### High Cohesion — Not a Violation

`saveAll`, `loadAll`, `toLine` and `fromLine` are all about persisting
`Return` records; the resolution calls inside `fromLine` are a direct
symptom of **V-12** rather than a separate cohesion problem — once the
DTO fix is applied, `fromLine` will only parse raw fields.

### Polymorphism — Not Applicable

`fromLine`'s sequential lookup (`productService.findProductById`, then
`accessoryService.findById`) reflects that products and accessories are
persisted in two separate stores, not a type-check that should instead
be polymorphic dispatch; this mirrors the same accepted pattern already
used in `SaleService.registerSale`.

### Pure Fabrication — Not a Violation

`ReturnRepository` has no direct real-world counterpart; it exists
purely to keep file I/O out of the domain and service layers. Its
*dependencies* are the problem (V-12), not its role as a Pure
Fabrication.

### Indirection — Not a Violation

Aside from V-12's layering issue, `ReturnRepository` otherwise correctly
mediates between the `Return` model and the file system.

### Protected Variations — Violation (V-13)

**V-13 — `fromLine()` silently skips unresolvable returns via
`System.out.println` instead of a typed exception**
- **Pattern:** Protected Variations
- **Module:** `fromLine(String)`, both early returns:
  `System.out.println("Skipping return " + id + ": original sale not
  found"); return null;` and the equivalent for unresolved products.
- **Evidence:** Neither case raises any exception; the caller
  (`loadAll`) simply does not add that return to the list, with no
  signal that data was lost.
- **Explanation:** This failure mode is partly a direct consequence of
  V-12: resolution can fail here specifically because
  `ReturnRepository` depends on services that may not be fully
  initialized yet. Regardless of cause, silently dropping a persisted
  record is exactly the kind of outcome the project's `PersistenceException`
  hierarchy was introduced to make visible instead of hidden.
- **Consequence:** A return record that fails to resolve (e.g. due to a
  data inconsistency) disappears from `viewAllReturns()` and from the
  monthly balance calculation with no trace, which is especially risky
  given `ReturnService.calculateMonthlyReturns` relies on `loadAll()`
  returning every persisted return.
- **Proposed solution:** Once V-12's `ReturnRecord` DTO is in place,
  `fromLine` only parses raw fields and cannot fail to resolve anything
  (resolution moves to `ReturnService`); any remaining parse failure
  should then be surfaced as `PersistenceException` rather than printed.
- **Other member's code involved:** No — entirely within
  `ReturnRepository.java`.

---

## 9. `ReturnService.java`

### Information Expert — Not a Violation

`ReturnService` delegates to the class that owns each piece of
information: sale lookup to `SaleService`, warranty cancellation to
`WarrantyService`, stock restoration to `ProductService`/
`AccessoryService`. `findProductInSale` operates on the `Sale`'s own
product list rather than duplicating that data.

### Creator — Not a Violation

`registerReturn` creates the `Return` instance after gathering the data
needed to initialize it (generated id, today's date, resolved sale,
resolved products, reason, calculated warranty refund) from its own
logic and collaborators, matching Creator's criteria.

### Controller — Not a Violation

`ReturnService` is the use-case controller for "register a return" and
the related return queries, receiving requests from `MainMenu` and
coordinating the appropriate experts without being a UI or persistence
class.

### Low Coupling — Not a Violation

`ReturnService` depends on five collaborators (`ReturnRepository`,
`SaleService`, `ProductService`, `AccessoryService`, `WarrantyService`),
all genuinely required by the return workflow: resolving the original
sale, cancelling warranties for returned consoles, and restoring stock
for returned products/accessories. None is incidental — in fact, this is
the correct place for the resolution logic that **V-12** proposes moving
out of `ReturnRepository`.

### High Cohesion — Violation (V-14, same shape as Leader's V-03)

**V-14 — `registerReturn` concentrates too many responsibilities in one
method**
- **Pattern:** High Cohesion
- **Module:** `ReturnService.registerReturn(saleId, productIds, reason)`
- **Evidence:** In a single method body: (1) validates the three raw
  inputs, (2) resolves the original sale, (3) checks the 30-day return
  window, (4) validates every product belongs to the sale, (5) generates
  the return id, (6) cancels warranties for returned consoles, (7)
  builds the `Return` and calculates its refund, (8) restores stock for
  every returned product/accessory, and (9) persists the updated list —
  nine distinct steps in one method.
- **Explanation:** Same issue the Líder Técnico already identified in
  `SaleService.registerSale` (their V-03): all nine steps belong to the
  same use case, but packing them into a single method makes its
  responsibility too broad to read, test or extend in isolation.
- **Consequence:** The method is hard to unit-test step by step, and
  adding a new step to the return workflow (e.g. notifying the customer)
  means editing an already large method.
- **Proposed solution:** Extract each logical step into small private
  helper methods (e.g. `validateReturnRequest`, `resolveSale`,
  `resolveReturnedProducts`, `cancelWarrantiesIfNeeded`,
  `restoreInventory`), with `registerReturn` reading as a short sequence
  of calls to those helpers — the same solution the Líder proposed for
  `SaleService`.
- **Other member's code involved:** No — entirely within
  `ReturnService.java`.

### Polymorphism — Violation (V-15, same root cause as Leader's V-04)

**V-15 — Explicit `instanceof` checks decide warranty cancellation and
stock-restoration routing**
- **Pattern:** Polymorphism
- **Module:** `ReturnService.registerReturn`, two separate loops:
  `if (product instanceof Console) { warrantyService.cancelWarranties(...); }`
  and `if (product instanceof Accessory) { accessoryService.restoreStock(...); }
  else { productService.restoreStock(...); }`
- **Evidence:** Both branches ask "what concrete type is this product?"
  instead of asking the product itself what it needs.
- **Explanation:** The `Console` check is the exact same root issue the
  Líder Técnico already flagged as their V-04 in `SaleService` (and V-07
  in `MainMenu`) — `ReturnService` needs the same fix
  (`product.requiresWarranty()`) applied here too. The `Accessory` check
  is a related but distinct instance of the same pattern: the service
  decides which stock-restoring collaborator to call based on concrete
  type, instead of a single uniform call.
- **Consequence:** Same as V-04/V-07 for the warranty check — any new
  product type needing warranties forces another edit here. For the
  stock check, `ReturnService` must know about both `ProductService` and
  `AccessoryService` and choose between them by type, instead of a
  single restoration call that works polymorphically.
- **Proposed solution:** Reuse `Product.requiresWarranty()` (once added
  per V-04) for the first check. For the second, since stock is still
  split across two separate DAOs/services by design, a reasonable
  alternative is a single `InventoryService.restoreStock(Product)`
  facade that internally picks `ProductDAO` or `AccessoryDAO` — but this
  would be a larger structural change, so for this requirement I will
  limit the fix to the `Console` check, which shares Developer 1's
  planned change, and document the `Accessory` check as a smaller,
  accepted exception (two concrete, already-distinct inventory stores,
  not an arbitrary type check).
- **Other member's code involved:** **Yes**, for the `Console`/warranty
  half — same coordination as the Líder's V-04/V-07, since it depends on
  `Product`/`Console` (Developer 1's scope).

### Pure Fabrication — Not Applicable

`ReturnService` is itself a Pure Fabrication (a service-layer class with
no direct real-world counterpart), already appropriate and covered under
Controller/Low Coupling above.

### Indirection — Not a Violation

`ReturnService` mediates between `MainMenu` and `ReturnRepository`/
`SaleService`/`ProductService`/`AccessoryService`/`WarrantyService`, so
`MainMenu` never touches persistence or other services directly for
return-related operations.

### Protected Variations — Violation (V-16, same root cause as V-07/V-11)

**V-16 — `ReturnService` exposes raw `IllegalArgumentException` instead
of the project's typed exception hierarchy**
- **Pattern:** Protected Variations
- **Module:** `registerReturn`, every validation branch (blank saleId,
  empty productIds, blank reason, sale not found, 30-day window
  exceeded, product not in sale).
- **Evidence:** E.g. `throw new IllegalArgumentException("La venta
  indicada no existe: " + saleId);` — compare to `ProductService`, which
  throws `ResourceNotFoundException`/`InvalidDataException` since
  Requirement 6.
- **Explanation:** Same gap as V-07/V-11: `ReturnService` was not
  included in the Requirement 6 migration to the typed
  `GameZoneException` hierarchy.
- **Consequence:** `MainMenu`'s differentiated catch blocks for
  `GameZoneException` subtypes do not apply to return registration,
  breaking the uniform error-handling contract the rest of the system
  now relies on.
- **Proposed solution:** Replace each `IllegalArgumentException` with
  the matching typed exception: `InvalidDataException` for blank
  inputs, `ResourceNotFoundException` for an unknown sale,
  `BusinessRuleException` for the 30-day window and for a product not
  belonging to the sale. This would most naturally live behind a new
  `ReturnValidator` class (same Pure Fabrication gap noted for
  Accessory/Promotion in V-06/V-10, though not re-flagged separately
  here to avoid duplicating the same finding a third time).
- **Other member's code involved:** No — entirely within
  `ReturnService.java`.


---

## 10. `WarrantyRepository.java`

### Information Expert — Not a Violation

`WarrantyRepository` holds the file path, separator and raw record
format, so it is the right expert to perform saving and loading of
warranty data.

### Creator — Not a Violation

`fromLine` creates `WarrantyRecord` instances from the exact fields it
just parsed, matching Creator's criteria.

### Controller — Not Applicable

`WarrantyRepository` is a persistence-layer class, not a use-case entry
point.

### Low Coupling — Not a Violation (correctly fixed by ajuste A2)

Unlike `ReturnRepository` (**V-12**), `WarrantyRepository` depends only
on `Warranty`/`ExtendedWarranty` — no service-layer dependency at all.
This is the direct result of the A2 fix
(`fix/warranty-circular-dependency`): the repository persists and loads
a raw `WarrantyRecord` (ids and dates only) instead of resolving
`Product`/`Sale` references itself, which is exactly the pattern
proposed in V-12 for `ReturnRepository`.

### High Cohesion — Not a Violation

`saveAll`, `loadAll`, `toLine`, `fromLine` and the `WarrantyRecord`
inner class are all about reading and writing raw warranty data.

### Polymorphism — Violation (V-17, smaller instance of V-03/V-08)

**V-17 — `toLine()` uses `instanceof` to pick the type discriminator**
- **Pattern:** Polymorphism
- **Module:** `WarrantyRepository.toLine(Warranty)`
- **Evidence:** `String type = (warranty instanceof ExtendedWarranty) ?
  EXTENDED_TYPE : BASIC_TYPE;`
- **Explanation:** Same root issue as V-03/V-08: the discriminator used
  for serialization should come from the object itself (e.g. a
  `getWarrantyType()` method, which `Warranty` already exposes per the
  class diagram as an abstract method returning `"BASIC"`/`"EXTENDED"`-style
  values), not from a type check in the DAO.
- **Consequence:** Smaller impact than V-03/V-08 since there are only
  two subtypes, but the same fragility applies if a third warranty type
  is ever added.
- **Proposed solution:** `Warranty` already defines
  `getWarrantyType()` (per `docs/warranty-class-diagram.md`); use
  `warranty.getWarrantyType()` directly instead of the `instanceof`
  check, assuming its return value matches `BASIC_TYPE`/`EXTENDED_TYPE`
  (or adjust the constants to match it).
- **Other member's code involved:** No — `getWarrantyType()` already
  exists on `Warranty`/`BasicWarranty`/`ExtendedWarranty` (my own scope,
  Requirement 4); this is purely a call-site fix in `toLine`.

### Pure Fabrication — Not a Violation

`WarrantyRepository` has no direct real-world counterpart; it exists
purely to keep file I/O out of the domain and service layers.

### Indirection — Not a Violation

`WarrantyRepository` correctly mediates only between raw warranty data
and the file system — it no longer mediates between `Warranty` and the
service layer, which was the A2 fix's purpose.

### Protected Variations — Violation (V-18, same root cause as V-04/V-09)

**V-18 — `WarrantyRepository` propagates raw `IOException` instead of
the project's exception hierarchy**
- **Pattern:** Protected Variations
- **Module:** `saveAll(List<Warranty>)` and `loadAll()`, both declaring
  `throws IOException` with no internal catch at all.
- **Evidence:** Neither method catches the `IOException` that the
  try-with-resources block can throw; it propagates directly to
  `WarrantyService`.
- **Explanation:** Same gap as V-04/V-09: `ProductDAO`/`PersonDAO`/
  `SaleDAO` wrap `IOException` as `PersistenceException` since
  Requirement 6. `WarrantyRepository` was not included in that
  migration — here the exception is not even swallowed (unlike the
  Accessory/Promotion DAOs), just left as a raw checked exception.
- **Consequence:** `WarrantyService`/`MainMenu` must handle a different
  exception type for warranty failures than for product, person or sale
  failures, breaking the system's single stable exception contract.
- **Proposed solution:** Wrap both methods' `IOException` as
  `PersistenceException`, exactly as `ProductDAO` does.
- **Other member's code involved:** No — entirely within
  `WarrantyRepository.java`.

---

## 11. `WarrantyService.java`

### Information Expert — Not a Violation

`WarrantyService` delegates to the class that owns each piece of
information: product resolution to `ProductService`, sale resolution to
`SaleDAO`. It does not duplicate data it is not entitled to.

### Creator — Not a Violation

`assignBasicWarranty`/`assignExtendedWarranty` create `BasicWarranty`/
`ExtendedWarranty` from exactly the `product`, `sale` and `startDate`
they receive, and `resolveWarranty` creates the same from a resolved
`product`/`sale` plus the raw record's fields — both match Creator's
criteria. The `EXTENDED_TYPE`-string check in `resolveWarranty` is not
flagged under Polymorphism, for the same reason as `parseLine` in the
other DAOs: there is no `Warranty` object yet to dispatch on at that
point, so a discriminated construction step is unavoidable.

### Controller — Not a Violation

`WarrantyService` is the use-case controller for every warranty
operation (assignment, cancellation, queries), coordinating
`WarrantyRepository`, `SaleDAO` and `ProductService` without being a UI
or persistence class.

### Low Coupling — Not a Violation (deliberate, already justified)

`WarrantyService` depends on `WarrantyRepository`, `SaleDAO` (not
`SaleService`) and `ProductService`. Using `SaleDAO` directly, instead of
`SaleService`, is intentional: it is precisely what the A2 fix relies on
to avoid recreating the circular dependency between `SaleService` and
the warranty module (`SaleService` already depends on `WarrantyService`
via `setWarrantyService`). This is deliberate, well-managed coupling,
not an accidental one.

### High Cohesion — Not a Violation

Each method has a single, well-scoped responsibility
(`assignBasicWarranty`, `assignExtendedWarranty`, `cancelWarranties`,
the three `list*` queries, `resolveWarranty`, `findSaleById`,
`generateWarrantyId`). Unlike `SaleService.registerSale` (Leader's V-03)
or `ReturnService.registerReturn` (**V-14**), no single method here
concentrates many unrelated steps — the warranty-assignment flow is
intentionally kept simple because warranty creation itself is a small
operation.

### Polymorphism — Not a Violation

No method branches on the concrete type of `Warranty` via `instanceof`;
`getAdditionalCost()` (used in `cancelWarranties`) and `getEndDate()`/
`isActive()` (used in the `list*` queries) are called polymorphically
through the `Warranty` abstraction, letting `BasicWarranty` and
`ExtendedWarranty` each provide their own values. This is a correct use
of polymorphism.

### Pure Fabrication — Not Applicable

`WarrantyService` is itself a Pure Fabrication, already appropriate and
covered under Controller/Low Coupling above.

### Indirection — Not a Violation

`WarrantyService` mediates between its callers and
`WarrantyRepository`/`SaleDAO`/`ProductService`, so callers never touch
persistence directly for warranty operations.

### Protected Variations — Violation (V-19, same root cause as V-16/V-18)

**V-19 — `WarrantyService` uses raw `IllegalArgumentException` and
silent `println` instead of the project's exception hierarchy**
- **Pattern:** Protected Variations
- **Module:** Two related gaps:
  - `assignBasicWarranty`, `assignExtendedWarranty` and
    `listWarrantiesExpiringSoon` throw plain `IllegalArgumentException`
    for invalid input, instead of `InvalidDataException`.
  - `resolveWarranty` silently skips an unresolvable record via
    `System.out.println("Skipping warranty " + record.getId() + ":
    product not found")` / `"... sale not found"`, with no exception and
    no caller-visible signal, the same pattern as **V-13** in
    `ReturnRepository`.
- **Evidence:** `throw new IllegalArgumentException("Debe indicar el
  producto y la venta...")`; and the two `System.out.println` calls in
  `resolveWarranty`.
- **Explanation:** Same gap as V-07/V-11/V-16: this class was not
  included in the Requirement 6 migration to the typed
  `GameZoneException` hierarchy, and its silent-skip behavior hides data
  loss the same way V-04/V-09/V-13 do elsewhere.
- **Consequence:** `MainMenu`'s differentiated catch blocks do not apply
  to warranty assignment, and a warranty record that fails to resolve
  (e.g. because its product was deleted) silently disappears from every
  listing with no trace — including `listWarrantiesExpiringSoon`, which
  a store owner might rely on for maintenance planning.
- **Proposed solution:** Replace the `IllegalArgumentException`s with
  `InvalidDataException`; once V-18 wraps `WarrantyRepository`'s
  `IOException` as `PersistenceException`, replace the two
  `System.out.println` skips in `resolveWarranty` with a
  `PersistenceException` (or, if silently skipping is intentionally kept
  as a resilience choice, document that decision explicitly rather than
  leaving it as a side effect of unrelated exception swallowing).
- **Other member's code involved:** No — entirely within
  `WarrantyService.java`.


