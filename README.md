# GameZoneUnicesar

GameZoneUnicesar is a Java-based data management system developed for a video game and console store. The system allows the store to manage its products, customers, sellers, sales, accessories, promotions, returns, and warranties through a graphical user interface built with Java Swing.

The project was developed using object-oriented programming principles and a four-layer architecture: **Model, Persistence, Service, and UI**.

---

## Features

The system provides the following main functionalities:

### Product Management

* Register video games.
* Register consoles.
* List available products.
* Manage product information such as ID, title, price, stock, and type-specific characteristics.
* Automatically update product stock after a sale.

### Customer and Seller Management

* Register customers.
* List registered customers.
* List registered sellers.
* Search customers and sellers by their ID when processing sales.

### Accessory Management

* Register controllers, cables, and memories, each with its own specific attributes.
* Register console compatibility for controllers and memories.
* List all registered accessories.
* List accessories filtered by type.
* Query accessories compatible with a specific console.
* Sell accessories together with products (video games and consoles) in the same transaction.
* Automatically update accessory stock after a sale.

### Sales Management

* Register a sale associated with a customer and a seller.
* Add one or more products to a sale.
* Calculate the total value of a sale automatically.
* Validate that the customer and seller exist.
* Validate that the products exist.
* Validate that the requested quantity is positive.
* Validate that there is enough stock before completing a sale.
* Automatically reduce product stock after a successful sale.
* Automatically apply the best available promotion to a sale, if any is active.
* Generate a receipt showing the subtotal, applied discount, and final total.
* List all registered sales.
* Consult a customer's purchase history.
* Consult the sales handled by a specific seller.

### Promotion Management

* Register percentage-based promotions.
* Register category-based promotions (videogames, consoles, or accessories).
* Register bulk-purchase promotions.
* List all registered promotions.
* List promotions currently active.
* Automatically select the promotion that grants the largest discount for a given sale.

### Return Management

* Register a return for one or more products from a previous sale (partial returns allowed).
* Validate that the return is requested within 30 calendar days of the original sale.
* Validate that the returned products actually belong to the referenced sale.
* Automatically calculate the refund amount.
* Automatically restore the stock of returned products.
* List all registered returns.
* Consult returns associated with a specific customer.
* Consult returns associated with a specific sale.
* Generate a monthly balance report (sales minus returns) for a given month and year.

### Warranty Management

* Automatically generate a basic warranty (6 months) for every console included in a sale.
* Optionally assign an extended warranty (12 months) to a console at the time of sale, adding 10% of its price to the sale's total.
* Consult the warranty associated with a specific product within a specific sale.
* List all registered warranties.
* List warranties currently active.
* List warranties expiring within a given number of days.

### Error Handling and Validation

* Report errors with a typed exception hierarchy instead of generic exceptions.
* Show a different message to the user for each category of error: resource not found, business rule violated, invalid data, and storage problem.
* Centralize the checks of rules and data in dedicated validators, one per functional area.
* Never show technical messages or stack traces to the user.

---

## Technologies Used

* **Java**
* **Maven**
* **Object-Oriented Programming (OOP)**
* **Git and GitHub**
* **CSV and TXT files for persistence**
* **Graphical user interface with Java Swing (JOptionPane, JTextArea, JScrollPane)**

The project uses Java release **21** as configured in the Maven `pom.xml`.

---

## Project Architecture

The application follows a four-layer architecture:

```text
┌─────────────────────────┐
│           UI            │
│     MainMenu.java       │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│        SERVICE          │
│ PersonService           │
│ ProductService          │
│ AccessoryService        │
│ PromotionService        │
│ ReturnService           │
│ WarrantyService         │
└────────────┬────────────┘
             │
       ┌─────┴─────┐
       ▼           ▼
┌────────────┐ ┌──────────────┐
│   MODEL    │ │ PERSISTENCE  │
│            │ │              │
│ Product    │ │ ProductDAO   │
│ VideoGame  │ │ PersonDAO    │
│ Console    │ │ SaleDAO      │
│ Accessory  │ │ AccessoryDAO │
│ Controller │ │ PromotionDAO │
│ Cable      │ │              │
│ Memory     │ │              │
│ Promotion  │ │ ReturnRepository│
│ Return     │ │              │
│ Warranty   │ │ WarrantyRepository│
│ BasicWarranty│ │            │
│ ExtendedWarranty│ │         │
│ Person     │ │              │
│ Customer   │ └──────┬───────┘
│ Seller     │        │
│ Sale       │◄───────┘
└────────────┘
```

