# Team Organization — GameZone Unicesar

## Team Members

| Name | Student ID | Role |
|---|---|---|
| Deiner Andrés De Luque Navarro | 1067604165 | Technical Leader |
| Ronald Yessid Mendoza Fernandez | 1122399234 | Developer 1 |
| Santiago Manuel Meza Meza Guerra | 1066873262 | Developer 2 |

## Roles and Modules

### Technical Leader — Deiner Andrés De Luque Navarro
**Module:** Sales module and system integration

Responsible for coordinating the team, integrating the work of all members, and implementing the sales module, which spans all four layers and depends on the modules developed by the other two members.

### Developer 1 — Ronald Yessid Mendoza Fernandez
**Module:** Product module

Responsible for implementing the complete product hierarchy and its persistence and service classes.

### Developer 2 — Santiago Manuel Meza Meza Guerra
**Module:** Person module

Responsible for implementing the complete person hierarchy (customers and sellers) and its persistence and service classes.

## Class Distribution

### Developer 1 (5 classes)
- `Product` (abstract base class)
- `VideoGame` (derived class)
- `Console` (derived class)
- `ProductDAO` (persistence layer)
- `ProductService` (service layer)

### Developer 2 (5 classes)
- `Person` (abstract base class)
- `Customer` (derived class)
- `Seller` (derived class)
- `PersonDAO` (persistence layer)
- `PersonService` (service layer)

### Technical Leader (5 classes)
- `Sale` (domain class)
- `SaleDAO` (persistence layer)
- `SaleService` (service layer)
- `MainMenu` (UI layer)
- `Main` (application entry point)

## Feature Branches

| Member | Branch |
|---|---|
| Developer 1 | `feature/product-module` |
| Developer 2 | `feature/person-module` |
| Technical Leader | `feature/sale-module` |

## Committed Activities

### Technical Leader — Deiner Andrés De Luque Navarro
1. Create the GitHub repository with initial configuration (README, .gitignore, license).
2. Configure project branches (`main` and `develop`) and enable branch protection.
3. Configure the Maven project with the initial `pom.xml` and the four-layer package structure.
4. Write the `TEAM.md` file with team information, assigned roles, and class distribution.
5. Implement the `Sale` domain class with its attributes, constructor, and basic methods.
6. Implement the sale total calculation method.
7. Implement the `SaleDAO` persistence class.
8. Implement the `SaleService` class with validation rules (minimum one product, stock check, inventory update).
9. Implement the basic structure of the UI class (main menu).
10. Implement the submenus for each of the three modules.
11. Implement the `Main` class with initial data loading and dependency wiring.
12. Review and merge Pull Requests from developers into the integration branch.
13. Write the final `README.md` with build and run instructions.

### Developer 1 — Ronald Yessid Mendoza Fernandez
1. Create the feature branch for the product module.
2. Implement the abstract base class of the product hierarchy with common attributes, constructor, and methods.
3. Declare the abstract description method to be implemented by derived classes.
4. Implement the first derived class (`VideoGame`) with its specific attributes and description method.
5. Implement the second derived class (`Console`) with its specific attributes and description method.
6. Implement the `ProductDAO` persistence class with save and load methods from files.
7. Implement the `ProductService` class with registration, listing, and stock update methods.
8. Document all classes of the module with JavaDoc in English.
9. Request Pull Requests to the Technical Leader for module integration.

### Developer 2 — Santiago Manuel Meza Meza Guerra
1. Create the feature branch for the person module.
2. Implement the abstract base class of the person hierarchy with common attributes, constructor, and methods.
3. Declare the abstract or business method to be implemented by derived classes.
4. Implement the first derived class (`Customer`) with its specific attributes.
5. Implement the second derived class (`Seller`) with its specific attributes.
6. Implement the `PersonDAO` persistence class with save and load methods from files.
7. Implement the `PersonService` class with registration and listing methods.
8. Document all classes of the module with JavaDoc in English.
9. Request Pull Requests to the Technical Leader for module integration.