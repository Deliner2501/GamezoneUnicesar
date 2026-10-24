# GRASP Audit — Team Orienting Questions

This document answers the five orienting questions required by
Requirement 7, discussed as a team and assigned as follows: the Technical
Leader answers questions 1 and 5 (criteria/evidence for the audit, and
correction ordering/coordination — both tied to the Leader's consolidation
and Phase 2 coordination role); Developer 1 and Developer 2 answer
questions 2, 3 and 4 (findings from their own audit work).

## 1. What criterion did the team use to decide whether a code fragment
violates a GRASP pattern, correctly applies it, or the pattern does not
apply? What minimum evidence must support each verdict?

A fragment is marked **Violation** only when it can be tied to a concrete,
checkable symptom of the pattern being broken — not a stylistic
preference. For each pattern, the team looked for its specific failure
signature: for example, Information Expert is violated when a class
computes a result using data it had to pull out of another class instead
of asking the class that owns that data; Low Coupling is violated when a
class depends on more collaborators or more concrete types than its
responsibility requires; Polymorphism is violated when behavior that
varies by type is selected with an explicit `instanceof`/type-check
conditional instead of a polymorphic call; Protected Variations is
violated when a caller is exposed to an implementation detail (such as a
raw `IOException`) that a sibling method already hides behind a stable
abstraction.

A verdict of **Correct** requires pointing to the specific class and
method where the pattern is properly applied, with a one-line justification
of why it satisfies the pattern's criteria (e.g. "`Sale` computes its own
total from its own fields — Information Expert is correctly applied").

A verdict of **Not Applicable** requires explaining why the pattern has no
point of application in the audited code (e.g. a class with no
type-varying behavior has nothing for Polymorphism to apply to).

