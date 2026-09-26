# Promotion Module - Class Diagram
```mermaid
classDiagram
    %% ===================== EXISTING MODEL LAYER (for context) =====================
    class Product {
        <<abstract>>
        -String id
        -String title
        -double price
        -int stock
        +getId() String
        +getPrice() double
        +getFullDescription()* String
    }

    class VideoGame {
        +getFullDescription() String
    }

    class Console {
        +getFullDescription() String
    }

    class Sale {
        -String id
        -LocalDate date
        -Customer customer
        -Seller seller
        -List~Product~ products
        -double total
        -String appliedPromotionName
        -double discountAmount
        +addProduct(product Product) void
        +calculateTotal() double
        +getProducts() List~Product~
        +getAppliedPromotionName() String
        +setAppliedPromotionName(name String) void
        +getDiscountAmount() double
        +setDiscountAmount(amount double) void
        +generateReceipt() String
    }

    %% ===================== NEW MODEL LAYER =====================
    class Promotion {
        <<abstract>>
        -String id
        -String name
        -LocalDate startDate
        -LocalDate endDate
        +isActive(date LocalDate) boolean
        +calculateDiscount(sale Sale)* double
    }

    class PercentageDiscount {
        -double percentage
        +calculateDiscount(sale Sale) double
    }

    class CategoryDiscount {
        -double percentage
        -String targetCategory
        +calculateDiscount(sale Sale) double
    }

    class BulkPurchaseDiscount {
        -int minQuantity
        -double percentage
        +calculateDiscount(sale Sale) double
    }

    %% ===================== NEW PERSISTENCE LAYER =====================
    class PromotionDAO {
        +saveAll(promotions List~Promotion~) void
        +loadAll() List~Promotion~
    }

    %% ===================== NEW SERVICE LAYER =====================
    class PromotionService {
        -PromotionDAO promotionDAO
        +registerPercentageDiscount(...) PercentageDiscount
        +registerCategoryDiscount(...) CategoryDiscount
        +registerBulkPurchaseDiscount(...) BulkPurchaseDiscount
        +listAllPromotions() List~Promotion~
        +listActivePromotions() List~Promotion~
        +findBestPromotionFor(sale Sale) Promotion
        +findById(id String) Promotion
    }

    %% ===================== EXISTING SERVICE LAYER (for context) =====================
    class SaleService {
        -SaleDAO saleDAO
        -ProductDAO productDAO
        +registerSale(...) Sale
    }

    class MainMenu {
        -PromotionService promotionService
        +showPromotionMenu() void
    }

    %% ===================== RELATIONSHIPS =====================
    Product <|-- VideoGame
    Product <|-- Console
    Promotion <|-- PercentageDiscount
    Promotion <|-- CategoryDiscount
    Promotion <|-- BulkPurchaseDiscount

    CategoryDiscount ..> Product : reads price of matching category
    CategoryDiscount ..> VideoGame : checks instanceof
    CategoryDiscount ..> Console : checks instanceof

    Promotion ..> Sale : evaluates

    PromotionDAO ..> Promotion : depends on
    PromotionService --> PromotionDAO : uses
    PromotionService ..> Promotion : depends on
    PromotionService ..> Sale : evaluates via findBestPromotionFor

    SaleService --> PromotionService : uses (additive)
    SaleService ..> Sale : creates and updates
    MainMenu --> PromotionService : uses