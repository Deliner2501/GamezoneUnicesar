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