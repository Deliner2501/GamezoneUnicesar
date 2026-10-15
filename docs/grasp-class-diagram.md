# GRASP Class Diagram — Before / After Phase 2

## Before (start of Phase 2)

All classes involved in the 34 violations of the consolidated audit
(`docs/grasp-audit/consolidated-audit.md`), across all three members'
scopes, with the violation IDs that touch each one.

```mermaid
classDiagram
    %% ===== Sales & UI (Technical Leader) =====
    class Sale {
        -products: List~Product~
        -total: double
        -discountAmount: double
        -additionalCost: double
        +addProduct(Product)
        +canBeReturned() bool
        +getFinalTotal() double
        +generateReceipt() String
        +getDiscountRate() double
    }
    note for Sale "V-01: generateReceipt() builds presentation text\nV-09 (needs): getDiscountRate() for Return to reuse"

    class SaleDAO {
        -personDAO: PersonDAO
        -productDAO: ProductDAO
        +save(Sale)
        +findAll() List~Sale~
        +findByCustomer(String) List~Sale~
        +findBySeller(String) List~Sale~
        -fromLine(String) Sale
    }
    note for SaleDAO "V-02: findByCustomer/findBySeller leak IOException\nV-34: depends on ProductDAO directly"

    class SaleService {
        -saleDAO: SaleDAO
        -productDAO: ProductDAO
        -accessoryDAO: AccessoryDAO
        -promotionService: PromotionService
        -warrantyService: WarrantyService
        -personService: PersonService
        +registerSale(...) Sale
    }
    note for SaleService "V-03: registerSale does 9 steps\nV-04: instanceof Console\nV-34: depends on ProductDAO directly"

    class MainMenu {
        -personService: PersonService
        -productService: ProductService
        -saleService: SaleService
        -accessoryService: AccessoryService
        -promotionService: PromotionService
        -warrantyService: WarrantyService
        -returnService: ReturnService
        +registerVideoGame()
        +registerConsole()
        +registerSale()
    }
    note for MainMenu "V-05: creates VideoGame/Console directly\nV-06: 7 feature areas in one class\nV-07: instanceof Console"

    %% ===== Products (Developer 1) =====
    class Product {
        -stock: int
        +reduceStock(int)
        +hasEnoughStock(int) bool
        +requiresWarranty() bool
    }
    note for Product "V-08 (needs): hasEnoughStock() as single source\nFF-01 (needs): requiresWarranty()"

    class VideoGame
    class Console
    VideoGame --|> Product
    Console --|> Product

    class ProductDAO {
        +writeAll(List~Product~)
        +parseLine(String) Product
        +findAll() List~Product~
    }
    note for ProductDAO "V-11: println + catch(Exception) in findAll\nV-12: instanceof VideoGame/Console"

    class ProductService {
        -productDAO: ProductDAO
        +checkStock(String, int) bool
        +restoreStock(String, int)
        +registerVideoGame(...)
        +registerConsole(...)
    }
    note for ProductService "V-08: duplicates stock-check rule\nV-10: depends on concrete ProductDAO\nFF-02 (needs): factory-style registration methods"

    class ProductValidator {
        +validateStockAvailability(Product, int)
    }
    note for ProductValidator "V-08: duplicates stock-check rule"

    %% ===== Accessories (Developer 1 model / Developer 2 service+DAO) =====
    class Accessory {
        -compatibleConsoleIds: List~String~
        +getCompatibleConsoleIds() List~String~
        +toCsvLine() String
        +getTypeCode() String
    }
    Accessory --|> Product
    note for Accessory "V-14: exposes mutable internal list\nFF-05 (needs): toCsvLine()/getTypeCode()"

    class Controller
    class Cable
    class Memory
    Controller --|> Accessory
    Cable --|> Accessory
    Memory --|> Accessory

    class AccessoryDAO {
        +writeAll(List~Accessory~)
        +findAll()
    }
    note for AccessoryDAO "V-17: instanceof Controller/Cable/Memory\nV-18: swallows IOException"

    class AccessoryService {
        -validateAccessory(Accessory)
        +matchesType(Accessory, String) bool
    }
    note for AccessoryService "V-19: instanceof-based matchesType\nV-20: validation as private method\nV-21: raw IllegalArgumentException"

    %% ===== Promotions (Developer 1 model / Developer 2 service+DAO) =====
    class Promotion {
        +toCsvLine() String
    }
    class PercentageDiscount
    class CategoryDiscount {
        +matchesCategory(Product) bool
    }
    class BulkPurchaseDiscount
    PercentageDiscount --|> Promotion
    CategoryDiscount --|> Promotion
    BulkPurchaseDiscount --|> Promotion
    note for CategoryDiscount "V-13: instanceof-based category match\nFF-06 (needs): toCsvLine()/category enum"

    class PromotionDAO {
        +saveAll(List~Promotion~)
    }
    note for PromotionDAO "V-22: instanceof branching for serialization\nV-23: swallows IOException"

    class PromotionService {
        -validatePercentage(double)
        -validateTargetCategory(String)
    }
    note for PromotionService "V-24: validation as private methods\nV-25: raw IllegalArgumentException"

    %% ===== Returns (Developer 1 model / Developer 2 service+repository) =====
    class Return {
        -returnedProducts: List~Product~
        +calculateRefundAmount() double
        +getReturnedProducts() List~Product~
    }
    note for Return "V-09: duplicates Sale's discount-rate formula\nV-14: exposes mutable internal list"

    class ReturnRepository {
        -saleService: SaleService
        -productService: ProductService
        -accessoryService: AccessoryService
        +fromLine(String) Return
    }
    note for ReturnRepository "V-26: persistence depends on 3 services (inverted layering)\nV-27: silently skips via println"

    class ReturnService {
        +registerReturn(...) Return
    }
    note for ReturnService "V-28: registerReturn does 9 steps\nV-29: instanceof Console/Accessory\nV-30: raw IllegalArgumentException"

    %% ===== Warranties (Developer 1 model / Developer 2 service+repository) =====
    class Warranty {
        +getWarrantyType() String
    }
    class BasicWarranty
    class ExtendedWarranty
    BasicWarranty --|> Warranty
    ExtendedWarranty --|> Warranty

    class WarrantyRepository {
        +toLine(Warranty) String
        +saveAll(List~Warranty~)
        +loadAll()
    }
    note for WarrantyRepository "V-31: instanceof ExtendedWarranty\nV-32: raw IOException propagated"

    class WarrantyService {
        +resolveWarranty(...)
    }
    note for WarrantyService "V-33: raw IllegalArgumentException + silent println"

    %% ===== People (Developer 2) =====
    class Person
    class Customer {
        -purchaseHistory: List~Sale~
        +getPurchaseHistory() List~Sale~
    }
    class Seller
    Customer --|> Person
    Seller --|> Person
    note for Customer "V-15: exposes mutable internal list"

    class PersonDAO
    note for PersonDAO "V-16: dead throws IOException declarations"

    %% ===== Relationships =====
    SaleService --> SaleDAO
    SaleService --> ProductDAO : V-34
    SaleService --> AccessoryDAO
    SaleService --> PromotionService
    SaleService --> WarrantyService
    SaleService --> PersonService
    SaleDAO --> ProductDAO
    SaleDAO --> PersonDAO
    MainMenu --> SaleService
    MainMenu --> ProductService
    MainMenu ..> VideoGame : creates (V-05)
    MainMenu ..> Console : creates (V-05)
    ProductService --> ProductDAO
    ProductService --> ProductValidator
    AccessoryService --> AccessoryDAO
    PromotionService --> PromotionDAO
    ReturnRepository --> SaleService : V-26
    ReturnRepository --> ProductService : V-26
    ReturnRepository --> AccessoryService : V-26
    ReturnService --> ReturnRepository
    ReturnService --> WarrantyService
    WarrantyService --> WarrantyRepository
    WarrantyService --> SaleDAO
    PersonService --> PersonDAO
    Sale "1" *-- "many" Product
    Return --> Sale
    Customer "1" *-- "many" Sale
```

## After (end of Phase 2)

_To be completed once all Phase 2 corrections are merged, showing the
same classes with new/removed methods and relationships per each
violation's applied fix._