### Model

The `model` layer contains the main domain entities of the system.

* `Product` — abstract base class for store products.
* `VideoGame` — represents a video game.
* `Console` — represents a console.
* `Person` — abstract base class for people interacting with the store.
* `Customer` — represents a customer.
* `Seller` — represents a seller.
* `Sale` — represents a store transaction.
* `Accessory` — abstract base class for video game accessories, extending `Product`.
* `Controller` — represents a game controller.
* `Cable` — represents a cable accessory.
* `Memory` — represents a storage memory accessory.
* `Promotion` — abstract base class for promotional discount campaigns.
* `PercentageDiscount` — applies a flat percentage discount to a sale's total.
* `CategoryDiscount` — applies a percentage discount only to products of a specific category.
* `BulkPurchaseDiscount` — applies a percentage discount when a sale reaches a minimum product quantity.
* `Return` — represents a return transaction, referencing an original sale and the specific products returned from it.
* `Warranty` — abstract base class for warranties associated with a purchased product and sale.
* `BasicWarranty` — 6-month warranty automatically assigned to consoles at no additional cost.
* `ExtendedWarranty` — 12-month optional warranty, adding 10% of the product's price to the sale.

`Product` uses polymorphism through the `getFullDescription()` abstract method, which is implemented by its subclasses.

`Person` is also abstract because the system works with specific roles such as customers and sellers.

### Persistence

The `persistence` layer is responsible for saving and loading information from files.

* `ProductDAO` — manages product information.
* `PersonDAO` — manages customers and sellers.
* `SaleDAO` — manages sales.
* `AccessoryDAO` — manages controllers, cables, and memories.
* `PromotionDAO` — manages promotional discount campaigns.
* `ReturnRepository` — manages returns, resolving references to the original sale and returned products.
* `WarrantyRepository` — manages warranties, resolving references to the associated product and sale.

`ProductDAO`, `PersonDAO`, and `SaleDAO` catch any `IOException` internally and rethrow it as a `PersistenceException` that includes the file name and the operation (reading or writing).

The system uses files inside the `data` directory to maintain information between executions.

### Service

The `service` layer contains the application's business rules.

* `ProductService` — handles product registration, listing, and stock validation.
* `PersonService` — handles customer and seller information.
* `SaleService` — handles sales, validations, total processing, and inventory updates for both products and accessories.
* `AccessoryService` — handles accessory registration, console compatibility, listing, and stock updates.
* `PromotionService` — handles promotion registration, validity checks, and selecting the best applicable promotion for a sale.
* `ReturnService` — handles return registration with 30-day and ownership validation, stock restoration, and the monthly balance report.
* `WarrantyService` — handles warranty assignment, validity checks, and queries for active or soon-to-expire warranties.

The service layer prevents invalid operations before information is persisted.

### Validation

The `validation` layer sits between `service` and `model`. Services call its validators before running any business operation, and the validators throw the custom exceptions when a rule is not met. Validators contain no file access and never call services or repositories.

* `ProductValidator` — validates product data, stock availability, and product existence.
* `PersonValidator` — validates customer and seller data, person existence, and identifier uniqueness.
* `SaleValidator` — validates that a sale has a customer, a seller, and at least one product, and that a sale exists.

### Exceptions

The `exceptions` package is cross-cutting and can be used from any layer. All exceptions extend `GameZoneException`, which is unchecked and carries an error code.

* `ResourceNotFoundException` — a requested product, customer, seller, or sale does not exist.
* `BusinessRuleException` — a business rule is violated, such as insufficient stock or a duplicated customer.
* `InvalidDataException` — an input value fails format or range checks.
* `PersistenceException` — a file could not be read or written; it keeps the original cause.

### UI

The `ui` layer contains the graphical interface, built entirely with Java Swing dialogs instead of a console loop.

* `MainMenu` — builds every menu as a `JOptionPane` dropdown dialog (`showInputDialog` with an options array), every data entry field as a `JOptionPane.showInputDialog` text prompt, every confirmation or error message as a `JOptionPane.showMessageDialog`, and every listing (products, customers, sellers, sales, accessories, promotions, returns, warranties) as a numbered, scrollable `JTextArea` wrapped in a `JScrollPane`. It still handles each type of custom exception with its own message in Spanish, now shown as an error dialog instead of a console line.

The UI communicates with the service layer and does not access the persistence layer directly. Converting the interaction model from console to Swing dialogs did not change any business rule or validation: every operation behaves exactly as before, only the presentation layer changed.

