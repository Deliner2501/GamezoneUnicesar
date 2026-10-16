# Leader SOLID Audit — Deiner Andrés De Luque Navarro

**Audited commit:** `develop` at `221b709` (GRASP refactor from Requirement 7
already integrated).

## Scope

As Technical Leader, my audit covers the classes I am responsible for per
`TEAM.md` and the requirement history. Class names in the Requirement 8
responsibility table map to the current repository as follows:

| Requirement 8 name | Class in the repository | Layer |
|---|---|---|
| `Sale` | `Sale` | model |
| `SaleRepository` | `SaleDAO` | persistence |
| `SaleService` | `SaleService` (including the accessory, promotion and warranty integrations) | service |
| `ConsoleMenu` | `MainMenu` plus the classes extracted from it in Requirement 7: `ProductMenu`, `PersonMenu`, `SaleMenu`, `AccessoryMenu`, `PromotionMenu`, `ReturnMenu`, `WarrantyMenu`, `MenuDialogs`, `SaleReceiptFormatter` | ui |
| `Main` | `Main` | root |
| `ProductService.restoreStock` | `ProductService.restoreStock` and `ProductService.reduceStock` (added by me in Requirement 7) | service |

Classes I created or modified during the GRASP correction (Requirement 7)
are included above: `SaleReceiptFormatter`, `MenuDialogs`, the seven menu
classes and the helper methods extracted from `SaleService.registerSale`.

Everything else belongs to Developer 1 or Developer 2 and is audited in
their reports. Problems that originate in their code are reported in the
"frontier findings" of the consolidated audit and are not audited here.

## Criteria used

| Verdict | Criterion |
|---|---|
| **Violation** | A concrete, checkable symptom of the principle being broken (class, method and quoted code from `develop`), with its real consequence. |
| **Correct** | The class and the place where the principle is met are named, with a one-line justification. |
| **Not applicable** | The class has no point of application for the principle, and the reason is stated (for example, "no inheritance relationship exists for LSP"). |

Principle symptoms searched for:

- **SRP:** a class or method with more than one reason to change.
- **OCP:** adding a new kind of item or area requires editing existing code
  (type-based branching, hard-coded lists).
- **LSP:** a subtype cannot replace its base type, or it throws or changes
  the contract.
- **ISP:** a client depends on methods it does not use.
- **DIP:** a high-level class depends on a concrete low-level class or on a
  layer above it, instead of an abstraction.