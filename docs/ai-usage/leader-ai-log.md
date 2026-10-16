# PROMPTS AND RESPONSES
## 1. How should I organize the GitHub repository and branches for a team project using Git Flow?
**Answer:** The repository can use main as the stable branch, develop as the integration branch, and feature/* branches for individual tasks. Feature branches should be created from develop and merged back through Pull Requests.
## 2. How do I create a Pull Request from a feature branch to the develop branch?
**Answer:** First, the feature branch should be pushed to GitHub. Then, a Pull Request can be created with develop as the base branch. The Pull Request should explain the changes and be reviewed by another team member before merging.
## 3. What should I do if two team members make changes in the same file and a Git conflict appears?
**Answer:** Git marks the parts of the file that have conflicts. The changes should be reviewed manually, keeping the correct version or combining both changes when necessary. After resolving the conflict, the file can be added and committed.
## 4. How can I make a good and atomic commit for a change in my project?
**Answer:** An atomic commit should contain one clear and related change. The commit message should briefly describe what was changed, instead of including several unrelated modifications.
## 5. What is the correct way to use Conventional Commits in a Java project?
**Answer:** Conventional Commits use prefixes to describe the type of change. For example, feat can be used for a new feature, fix 
## 6. How should I organize the packages of my Maven project according to a layered architecture?
**Answer:** The project can have a main package such as com.gamezone with the subpackages model, persistence, service, and ui. Main.java can remain in the root package.
## 7. What is the correct dependency direction between the model, persistence, service, and UI layers?
**Answer:** The UI layer depends on the service layer. The service layer depends on the model and persistence layers. The persistence layer depends on the model, while the model should remain independent.
## 8. I have an error in my Maven project. How can I understand what the error message means?
**Answer:** The first step is to read the error message and identify the file, class, or dependency related to the problem. The error should be analyzed before making changes to the project.
## 9. Can you review this part of my Java code and tell me if there are things I could improve?
**Answer:** The code can be reviewed for things such as naming, organization, visibility of attributes, duplicated code, and whether it follows the project's requirements. The final changes should be made by the programmer after understanding the suggestions.
## 10. What validations should a sale service normally perform before registering a sale?
**Answer:**The service should verify that the information required for the sale is valid before registering it. For example, it can check that the customer and products exist and that the necessary information is available.
## 11. What things should I check when reviewing a teammate's Pull Request?
**Answer:** The changes should be checked to make sure they correspond to the purpose of the feature, follow the project's structure and rules, and do not introduce unnecessary changes. The code should also be understandable before approving the Pull Request.
## 12. What are some good English naming conventions for Java classes, methods, and variables?
**Answer:** Java classes normally use PascalCase, while methods and variables use camelCase. Names should be clear and describe their purpose. For example, Customer, createSale(), and totalAmount.

# Requirement 5 - AI usage log (Technical Leader)

Retrospective summary covering the most relevant AI-assisted decisions across Requirements 1-5.

**2026-09-26 — Claude — Requirement 1, `feature/accessory-module`.** Objective: integrate `SaleService.registerSale` so a sale could mix products and accessories without breaking existing behavior. I asked how to resolve an item id against both catalogs, given that `Accessory extends Product`. Claude suggested checking the item id against both `ProductDAO` and `AccessoryDAO`, keeping two separate maps so each type gets persisted through its own DAO. I accepted the approach and kept `Sale.addProduct` untouched, since an `Accessory` already satisfies `Product`. Reflected in commit `feat(service): extend SaleService to resolve accessories alongside products`.

**2026-09-26 — Claude — Requirement 1, `feature/accessory-module`.** Objective: fix a compile error in `MainMenu.java` after adding the accessory submenu. I pasted the compiler's complaint about symbols outside the class. Claude found an extra closing brace right after the `accessoryMenu()` switch block, which closed the class early and left the newly added methods floating outside it. I removed the extra brace and verified the brace count matched at the end of the file. No dedicated commit; folded into the same commit as the submenu.

**2026-09-26 — Claude — Requirement 2, `feature/promotion-module`.** Objective: show the subtotal, discount, and final total on a sale receipt. I asked how to add a receipt to `Sale`, since the class had no `generateReceipt` method at all despite the requirement asking to "update" one. Claude recommended adding `appliedPromotionName`/`discountAmount` fields, an `applyPromotion()` method, a `getFinalTotal()` method, and a new `generateReceipt()` method, without touching the existing `toString()`. I accepted the design, and Claude also flagged that `SaleDAO` did not persist the discount to file, which I asked to fix in the same session. Reflected in commits `feat(model): add applied promotion fields and generateReceipt to Sale` and `fix(persistence): persist applied promotion data in SaleDAO`.

**2026-09-26 — Claude — Requirement 3, `feature/return-module`.** Objective: understand why the project did not compile after a teammate's merge, before starting my own integration work. Claude reviewed the uploaded repository and found that `ReturnService.registerReturn` was left mid-statement — missing `viewAllReturns`, `viewReturnsByCustomer`, `viewReturnsBySale`, and `generateMonthlyBalance` entirely — and that `SaleService.findSaleById` and `ProductService.findProductById` were referenced by other classes but never implemented anywhere. I asked Claude to complete the missing methods and add the two missing lookups so the branch would compile before continuing with my own tasks. Reflected in commits `fix(service): add missing findSaleById to SaleService`, `fix(service): add missing findProductById to ProductService`, and `fix(service): complete ReturnService registration and query methods`.

**2026-09-27 — Claude — Requirement 4, `feature/warranty-module`.** Objective: resolve a real circular dependency, since `SaleService` needed `WarrantyService`, and `WarrantyRepository` needed `SaleService` to resolve sale references while loading. I asked how to wire both services in `Main` given that cycle. Claude suggested breaking it with setter injection: build `SaleService` first without `WarrantyService`, then call `saleService.setWarrantyService(...)` once `WarrantyService` exists. I accepted this as a working short-term solution, knowing it would later be superseded by a cleaner fix. Reflected in commit `feat(service): generate warranties automatically in SaleService.registerSale`.

**2026-09-27 — Claude — Requirement 4, `feature/warranty-module`.** Objective: add the extended warranty's 10% cost to a sale's total, with no existing way to write to `Sale.total` from outside the class. I asked how `SaleService` could charge an extra amount not tied to any product's price. Claude suggested adding a new `Sale.addAdditionalCost(double)` method, tracked separately from the product-based subtotal. I accepted the design. Reflected in commit `feat(model): add additional cost tracking to Sale`.

**2026-09-27 — Claude — Requirement 5, `refactor/unified-sale-registration` (A3).** Objective: reorganize `registerSale` into the 8-step flow required by the integration spec. I asked Claude to restructure the method and verify the resulting order matched the specification exactly. While doing so, Claude found that my earlier `addAdditionalCost` fix (Requirement 4) was also incrementing `total` directly, which made the receipt's "Subtotal" line silently include the warranty cost. Claude proposed separating `additionalCost` from `total` and moving it into `getFinalTotal()`'s formula instead. I accepted both the reorganization and the bug fix, and verified the final total formula matched `subtotal − discount + additionalCost`. Reflected in commits `refactor(model): keep Sale subtotal separate from additional warranty cost` and `refactor(service): reorganize SaleService.registerSale into the unified 8-step flow`.

**2026-09-27 — Claude — Requirement 5, `docs/integration-documentation` (A8).** Objective: produce the integration analysis and the unified class diagram covering all four modules. I asked Claude to write `docs/integration-analysis.md` and `docs/integrated-class-diagram.md` based on the final, already-merged code for A1 through A7, rather than from the original specification alone. Claude reviewed the actual merged classes first and wrote the cause/solution summary and a single Mermaid diagram spanning all four layers and modules from that. I accepted the content with no changes. Reflected in commits `docs: add integration analysis for the A1-A7 adjustments` and `docs: add integrated class diagram for the full system`.


# Requirement 7 - Phase 1 (GRASP Audit) - AI usage log

## 1. How should a team split a GRASP audit of an existing codebase among three members?
**Answer:** Each member audits the code they are responsible for, following the same ownership already used for previous requirements, so the scope is consistent with the project's history instead of reassigned from scratch.

## 2. What should an individual GRASP audit report contain before it can be consolidated?
**Answer:** A scope section listing exactly what was audited, a verdict per pattern (Violation, Correct, or Not Applicable), and for every violation: an ID, evidence from the actual code, an explanation, a consequence, and a proposed solution.

## 3. As Technical Leader, what is my role in consolidating three individual audits into one?
**Answer:** Verify that every class in the project was audited by exactly one person, check that each violation has real evidence, merge any findings that appear in more than one report into a single entry, and assign a final responsible person to each one.

## 4. How should orienting questions be split between team members when the assignment isn't fixed by the instructions?
**Answer:** Split them by who is best placed to answer from their own role — the member who coordinates consolidation and corrections answers the questions about methodology and coordination, and the rest answer the questions tied to their own audit work.

## 5. If a Pull Request workflow deviates from what was planned (for example, everyone committing to one shared branch instead of separate branches), how should that be documented?
**Answer:** State plainly in the consolidated document what actually happened and why, instead of describing the workflow as if it went as originally planned, so the record stays accurate even if the team accepts the risk of not meeting a specific rule.

## 6. What should happen when one team member's audit finds a problem in another member's code that the responsible person didn't report themselves?
**Answer:** The finding should still be included in the consolidated audit, credited to whoever found it, and assigned to the person actually responsible for that code to correct in the next phase — it shouldn't be dropped just because it wasn't self-reported.

## 7. How should violations that depend on another member's code be organized before starting corrections?
**Answer:** Group them separately from independent violations, note exactly what the prerequisite change is and who makes it, and define an order so the dependent fix only happens after the prerequisite change is merged.

## 8. What should be done with a real bug found during an audit that isn't the GRASP pattern violation being looked for?
**Answer:** Record it separately as an out-of-scope functional defect instead of fixing it, since correcting it would change what the user observes, which goes against the requirement's rule of preserving current behavior.

# Requirement 7 - Phase 2 (GRASP Correction) - AI usage log

In Phase 2 the AI assistant was used in a guided mode: it never edited the repository. It explained each correction and gave the exact code and git commands, and I applied, compiled, tested and committed every change myself, one violation per `refactor:` commit with its own row in `docs/grasp-refactor-log.md`. It was also used to check the current state of the code against the latest uploaded version of the project before each instruction.

### Entry 1
- **Date:** 2026-10-14
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Build the "before" class diagram that opens Phase 2.
- **Query:** I asked for the before diagram of my own classes first.
- **Response summary:** It produced a Mermaid diagram of the Sales and UI classes only. I pointed out that the diagram is my responsibility for the whole team, so it was rebuilt to cover every class touched by the 34 consolidated violations, with a note citing the violation IDs on each class.
- **Decision:** Accepted the complete version. The scope mistake was in the first answer, not in the requirement.
- **Related commit:** d8be90e

### Entry 2
- **Date:** 2026-10-14
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Correct V-01 and V-02.
- **Query:** I asked for the exact change for each violation after it read the current `Sale` and `SaleDAO`.
- **Response summary:** For V-01 it proposed moving the receipt text into a new `SaleReceiptFormatter` that only uses `Sale`'s public getters, so the text stays identical. For V-02 it proposed wrapping the `IOException` in `PersistenceException`, copying the pattern already used in `save` and `findAll`, and keeping the `throws IOException` signatures so no caller changes.
- **Decision:** Applied both as proposed, in separate commits, and checked that the receipt text did not change.
- **Related commit:** 812aea6, 2dd7168, 20f7ef2, 321f17e

### Entry 3
- **Date:** 2026-10-15
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Define the no-regression verification strategy that is my responsibility.
- **Query:** My teammates told me the strategy belongs in `docs/grasp-refactor-log.md`, so I asked for it written like the previous steps.
- **Response summary:** It wrote the strategy (baseline on `develop`, re-check after each change, data files compared, final full run) and a 16-flow baseline checklist. It kept it separate from Developer 2's own checklist, which only covers his scope.
- **Decision:** Accepted and ran the baseline on `develop` before correcting my own violations.
- **Related commit:** fecb949

### Entry 4
- **Date:** 2026-10-15
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Unblock Developer 1 by adding `Sale.getDiscountRate` (V-09 prerequisite).
- **Query:** I asked which of my corrections Developer 1 was waiting for.
- **Response summary:** It identified FF-03 as the blocker and gave the method to add, plus the log row.
- **Decision:** Applied it first so Developer 1 could do his part of V-09.
- **Related commit:** c73fd80, 7913aa2

### Entry 5
- **Date:** 2026-10-15
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Correct V-34, the dependency of `SaleService` on `ProductDAO`.
- **Query:** I asked for the fix after reading the audit wording, which mentioned both `SaleService` and `SaleDAO`.
- **Response summary:** It reasoned from the `ui → service → persistence → model` rule: `SaleService` depending on a DAO is a violation, but `SaleDAO` depending on `ProductDAO` is persistence-to-persistence, the same as its existing dependency on `PersonDAO`. It also found that `ProductService` had no stock-reduction method and proposed adding `reduceStock`, mirroring `restoreStock`.
- **Decision:** Accepted. I changed only `SaleService`, documented in the log why `SaleDAO` was left alone, and told Developer 1 that I added a method to his service.
- **Related commit:** f76c8ab, 018b81e

### Entry 6
- **Date:** 2026-10-15
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Correct V-03 and V-04 in `SaleService.registerSale`.
- **Query:** I asked for the fix for both together, since they touch the same method. The project then failed to compile, so I uploaded it and asked what was wrong.
- **Response summary:** It split `registerSale` into private helper methods and replaced `instanceof Console` with `product.requiresWarranty()`. After compiling the uploaded project it found that the extracted `assignWarranties` method was missing `throws IOException`, which the original method did not need because the enclosing method already declared it.
- **Decision:** Accepted. I added the `throws IOException` and compiled again before committing.
- **Related commit:** 26f7ead, f6be7bb, a49cc0e

### Entry 7
- **Date:** 2026-10-15
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Correct V-05 and V-07 in `MainMenu`.
- **Query:** I asked for both once Developer 1's factory methods and `requiresWarranty()` were available.
- **Response summary:** It replaced the direct `new VideoGame/Console` with the `ProductService` factory methods. For V-07 it warned that, unlike `instanceof`, `requiresWarranty()` fails with a null pointer when the id belongs to an accessory (`findProductById` returns null), so it added an explicit null check.
- **Decision:** Applied both in separate commits and tested a sale with a console and a sale with only an accessory.
- **Related commit:** 7740088, a32b3ec, 7b0429a

### Entry 8
- **Date:** 2026-10-15
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Split `MainMenu` (V-06), the largest violation.
- **Query:** I asked for the correction after the rest of my violations were done.
- **Response summary:** It proposed one class per feature area plus a shared `MenuDialogs`, and gave one commit per piece so each step could be compiled and tested. After the `MenuDialogs` commit the project did not compile; I uploaded it and it found 13 remaining calls to `showPersistenceError()` inside the area methods that it had not told me to qualify. It then recommended removing the unused imports in one final commit instead of piece by piece, to avoid deleting one still needed by an area not yet extracted.
- **Decision:** Accepted. I replaced the 13 calls, applied each extraction and compiled before every commit. `MainMenu` went from 1105 to 120 lines, and I ran the full checklist afterwards.
- **Related commit:** 25a011b, 0e9810e, b20c333, 869544a, 3e7ef0c, 267ba80, 109f168, 5df8b64, 871aa47, 37220e7

### Entry 9
- **Date:** 2026-10-15
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Understand a rejected push and the editor that opened during a merge.
- **Query:** I pasted a screenshot of the terminal showing the merge message editor.
- **Response summary:** It explained that a teammate had pushed commits to the shared branch, that the pull created a merge commit, and how to save and leave the editor.
- **Decision:** Accepted. I confirmed the project still compiled after the merge before continuing.
- **Related commit:** -

### Entry 10
- **Date:** 2026-10-16
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Close my team-wide responsibilities: after diagram, final checklist, README and Pull Request description.
- **Query:** I uploaded the project with all three members' work and asked for the remaining documents.
- **Response summary:** It compiled the whole project first, then read the real classes to draw the after diagram. It also filled the final column of the checklist from the results I got by running every flow myself, updated the README sections that were out of date, and wrote a description for a Pull Request limited to my own contributions.
- **Decision:** Accepted. The results of the checklist are the ones I observed in NetBeans, not the assistant's.
- **Related commit:** (fill with `git log --oneline` for the after diagram, final checklist and README commits)