---

## Project Structure

```text
GamezoneUnicesar/
│
├── data/
│   ├── sellers.csv
│   ├── accessories.csv
│   ├── promotions.csv
│   ├── returns.csv
│   └── warranties.csv
│
├── docs/
│   ├── ai-usage/
│   │   ├── leader-ai-log.md
│   │   ├── developer1-ai-log.md
│   │   └── developer2-ai-log.md
│   ├── analysis.md
│   ├── class-diagram.md
│   ├── hierarchy-diagram.md
│   ├── layers-diagram.md
│   ├── accessory-analysis.md
│   ├── accessory-class-diagram.md
│   ├── promotion-analysis.md
│   ├── promotion-class-diagram.md
│   ├── return-analysis.md
│   ├── return-class-diagram.md
│   ├── warranty-analysis.md
│   ├── warranty-class-diagram.md
│   ├── exception-analysis.md
│   └── exception-class-diagram.md
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── gamezone/
│                   ├── exceptions/
│                   ├── model/
│                   ├── persistence/
│                   ├── service/
│                   ├── ui/
│                   ├── validation/
│                   └── Main.java
│
├── pom.xml
├── TEAM.md
└── README.md
```

The `data` directory also contains files generated by the application during execution, such as customer, product, and sales data.

---

## Data Persistence

The system stores information in local files so that data can remain available between executions.

The main files are:

* `data/products.csv` — stores products.
* `data/customers.txt` — stores registered customers.
* `data/sellers.csv` — stores sellers.
* `data/sales.txt` — stores registered sales.
* `data/accessories.csv` — stores controllers, cables, and memories.
* `data/promotions.csv` — stores promotional discount campaigns.
* `data/returns.csv` — stores registered returns.
* `data/warranties.csv` — stores registered warranties.

If some files do not exist when the application starts, the system creates the necessary data structures and generates the files when information is saved.

---

## Business Rules

The system applies several business validations:

1. A sale must contain at least one product.
2. The customer associated with a sale must exist.
3. The seller associated with a sale must exist.
4. Every product included in a sale must exist.
5. The quantity of a product must be greater than zero.
6. The available stock must be enough to complete the sale.
7. Product stock is automatically reduced after a successful sale.
8. A product cannot have a negative price.
9. Customers cannot be registered using an ID that already exists.
10. Products require a valid ID.

These rules are mainly handled in the service layer.

---

## Object-Oriented Design

The project applies several object-oriented programming concepts.

### Inheritance

The system uses inheritance in both main hierarchies:

```text
Product
├── VideoGame
└── Console
```

```text
Person
├── Customer
└── Seller
```

### Abstraction

`Product` and `Person` are abstract classes because they represent common characteristics and behavior without being directly instantiated as generic objects.

### Polymorphism

Each product type provides its own implementation of:

```java
getFullDescription()
```

This allows the system to obtain the specific description of a product according to its type.

### Encapsulation

The domain classes use private attributes and provide methods to access their information.

### Associations

A `Sale` is associated with:

* One `Customer`.
* One `Seller`.
* One or more `Product` objects.

---

## Requirements

To run the project, the following software is required:

* **JDK 21**
* **Apache Maven**
* **Git** (optional, if cloning the repository)

The Java version should match the version configured in `pom.xml`.

---

## How to Run the Project

### 1. Clone the repository

```bash
git clone <repository-url>
```

### 2. Enter the project directory

```bash
cd GamezoneUnicesar
```

### 3. Compile the project

```bash
mvn clean compile
```

### 4. Run the application

After compiling, run the main class:

```bash
java -cp target/classes com.gamezone.Main
```

The application opens a Swing dialog with a dropdown listing the main menu options:

* Gestionar productos
* Gestionar clientes y vendedores
* Gestionar ventas
* Gestionar accesorios
* Gestionar promociones
* Gestionar devoluciones
* Gestionar garantías
* Salir

Every submenu works the same way: a dropdown dialog for navigation, input dialogs for each field, and a scrollable text window for listings and receipts.

---

## Application Flow

The main menu provides access to three modules:

