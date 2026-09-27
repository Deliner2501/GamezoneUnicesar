# AI Usage Log — Developer 1

---

## Entry 1
- **Date:** 2026-09-25
- **Tool:** Claude
- **Phase and branch:** Accessory module, feature/accessory-module
- **Objective:** Implement the Accessory hierarchy (Accessory abstract class, Controller, Cable, Memory) extending Product.
- **Query:** Requested the model-layer classes for the accessory module, following the team's class diagram (console compatibility list, getFullDescription per subtype).
- **Response summary:** Provided the four classes with console-compatibility tracking in the abstract class and type-specific attributes in each subclass.
- **Decision:** Accepted as provided; added a setter for the compatible console list and validation improvements afterward to reach the minimum commit count.
- **Related commit:** multiple (Accessory/Controller/Cable/Memory implementation)

## Entry 2
- **Date:** 2026-09-25
- **Tool:** Claude
- **Phase and branch:** Promotion module, feature/promotion-module
- **Objective:** Implement the Promotion hierarchy (Promotion abstract class, PercentageDiscount, CategoryDiscount, BulkPurchaseDiscount).
- **Query:** Requested the model-layer classes following the team's class diagram, including calculateDiscount() logic specific to each promotion type.
- **Response summary:** Provided the four classes; CategoryDiscount was implemented to sum only the prices of products matching the target category (VIDEOGAME or CONSOLE) using instanceof checks.
- **Decision:** Accepted as provided; added constructor validation afterward as a separate improvement commit.
- **Related commit:** multiple (Promotion/PercentageDiscount/CategoryDiscount/BulkPurchaseDiscount implementation)

## Entry 3
- **Date:** 2026-09-26
- **Tool:** Claude
- **Phase and branch:** Return module, feature/return-module
- **Objective:** Implement the Return class and the additive canBeReturned() method in Sale.
- **Query:** Requested the Return class (referencing an original sale, a subset of returned products, refund calculation, Spanish receipt) and the 30-day validation method in Sale.
- **Response summary:** Provided Return with no setters for immutable relationships, and canBeReturned() using ChronoUnit.DAYS between the sale date and the current date.
- **Decision:** Accepted as provided; later added constructor validation and an overload of canBeReturned() accepting a reference date, for testability.
- **Related commit:** multiple (Return implementation, Sale.canBeReturned)

## Entry 4
- **Date:** 2026-09-26
- **Tool:** Claude
- **Phase and branch:** Warranty module, feature/warranty-module
- **Objective:** Implement the Warranty hierarchy (Warranty abstract class, BasicWarranty, ExtendedWarranty).
- **Query:** Requested the model-layer classes, where the end date is calculated inside the constructor by calling the abstract getDurationInMonths() method.
- **Response summary:** Provided the three classes; discussed how calling an abstract method from the base constructor resolves polymorphically to the concrete subclass being built.
- **Decision:** Accepted as provided; added constructor validation and a getRemainingDays() helper method afterward.
- **Related commit:** multiple (Warranty/BasicWarranty/ExtendedWarranty implementation)

## Entry 5
- **Date:** 2026-09-27
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/accessory-category-discount (A1)
- **Objective:** Understand what changes A1 required across layers, since the responsibility table assigned the full branch to Developer 1, not just the model layer.
- **Query:** Asked whether A1 was only a model-layer change (CategoryDiscount) or required touching PromotionService and MainMenu as well.
- **Response summary:** Confirmed that per the team's responsibility table, A1 was assigned entirely to Developer 1 across all layers, unlike the module requirements which were split strictly by layer.
- **Decision:** Accepted that A1 required changes in CategoryDiscount, PromotionService, MainMenu, and data/promotions.csv.
- **Related commit:** e97aeec

## Entry 6
- **Date:** 2026-09-27
- **Tool:** Claude
- **Phase and branch:** Phase 2, feature/accessory-category-discount (A1)
- **Objective:** Add "ACCESSORY" as a valid target category in CategoryDiscount, PromotionService, and the console menu, plus a preloaded promotion in data/promotions.csv.
- **Query:** Requested the updated CategoryDiscount code, a validateTargetCategory method in PromotionService, the updated menu prompt, and the CSV line for a promotion valid during the current work week.
- **Response summary:** Provided the updated CategoryDiscount (matchesCategory using instanceof Accessory), the PromotionService validation method, the updated MainMenu prompt, and a CSV line with a date range (2026-09-27 to 2026-10-04) covering the current week, since the exact defense date was unknown.
- **Decision:** Accepted all as provided.
- **Related commit:** e97aeec, 53f06d4, bae5473

## Entry 7
- **Date:** 2026-09-27
- **Tool:** Claude
- **Phase and branch:** Phase 4, fix/return-discounted-refund (A5)
- **Objective:** Fix Return.calculateRefundAmount so it does not over-refund when the original sale had a promotion discount.
- **Query:** Requested the full updated Return class implementing a proportional refund based on the original sale's discount rate.
- **Response summary:** Provided the full class: calculateRefundAmount now computes a discountRate from Sale.getDiscountAmount()/Sale.getTotal() and applies it to each returned item's price; generateReturnReceipt updated to show list price, proportional discount, and refunded amount per item.
- **Decision:** Accepted as provided.
- **Related commit:** 90a2ea7