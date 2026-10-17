# SOLID Audit — Team Orienting Questions

This document answers the five orienting questions required by
Requirement 8, discussed as a team and assigned as follows: the Technical
Leader answers questions 1 and 5 (criteria and evidence for the audit, and
correction order and coordination, both tied to the consolidation and
Phase 2 coordination role); Developer 1 and Developer 2 answer questions 2,
3 and 4 (findings from their own audit work).

## 1. What criterion did the team use to decide whether a code fragment violates a SOLID principle, complies with it, or the principle does not apply? What minimum evidence must support each verdict?

A fragment is marked **Violation** only when it can be tied to a concrete,
checkable symptom of the principle being broken, not to a stylistic
preference. For each principle the team looked for its specific failure
signature:

- **SRP:** a class that has more than one reason to change, shown by members
  that belong to different axes of change (for example file access,
  serialization format and reference resolution in the same class).
- **OCP:** adding a new kind of item or area requires editing existing code,
  shown by type-based branching (`instanceof`, parallel collections per
  type) or by hard-coded lists coupled by position (an options array and a
  `switch` that must be kept in sync).
- **LSP:** a subtype cannot replace its base type, because it throws where the
  base type does not, narrows a precondition or changes the contract.
- **ISP:** a client is forced to depend on interface methods it does not use.
  Because the project has few interfaces, this was only reported when an
  interface exists or is introduced; a client that depends on a few methods
  of a concrete class was not counted as an ISP violation.
- **DIP:** a high-level class depends on a concrete low-level class or on a
  layer above it instead of an abstraction, for example a model class that
  references the UI layer, or a service that holds a concrete DAO.
  Same-layer service-to-service dependencies with a single implementation and
  no expected variation were not counted, and the composition root (`Main`)
  is the intended place where concrete classes are chosen.

