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

  
---

## 1. `Sale.java`

### SRP — Correct

`Sale` has one reason to change: the business data and calculations of a
sale. Its methods all work on its own state: `addProduct` (line 69),
`calculateTotal` (line 83), `applyPromotion` (line 172), `addAdditionalCost`
(line 230), `getDiscountRate` (line 208) and `getFinalTotal` (line 217). The
receipt text was already moved out to `SaleReceiptFormatter` in
Requirement 7; the only receipt-related code left in the class is the
delegating method `generateReceipt()` (line 256), whose problem is a
dependency direction issue and is reported under DIP (V-01), not SRP.
The 30-day return window `canBeReturned()` (lines 146–161) is audited by
Developer 1, as assigned in the responsibility table.

### OCP — Correct

`calculateTotal()` (lines 83–90) sums `product.getPrice()` over a
`List<Product>`. It never checks the concrete type of an item, so a new
kind of sellable item (a new `Product` or `Accessory` subtype) is added
without modifying `Sale`. The promotion is received already calculated
through `applyPromotion(String, double)` (line 172), so `Sale` is also
closed to new promotion types.

### LSP — Not applicable

`Sale` is a leaf class: `public class Sale` (line 15) has no `extends`, and
nothing extends it, so there is no substitution relationship to audit in
this class. As a client of the `Product` hierarchy, `Sale` only uses
`getPrice()` and `getId()`, which every subtype (including `Accessory`)
honors, so substitution of subtypes inside `Sale.products` is safe. The
hierarchy itself is audited by Developer 1.

### ISP — Not applicable

`Sale` does not implement or declare any interface (line 15), so there is
no interface whose methods a client could be forced to depend on. Its
clients (`SaleReceiptFormatter`, `SaleDAO`, `SaleService`) call only the
getters they need.

### DIP — Violation (V-01)

**V-01 — The domain class `Sale` depends on the UI layer through
`generateReceipt()`**
- **Principle:** Dependency Inversion Principle
- **Module:** `Sale.generateReceipt()`, `Sale.java` lines 256–258:
```java
  public String generateReceipt() {
      return com.gamezone.ui.SaleReceiptFormatter.format(this);
  }
```
- **Evidence:** A `model` class references the concrete class
  `com.gamezone.ui.SaleReceiptFormatter` (fully qualified, so there is no
  import). `grep` for `import com.gamezone.ui` in `model`, `service` and
  `persistence` returns nothing, because this reference is the only one and
  it is written inline. The dependency also appears in the JavaDoc link at
  line 250. The formatter in turn depends on `Sale`
  (`SaleReceiptFormatter.format(Sale)`), so `model` and `ui` depend on each
  other.
- **Explanation:** The model is the highest-level, most stable layer of the
  system and must not depend on a concrete detail of the presentation
  layer. The required layer direction is `ui → service → persistence →
  model`; this call goes the opposite way. It was introduced when V-01 of
  the GRASP audit (receipt text inside `Sale`) was corrected by moving the
  text to `SaleReceiptFormatter` but leaving a delegating method in the
  model.
- **Consequence:** `Sale` cannot be compiled, tested or reused without the
  `ui` package. Any change in the formatter's package or signature breaks
  the domain class, and the model can never be shipped to a context without
  a UI.
- **Proposed solution:** Remove `generateReceipt()` from `Sale` and make its
  only two callers call the formatter directly: `SaleMenu.registerSale()`
  (line 115) and `SaleMenu.printSales()` (line 161) would use
  `SaleReceiptFormatter.format(sale)`. The receipt text and every message
  shown to the user stay identical.
- **Code of another member:** No — `Sale`, `SaleMenu` and
  `SaleReceiptFormatter` are all in my own scope. `grep` shows no other
  class calls `generateReceipt()`.

### Note — functional defect found (out of scope for this requirement)

`Sale.getDiscountRate()` (line 209) returns `discountAmount / total`
without checking `total == 0`, while its JavaDoc (lines 205–206) promises
`0.0` when the subtotal is zero. This is a mismatch between contract and
code, not a SOLID violation, and fixing it would change observable
behavior (`Return` has its own guard today, so the practical impact is
limited). It is recorded as an **out-of-scope functional defect** for the
consolidated audit and is not corrected in Phase 2. Developer 1 already
reported the same difference when applying V-09 of Requirement 7, so it is
not a new finding for that requirement.