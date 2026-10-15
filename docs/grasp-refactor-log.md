# GRASP Refactor Log — Phase 2

| Class | Change made | Violation ID | Pattern applied | Commit/PR | Responsible |
|---|---|---|---|---|---|
| `Sale`, `SaleReceiptFormatter` (new) | Extracted `generateReceipt()`'s body into a new `SaleReceiptFormatter.format(Sale)` Pure Fabrication; `Sale.generateReceipt()` now delegates to it | V-01 | Pure Fabrication / Protected Variations | `refactor: extract receipt formatting to SaleReceiptFormatter (V-01)` | Technical Leader |
| `SaleDAO` | Wrapped `IOException` as `PersistenceException` in `findByCustomer`/`findBySeller`, consistent with `save`/`findAll` | V-02 | Protected Variations | `fix: wrap IOException as PersistenceException in SaleDAO findByCustomer/findBySeller (V-02)` | Technical Leader |