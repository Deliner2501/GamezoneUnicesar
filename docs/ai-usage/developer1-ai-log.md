# AI Usage Log — Developer 1 (Product Module)

**Student:** Ronald

---

1. **"What exactly do I get as Developer 1 of the product module, according to the workshop document?" **

The AI explained to me that my responsibility is 5 classes through the 3 layers (model, persistence, service): the abstract class of products, their two derivatives, persistence and service - and that the rest (people, sales, the interface and the Main) is the responsibility of my colleagues.

2. **"How do I clone the repository and create my own branch without touching main or develop directly?" **

I was guided step by step in PowerShell: git clone, git checkout develop, git pull origin develop, and then git checkout -b feature/product-module to create my work branch following the Git Flow model required by the workshop.

3. **"I made git commit but the code on GitHub appears empty, with only the template that generates NetBeans. What happened?" **

We identified that he had done git add/commit before actually saving the file in NetBeans (Ctrl+S). I learned to always check with cat archivo.java in PowerShell before commiting, to confirm that the content on the disk matched what I saw in the editor.

4. **"When I push my new branch, it didn't appear on GitHub. Why?" **

The branch existed only locally because the first push had never been made. I used git push -u origin feature/product-module to create it on the remote and link it with my local branch.

5. **"The class diagram sent by the leader does not have setters in Product, but my code does. Should I leave them?" **

We compared the official diagram against my code and confirmed that the diagram only defined getters (plus reduceStock() to modify the stock), so I removed the four setters to keep the class faithful to the design agreed by the team.

6. **"When compiling the complete project I get error: release version 26 not supported. Is it my mistake?" **

The AI explained to me that this error comes from the maven.compiler.release configuration in the pom.xml, a general configuration file of the project that it is not up to me to modify - I warned the leader to adjust it.

7. **"A colleague suggested that the leader work on a separate project and copy the code later to avoid Git conflicts. Is that necessary?" **

I asked the AI if that made technical sense. He explained to me that Git merges files, not folders, so two modules with folders of the same name (persistence, for example) do not generate conflict if they have different files - the team decided to continue working directly on the real repo.

9. **"What do the prefixes feat:, fix:, refactor: mean in the commits, and when is each one used?" **

He explained to me the Conventional Commits convention that the workshop requires: feat: for new functionality, fix: to correct errors, refactor: to reorganize without changing behavior, and docs:/chore: for documentation and maintenance.




---

## Phase 1 - Audit (feature/grasp-audit)

### Entry 1
- **Date:** 2026-10-13
- **Tool:** Claude
- **Phase and branch:** Phase 1, feature/grasp-audit
- **Objective:** Understand Requirement 7, its two phases and my role as Developer 1.
- **Query:** I shared the Requirement 7 PDF and asked what I had to do and in what order.
- **Response summary:** It summarized the two phases, my audit scope (products, accessories, promotions, Return and Sale.canBeReturned, warranties, exceptions and validation) and the deliverables (individual audit, orienting questions, AI log), and explained the nine GRASP patterns with a key question for each.
- **Decision:** Accepted. I confirmed the scope against the responsibility table of the PDF and used the key questions as a checklist for the audit.
- **Related commit:** -

### Entry 2
- **Date:** 2026-10-13
- **Tool:** Claude
- **Phase and branch:** Phase 1, feature/grasp-audit
- **Objective:** Know which version of the code had to be audited.
- **Query:** I asked how to check whether Requirement 6 was already merged into develop and which commit to audit.
- **Response summary:** It gave git fetch, git log and git grep commands, and told me to compare the source tree of the audit branch with develop.
- **Decision:** Accepted. The audit is based on commit a377cfb, whose src tree is identical to develop at d669eed (git diff showed no source differences).
- **Related commit:** f207ad8

### Entry 3
- **Date:** 2026-10-13
- **Tool:** Claude
- **Phase and branch:** Phase 1, feature/grasp-audit
- **Objective:** Audit the product module against the nine patterns.
- **Query:** I pasted Product, VideoGame, Console, ProductDAO and ProductService and asked for the candidate findings per pattern.
- **Response summary:** It proposed violations in the duplicated stock rule, the instanceof chain in ProductDAO, the dependency of ProductService on the concrete ProductDAO and the console output inside ProductDAO, and marked Creator, Controller, Pure Fabrication and Indirection as correct. I ran Select-String commands to get the exact lines.
- **Decision:** Accepted the findings and used the lines returned by Select-String as evidence. The use of ProductDAO inside SaleService and the creation of VideoGame and Console inside MainMenu were not audited by me and were reported to the Technical Leader as boundary findings.
- **Related commit:** 64c8c7e, 7d90fae

