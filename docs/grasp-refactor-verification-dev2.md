# GRASP Refactor — Developer 2 No-Regression Verification

Manual checks run on `feature/grasp-refactor` after all Developer 2 corrections. Each check covers a flow touched by the refactor. "Expected" is the behavior before the refactor; the visible outcome must be the same, except that error dialogs now come from the typed exceptions.

Verified by: Developer 2 (Moises) on 2026-10-15.

Build check: `mvn clean compile` finishes with BUILD SUCCESS. Result: Pass.

| # | Flow | Steps | Expected | Violations covered | Result |
|---|---|---|---|---|---|
| 1 | Register customer (valid) | Persons > Register customer with valid name, id, phone and email | Success dialog; customer appears in the customer list | V-16 | Pass |
| 2 | Register customer (invalid email) | Same flow with an invalid email | Error dialog; customer is not saved | V-16 | Pass |
| 3 | List customers and sellers | Persons > list customers; list sellers | Both lists show the data from the CSV files | V-15, V-16 | Pass |
| 4 | Register accessory (valid) | Accessories > register a cable with valid data | Success dialog; cable appears in the list | V-17, V-21 | Pass |
| 5 | Register accessory (negative price) | Register a controller with a negative price | Error dialog about invalid data; nothing saved | V-20, V-21 | Pass |
| 6 | List accessories by type | Accessories > list by type CABLE, then by `controller` (lowercase) | Only accessories of that type are listed, in both cases | V-19 | Pass |
| 7 | Register compatibility (missing accessory) | Register compatibility for an accessory id that does not exist | Error dialog saying the resource was not found | V-21 | Pass |
| 8 | Register promotion (percentage 150) | Promotions > register a percentage discount of 150 | Error dialog about invalid data; nothing saved | V-24, V-25 | Pass |
| 9 | Register and list promotion (valid) | Register a valid percentage promotion, then list all promotions | Promotion appears in the list; CSV line keeps the same format | V-22, V-25 | Pass |
| 10 | View all returns | Returns > view all returns | Same returns as before the refactor | V-26, V-27 | Pass |
| 11 | Register return with a console | Returns > register a return of a recent sale that includes a console | Success dialog; refund includes the warranty cost; return appears in the list; stock restored | V-28, V-29 | Pass |
| 12 | Register return (product not in sale) | Register a return with a product id that is not in the sale | Error dialog saying the product does not belong to the sale | V-30 | Pass |
| 13 | Register return (missing sale) | Register a return with a sale id that does not exist | Error dialog saying the resource was not found | V-30 | Pass |
| 14 | Warranties | Warranties > list all; list expiring soon with a valid number; list expiring soon with a negative number | Lists show the same warranties; the negative number gives an error dialog about invalid data | V-31, V-32, V-33 | Pass |
| 15 | CSV format | After the checks, compare `data/accessories.csv`, `data/promotions.csv`, `data/returns.csv` and `data/warranties.csv` with `git diff` | Existing lines are unchanged; only the lines created by the checks were added | V-17, V-22, V-26, V-31 | Pass |