```text
GameZone Unicesar
│
├── Product Management
│   ├── Register Video Game
│   ├── Register Console
│   └── List Available Products
│
├── Customer and Seller Management
│   ├── Register Customer
│   ├── List Customers
│   └── List Sellers
│
├── Sales Management
│   ├── Register Sale (products and/or accessories)
│   ├── List Sales
│   ├── Customer Purchase History
│   └── Seller Sales History
│
├── Accessory Management
│   ├── Register Controller
│   ├── Register Cable
│   ├── Register Memory
│   ├── List All Accessories
│   ├── List Accessories by Type
│   └── Query Accessories Compatible With a Console
│
├── Promotion Management
│   ├── Register Percentage Discount
│   ├── Register Category Discount
│   ├── Register Bulk Purchase Discount
│   ├── List All Promotions
│   └── List Active Promotions
│
├── Return Management
│   ├── Register Return
│   ├── View All Returns
│   ├── View Returns by Customer
│   ├── View Returns by Sale
│   └── Consult Monthly Balance
│
└── Warranty Management
    ├── Consult Warranty by Product and Sale
    ├── List All Warranties
    ├── List Active Warranties
    └── List Warranties Expiring Soon
```

---

## Team

| Name                             | Student ID | Role             |
| -------------------------------- | ---------- | ---------------- |
| Deiner Andrés De Luque Navarro   | 1067604165 | Technical Leader |
| Ronald Yessid Mendoza Fernandez  | 1122399234 | Developer 1      |
| Santiago Manuel Meza Meza Guerra | 1066873262 | Developer 2      |

### Responsibilities

**Technical Leader — Deiner Andrés De Luque Navarro**
Responsible for the sales module, repository configuration, team coordination, Pull Request integration, system integration, UI menu, and application entry point.

**Developer 1 — Ronald Yessid Mendoza Fernandez**
Responsible for the product module, including the product hierarchy, persistence, and service layer.

**Developer 2 — Santiago Manuel Meza Meza Guerra**
Responsible for the person module, including customers, sellers, persistence, and service layer.

More information about the team's organization and class distribution can be found in [`TEAM.md`](TEAM.md).

---

## Git Workflow

The project was managed using a Git Flow-based workflow:

```text
main
  │
  └── develop
        │
        ├── feature/product-module
        ├── feature/person-module
        └── feature/sale-module
```

The `main` branch represents the stable version of the project, while `develop` was used for integration.

Feature branches were used to develop individual modules, which were later integrated through Pull Requests.

---

## Documentation

Additional project documentation is available in the `docs` directory:

* [`analysis.md`](docs/analysis.md) — analysis questions and answers.
* [`class-diagram.md`](docs/class-diagram.md) — class diagram.
* [`hierarchy-diagram.md`](docs/hierarchy-diagram.md) — class hierarchy.
* [`layers-diagram.md`](docs/layers-diagram.md) — layered architecture diagram.
* [`integration-analysis.md`](docs/integration-analysis.md) — description, cause, and applied solution for each integration adjustment (A1-A7).
* [`integrated-class-diagram.md`](docs/integrated-class-diagram.md) — single class diagram covering all four layers and all four integrated modules.
* [`accessory-analysis.md`](docs/accessory-analysis.md) — analysis questions and answers for the accessory module.
* [`accessory-class-diagram.md`](docs/accessory-class-diagram.md) — class diagram for the accessory module.
* [`promotion-analysis.md`](docs/promotion-analysis.md) — analysis questions and answers for the promotion module.
* [`promotion-class-diagram.md`](docs/promotion-class-diagram.md) — class diagram for the promotion module.
* [`return-analysis.md`](docs/return-analysis.md) — analysis questions and answers for the return module.
* [`return-class-diagram.md`](docs/return-class-diagram.md) — class diagram for the return module.
* [`warranty-analysis.md`](docs/warranty-analysis.md) — analysis questions and answers for the warranty module.
* [`warranty-class-diagram.md`](docs/warranty-class-diagram.md) — class diagram for the warranty module.
* [`ai-usage/`](docs/ai-usage/) — individual AI usage logs for each team member.

---

## AI Usage

Artificial intelligence was used as a support tool during the development process for:

* Understanding object-oriented programming concepts.
* Clarifying layered architecture and dependencies.
* Understanding Git and GitHub commands.
* Understanding Maven-related errors.
* Reviewing specific code written by the team.
* Clarifying Java functionality and naming conventions.

AI was not used to replace the team's analysis, design, understanding, or decision-making. The individual AI usage logs are available in `docs/ai-usage/`.

---

## Project Status

**Final version completed.**

The system includes the three main modules:

* Product management.
* Customer and seller management.
* Sales management.

It also includes persistence, business validations, layered architecture, documentation, Git workflow, and a Swing-based graphical user interface.

---

## Authors

**El Tridente de Java Team**

Universidad Popular del Cesar
Systems Engineering

