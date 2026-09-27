# Warranty Module - Class Diagram
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

    class Console {
        -String brand
        -String model
        -String generation
        +getFullDescription() String
    }

    class Sale {
        -String id
        -LocalDate date
        -List~Product~ products
        -double total
        +getId() String
        +getDate() LocalDate
    }

    %% ===================== NEW MODEL LAYER =====================
    class Warranty {
        <<abstract>>
        -String id
        -Product product
        -Sale sale
        -LocalDate startDate
        -LocalDate endDate
        +getId() String
        +getProduct() Product
        +getSale() Sale
        +getStartDate() LocalDate
        +getEndDate() LocalDate
        +getDurationInMonths()* int
        +getWarrantyType()* String
        +getAdditionalCost()* double
        +isActive(date LocalDate) boolean
        +generateWarrantyCertificate() String
    }

    class BasicWarranty {
        +getDurationInMonths() int
        +getWarrantyType() String
        +getAdditionalCost() double
    }

    class ExtendedWarranty {
        +getDurationInMonths() int
        +getWarrantyType() String
        +getAdditionalCost() double
    }

    %% ===================== NEW PERSISTENCE LAYER =====================
    class WarrantyRecord {
        <<DTO>>
        -String type
        -String id
        -String productId
        -String saleId
        -LocalDate startDate
    }

    class WarrantyRepository {
        +saveAll(warranties List~Warranty~) void
        +loadAll() List~WarrantyRecord~
    }

    %% ===================== NEW SERVICE LAYER =====================
    class WarrantyService {
        -WarrantyRepository warrantyRepository
        -SaleDAO saleDAO
        -ProductService productService
        +assignBasicWarranty(product Product, sale Sale, startDate LocalDate) BasicWarranty
        +assignExtendedWarranty(product Product, sale Sale, startDate LocalDate) ExtendedWarranty
        +findWarrantyByProduct(productId String, saleId String) Warranty
        +listAllWarranties() List~Warranty~
        +listActiveWarranties() List~Warranty~
        +listWarrantiesExpiringSoon(daysAhead int) List~Warranty~
    }

    %% ===================== EXISTING PERSISTENCE/SERVICE LAYER (for context) =====================
    class SaleDAO {
        +findAll() List~Sale~
    }

    class SaleService {
        -SaleDAO saleDAO
        -WarrantyService warrantyService
        +setWarrantyService(warrantyService WarrantyService) void
        +registerSale(..., productIdsWithExtendedWarranty List~String~) Sale
    }

    class ConsoleMenu {
        -WarrantyService warrantyService
        +warrantyMenu() void
    }

    %% ===================== RELATIONSHIPS =====================
    Product <|-- Console
    Warranty <|-- BasicWarranty
    Warranty <|-- ExtendedWarranty

    Warranty "many" --> "1" Product : covers (association)
    Warranty "many" --> "1" Sale : references (association)

    WarrantyRepository ..> Warranty : persists (writes)
    WarrantyRepository ..> WarrantyRecord : reads/returns (no Sale/Product resolution)

    WarrantyService --> WarrantyRepository : uses
    WarrantyService --> SaleDAO : resolves Sale references
    WarrantyService --> ProductService : resolves Product references
    WarrantyService ..> WarrantyRecord : resolves into Warranty
    WarrantyService ..> BasicWarranty : creates
    WarrantyService ..> ExtendedWarranty : creates

    SaleService --> WarrantyService : assigns warranties (setter injection, breaks the cycle)
    SaleService ..> Console : checks instanceof

    ConsoleMenu --> WarrantyService : uses

    note for WarrantyRepository "A2 fix: no longer depends on\nSaleService or ProductService.\nOnly reads/writes raw ids."
    note for WarrantyService "A2 fix: now depends on SaleDAO\n(not SaleService) to resolve sales,\nbreaking the circular dependency\nSaleService -> WarrantyService ->\nWarrantyRepository -> SaleService."
```