### Entry 4
- **Date:** 2026-10-13
- **Tool:** Claude
- **Phase and branch:** Phase 1, feature/grasp-audit
- **Objective:** Audit the accessory, promotion, return, warranty, exception and validation classes.
- **Query:** I pasted the source of those classes and asked for the findings and the correct applications of each pattern.
- **Response summary:** It proposed three additional violations: Return computing the discount rate from data owned by Sale, CategoryDiscount selecting the category with instanceof and text codes, and Accessory and Return exposing their internal mutable lists. It also listed where Polymorphism, Pure Fabrication, Indirection and Protected Variations are correctly applied.
- **Decision:** Accepted. I kept as evidence the class, the method and the code fragment of each finding.
- **Related commit:** 64c8c7e, 7d90fae, bee37d8, 9265cf1

### Entry 5
- **Date:** 2026-10-14
- **Tool:** Claude
- **Phase and branch:** Phase 1, feature/grasp-audit
- **Objective:** Answer the orienting question assigned to me.
- **Query:** I asked which questions were pending in grasp-analysis.md and for an answer to question 4, based on the no-regression section of the PDF.
- **Response summary:** It detected that question 4 was missing and drafted the answer: a baseline checklist run on develop, one violation per commit, verification on every Pull Request, comparison of the data files and the evidence shown at the end.
- **Decision:** Accepted, and I asked the Technical Leader to confirm that it matches the strategy of the refactor log.
- **Related commit:** a8af1b0

## Phase 2 - Correction (feature/grasp-refactor)

No entries yet.

### Entry 1
- **Date:** 2026-10-14
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Understand the consolidated audit and in what order my violations had to be corrected.
- **Query:** I pasted the lines of consolidated-audit.md assigned to Developer 1 and the frontier findings, and asked what each one required and what depended on what.
- **Response summary:** It explained which violations were mine (V-08 to V-14), which prerequisites I owed to the Leader and Developer 2 (FF-01, FF-02, FF-05, FF-06) and why V-09 and V-14 stay blocked until FF-03 and FF-07 are done.
- **Decision:** Accepted the order: prerequisites first so my teammates are unblocked.
- **Related commit:** -

### Entry 2
- **Date:** 2026-10-14
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Understand how to remove instanceof checks and duplicated rules in the Product, Promotion and Accessory hierarchies without changing behavior.
- **Query:** I asked why an abstract method in the base class solves the instanceof problem, how to keep the CSV format identical, and what exactly to change in each class.
- **Response summary:** It explained Polymorphism and Information Expert for these cases and proposed the changes (requiresWarranty, registerVideoGame/registerConsole, ProductCategory with getCategory, toCsvLine, getTypeCode, hasEnoughStock), showing the before and after of each file.
- **Decision:** Applied each proposal myself in its own commit and checked BUILD SUCCESS. Left out the parts that belong to Developer 2 and the Leader. When a method was pasted in the wrong place in CategoryDiscount, I replaced the class with the complete version it gave me.
- **Related commit:** 0cfe78f, 3a561dd, 1d908d5, 297f07d, f71384e, 53c8503, 0353999

### Entry 3
- **Date:** 2026-10-14
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Understand how to correct V-10, V-11 and V-12 in ProductDAO and ProductService.
- **Query:** I pasted ProductDAO and Main and asked how an injected logger, per-type CSV mappers and a repository interface fit together. Replacing the code piece by piece was giving me errors, so I asked for the complete classes.
- **Response summary:** It explained the roles of the logger, the mappers and the interface, and gave the complete ProductDAO and ProductService for each commit, keeping the out-of-scope defects (F-02, F-03) untouched.
- **Decision:** Accepted. I built the project before each commit and used git log to check that nobody else had modified ProductService.
- **Related commit:** 96fc2e0, c41916a, c3e9a89

### Entry 4
- **Date:** 2026-10-14
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/grasp-refactor
- **Objective:** Understand Git errors during the work.
- **Query:** I pasted the output of rejected pushes and the vim screen of a merge and asked what was happening.
- **Response summary:** It explained that the remote had new commits, that git pull was needed first and how to leave vim.
- **Decision:** Accepted.
- **Related commit:** -