A verdict of **Correct** requires naming the class, method and place where
the principle is met, with a one-line justification (for example "`Sale`
sums `product.getPrice()` without checking the item type: OCP is met").

A verdict of **Not applicable** requires explaining why the principle has no
point of application in the audited code (for example "`SaleDAO` neither
extends a class nor implements an interface, so there is no substitution
relationship for LSP").

For a **Violation**, the minimum evidence accepted into the consolidation is:
the exact class and method, the line numbers or code fragment quoted from the
current state of `develop`, a one- or two-sentence explanation of why the
fragment breaks the principle, and the concrete consequence it causes (what
becomes harder or more fragile, not just "bad practice"). A finding without
at least the class/method and a quoted fragment is returned to its author
before it is consolidated. A single fragment gets a single ID, tagged with
the principle whose proposed solution fixes it most directly, and a related
principle is noted in parentheses only as context.

## 2. Some violations show up at the boundary between two members' code, for example when one member's class depends inappropriately on another member's class. What findings of this kind did the team identify? How were they registered in the consolidation and who was assigned to correct them? Present at least one concrete case or, if there were none, explain how the team verified none exist.

_To be answered by Developer 1 and Developer 2._

## 3. Some violations were already fixed by the GRASP refactoring of Requirement 7, and some GRASP fixes may have solved or introduced SOLID problems. What cases did the team find? Present at least one concrete case of each kind.

_To be answered by Developer 1 and Developer 2._

## 4. The correction must preserve the external behavior of the system. What strategy does the team follow to guarantee that all the menu operations keep working the same? What evidence is shown at the end to demonstrate that nothing was broken?

_To be answered by Developer 1 and Developer 2._

## 5. In what order are violations corrected, and why? When a correction involves more than one member's code, how is the work coordinated, especially when several members must change the same files on the shared integration branch?

Violations are corrected in Phase 2 in this order:

1. **Prerequisite abstractions first.** The interfaces that other members'
   corrections depend on are created and merged to `develop` before anything
   else, because they unblock the rest: the `PersonRepository` and
   `AccessoryRepository` interfaces (Developer 2) are needed by the Leader's
   V-03 and V-05. Whoever owns the prerequisite class makes that change first,
   in a small Pull Request, and notifies the dependent member.
2. **Violations entirely within one member's own code, in parallel.** These
   need nobody else, so each member can progress at the same time. For the
   Leader these are V-01 (remove the model-to-UI dependency), V-06 (the
   `AreaMenu` abstraction) and V-02 (extract the sale line mapper).
3. **Dependent violations last, after the prerequisite is on `develop`.**
   The dependent member merges the updated `develop` into their branch and
   then applies their own fix. For the Leader this means V-04 (the item
   source abstraction, which also uses the accessory repository) and
   V-03/V-05 (wiring `SaleRepository`, `PersonRepository` and
   `AccessoryRepository`) at the end, after V-02, because the mapper
   extracted in V-02 is what receives those abstractions.
4. **No order may break the build or change behavior mid-sequence.** Every
   commit must compile with `mvn clean compile` and leave the no-regression
   checklist unchanged.

Because all three members change the same integration code, the team
applies these coordination rules:

- Each member works in their own branch created from `develop`, with their
  own Pull Request reviewed by another member, so a conflict affects one
  small change at a time and not a shared branch.
- Each violation is a separate atomic commit tagged with its ID, so any
  regression can be traced to one commit and reverted on its own.
- `Main.java` is the main conflict hotspot, because each new interface is
  wired there by a different member. Changes to `Main.java` are kept to the
  minimum lines per commit, and the member who merges second resolves the
  conflict after merging the updated `develop` into their branch.
- Members pull `develop` before starting each correction and before each push,
  never use `git push --force`, and re-run the affected rows of the
  checklist after merging a teammate's prerequisite.
- The agreed split of every cross-member violation is recorded in the
  "frontier findings" section of the consolidated audit, so who makes which
  half and in what order is explicit and not improvised during Phase 2. The
  Leader notifies the owner of each prerequisite (class, interface name and
  methods) before Phase 2 starts.

## Question 2: Boundary findings between members' code

**Answered by: Developer 2.**

Developer 2 found boundary findings in four places, where a class in Developer 2's scope and a class of the Technical Leader depend on each other in a way that breaks a principle:

| Finding | Developer 2 code | Technical Leader code | Principle |
|---|---|---|---|
| V-01 | `PersonDAO` handles customers and sellers | `SaleDAO` (calls `loadCustomers`/`loadSellers`) and `Main` (builds `PersonDAO`) | SRP |
| V-02 | `ReturnService` computes the monthly balance | `ReturnMenu` (calls the three balance methods) and `Main` | SRP |
| V-09 | `WarrantyService` depends on the whole `SaleDAO` and only calls `findAll()` | `SaleDAO`, `Main` | ISP |
| V-10 to V-13 | Services depend on concrete DAOs | `Main` builds them; `SaleService` receives `AccessoryDAO` directly | DIP |

The clearest concrete case is **V-01**. `SaleDAO` receives the whole `PersonDAO` only to read the two lists, so splitting `PersonDAO` into `CustomerDAO` and `SellerDAO` also changes the `SaleDAO` constructor and the wiring in `Main`. No single member can correct it alone.

A second case was found outside the five principles' direct scope: `SaleService` reads and updates accessory stock directly through `AccessoryDAO` (`accessory.reduceStock(quantity); accessoryDAO.update(accessory);`), skipping `AccessoryService.updateStock`. Stock responsibility is split across two services. It belongs to the Leader's class, so it was reported to the Leader and not counted as a Developer 2 violation.

**How they were registered.** In Developer 2's report, each finding has the field "Code of another member" set to "Yes" with the exact classes and lines, and the report ends with a "Reported to the Technical Leader" list. The Technical Leader unifies these findings in the consolidated audit, gives them definitive identifiers and assigns the person responsible for the correction. Developer 2 proposes that the owner of the class being split (Developer 2) creates the new classes, and that the Leader adapts the callers (`SaleDAO`, `Main`, `ReturnMenu`) in the same coordinated order.

## Question 3: Relation with the GRASP audit (Requirement 7)

**Answered by: Developer 2.**

**SOLID violations in code that GRASP had already corrected**

- **V-03 and V-04 (SRP).** In Requirement 7 the persistence classes were made independent of the service layer with raw records (`ReturnRecord`, `WarrantyRecord`), and the logic that rebuilds `Return` and `Warranty` objects from those records was moved into `ReturnService` and `WarrantyService` (`resolveReturn`, `resolveWarranty`). This fixed the layering problem (persistence no longer depends on services) but the services now carry a mapping responsibility next to their business rules. The SOLID audit proposes extracting it to resolver classes.
- **V-08 (OCP).** The GRASP correction replaced the magic string with the constant `EXTENDED_TYPE`, which improved readability but kept the type check `if (EXTENDED_TYPE.equals(record.getType()))`, so a new warranty type still requires editing `resolveWarranty`.

**GRASP corrections that also solved a SOLID problem**

- **Polymorphic `toCsvLine()` and `getTypeCode()`** (Accessory, Promotion, Warranty). Saving no longer asks "which subtype is this?", so writing is closed for modification (OCP). This is why V-05 and V-06 only concern reading (`parseLine`).
- **`Product.requiresWarranty()`.** `ReturnService.cancelConsoleWarranties` no longer checks for `Console`, which removed a type check that would have been an OCP problem.
- **Raw records for persistence.** Removing the dependency from persistence to services also fixed the direction of dependencies, which is the base of DIP (high-level code does not depend on low-level code). The remaining DIP findings (V-10 to V-13) are about concrete classes instead of abstractions.

## question 4. The correction must preserve the external behavior of the system. What strategy does the team follow to guarantee that all the menu operations keep working the same? What evidence is shown at the end to demonstrate that nothing was broken?

The corrections are refactors, so the strategy is to compare the system before and after with the same inputs, and to keep every change small enough to be compared.

**Strategy.**

1. **Baseline before the first correction.** The team documents in `docs/solid-refactor-log.md` a checklist that covers every menu operation (products, people, sales, accessories, promotions, returns, warranties). Each row states the operation, the input data and the expected result. It is run on `develop` and the observed results are recorded as the reference.
2. **One violation per commit.** Each correction is an atomic `refactor:` commit tagged with its violation ID, so a regression can be traced to one change and reverted on its own.
3. **Verification on every Phase 2 Pull Request.** Before a Pull Request is opened the project must build with `mvn clean compile`, and the checklist rows for the operations touched by that correction are run again and compared with the baseline: same inputs, same results, same messages.
4. **Data files are part of the behavior.** The same scenario is run on a copy of the `data/` folder before and after each correction, and the resulting CSV files are compared, because the requirement demands identical file contents.
5. **Out-of-scope defects stay untouched.** The defects recorded as out of scope in Requirement 7 and in this audit are not fixed, so that "the same behavior" stays well defined.
6. **No new violations.** A correction cannot introduce a new SOLID violation or bring back a GRASP one. A sacrifice of one principle for another is justified in the refactor log.
7. **Known baseline difference.** `Console.getCategory()` returns the wrong category (V-06), so saving a console fails in `ProductDAO.writeAll` on the current `develop`. The baseline records this behavior explicitly, and the correction of V-06 is documented as the only expected difference in the console registration flow.

**Evidence shown at the end.** (a) The baseline and the final results of the checklist side by side, covering all operations. (b) The `BUILD SUCCESS` output for each Pull Request. (c) The comparison of the data files before and after the scenario. (d) A live demonstration of the full scenario: a sale with a console, a video game and an accessory, the best promotion applied, an extended warranty, a partial return and the monthly balance.