For a **Violation**, the minimum evidence required to be accepted into the
consolidation is: the exact class and method name, the relevant lines or
code fragment (quoted from the current state of `develop`), a one- or
two-sentence explanation of why that fragment breaks the pattern's
criteria, and the concrete consequence it causes (not just "this is bad
practice," but what becomes harder or more fragile because of it). A
finding without at least the class/method and a quoted fragment is
returned to its author for revision before it is consolidated.


## 2. Some violations show up at the boundary between two members' code,
for example when one member's class depends inappropriately on another
member's class. What findings of this kind did the team identify? How
were they registered in the consolidation and who was assigned to
correct them? Present at least one concrete case or, if there were none,
explain how the team verified none exist.

The team found two clear frontier findings so far.

**Case 1 — `ReturnRepository` (Developer 2) depends on three
service-layer classes across all three members' scope.** `ReturnRepository.fromLine()`
calls `saleService.findSaleById(...)` (Technical Leader's scope),
`productService.findProductById(...)` (Developer 1's scope) and
`accessoryService.findById(...)` (Developer 2's own scope) to resolve
references while deserializing a return, inverting the project's
required `ui → service → persistence → model` layering. This is the
same shape of circular/inappropriate dependency already identified and
fixed for `WarrantyRepository` in ajuste A2 (`WarrantyRecord` DTO), never
applied to `ReturnRepository`. The fix is entirely within Developer 2's
own classes (`ReturnRepository`/`ReturnService`), so no other member's
code needs to change for this one — it is a frontier finding only in the
sense that the *violation* spans dependencies on other members' classes,
not that its *correction* does.

**Case 2 — the `instanceof Console` chain spans all three members.** The
same root cause (no polymorphic way to ask a product whether it needs a
warranty) was independently found in `SaleService` and `MainMenu`
(Technical Leader's audit, V-04/V-07) and in `ReturnService` (Developer
2's audit, V-15). All three call sites need `Product.requiresWarranty()`
added to the `Product`/`Console` hierarchy — Developer 1's scope —
before any of the three call-site fixes can be applied.

Both are registered the same way: each individual audit marks the
finding's **"Other member's code involved"** field as **Yes**, naming
the specific class and member whose code must change first. The
consolidation pulls every "Yes" finding into the consolidated audit's
dedicated **hallazgos de frontera** section, where the Líder Técnico
assigns the split explicitly: the member who owns the prerequisite class
adds the new method or interface; each member who owns a dependent call
site applies their own fix only after that prerequisite is merged to
`develop` (per the correction order described in Question 5).

## 3. Some patterns are closely related to each other. Did the team find
code fragments that violate more than one pattern at the same time? How
were they registered without duplicating findings, and how was it
decided which pattern guides the correction?

Yes. Two recurring examples:

**Technical Leader's V-01 (`Sale.generateReceipt()`).** This fragment was
registered under a single ID, explicitly tagged **"Protected Variations
(related to Pure Fabrication)"**: the domain class is not shielded from
presentation changes (Protected Variations), and the reason it isn't is
that no dedicated Pure Fabrication class exists to hold that
responsibility. Rather than opening two findings for one fragment, the
team registered it once under the pattern whose textbook fix matches the
proposed solution exactly (extracting a `SaleReceiptFormatter`), with
the secondary pattern noted in parentheses for context.

**The serialization `instanceof` checks (Developer 2's V-03, V-08,
V-17, in `AccessoryDAO`, `PromotionDAO` and `WarrantyRepository`).**
Each was registered under **Polymorphism**, since the proposed fix is a
polymorphic method (`toCsvLine()`/`getWarrantyType()`). They equally
describe a Protected Variations failure (the DAO is not shielded from a
new subtype being added), but that aspect was left implicit rather than
opened as a second finding, since fixing the Polymorphism issue
automatically resolves it.

The rule the team applied: a single fragment gets **one** violation ID,
tagged with whichever pattern's criteria the *proposed solution*
satisfies most directly — that pattern "guides" the correction. A second,
closely related pattern is noted in parentheses only when it adds
useful context for *why* the fix matters, never as a separate ID, so the
final violation count is not inflated by counting the same code twice.

## 4. The correction must preserve the external behavior of the system. What strategy does the team follow to guarantee that all the menu operations keep working the same? What evidence is shown at the end to demonstrate that nothing was broken?

The corrections are refactors, so the strategy is to compare the system before and after with the same inputs, and to keep every change small enough to be compared.

**Strategy.**

1. **Baseline before the first correction.** Before any Phase 2 change, the team writes in `docs/grasp-refactor-log.md` a verification checklist that covers every operation of the user interface (the ten operations of the base workshop plus those added by the accessory, promotion, return and warranty modules). Each row states the operation, the input data and the expected result. The checklist is run on `develop` and the observed results are recorded as the reference.
2. **One violation per commit.** Each correction is a separate atomic commit tagged with its violation ID, so any regression can be traced to a single change and reverted on its own.
3. **Verification on every Phase 2 Pull Request.** Before a Pull Request is opened, the project must compile with `mvn clean compile`, and the checklist rows for the operations touched by that correction are run again and compared with the baseline: same inputs, same results, same messages shown to the user.
4. **Data files are part of the behavior.** The same scenario is run against a copy of the `data/` folder before and after each correction, and the resulting CSV files are compared (for example with `Compare-Object` on their content or with a file hash), because the requirement demands identical file contents.
5. **Out-of-scope defects stay untouched.** Functional defects recorded as out of scope, such as the data loss in `ProductDAO.save`, are not fixed in this requirement even when a correction touches the same code, so that "the same behavior" remains well defined.
6. **No new violations.** A correction cannot introduce a new violation. If a pattern has to be sacrificed in favor of another one, the decision is justified in the refactor log.

**Evidence shown at the end.** (a) The baseline and the final results of the checklist side by side, covering all operations. (b) The `BUILD SUCCESS` output of `mvn clean compile` for each Pull Request. (c) The comparison of the data files before and after the scenario. (d) A live demonstration in the oral defense of the complete scenario (a sale with a console, a video game and an accessory, the best promotion applied, an extended warranty, a partial return and the monthly balance), showing the same results as the baseline.

## 5. In what order are violations corrected, and why? When a correction
involves more than one member's code, how is the work coordinated?

Violations are corrected in the following order during Phase 2:

1. **Violations entirely within one member's own code first** (no
   cross-member dependency), so each member can make progress
   independently and in parallel without waiting on anyone else.
2. **Violations that require a change in another member's class before
   the dependent fix can be made** (frontier/cross-member violations),
   corrected only after the prerequisite change lands in `develop`. The
   member who owns the prerequisite class makes their change first, opens
   and merges their PR, and only then does the dependent member rebase
   their branch on the updated `develop` and apply their own fix.
3. Violations are **never corrected in an order that would temporarily
   break the build or change observable behavior mid-sequence** — each
   individual commit must leave the project compiling and passing the
   same manual verification checklist used for the no-regression baseline.

When a correction involves code under more than one member's
responsibility, the Leader coordinates it directly: the member who owns
the prerequisite class is notified of exactly what is needed (method
name, intended behavior) before they start their own Phase 2 work, so the
dependent member isn't blocked for long. All cross-member violations and
their agreed split of responsibility are recorded in the consolidated
audit's "frontier findings" section, so the correction order and who does
each half is explicit and not left to be improvised during Phase 2.