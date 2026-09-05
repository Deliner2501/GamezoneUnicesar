# Regarding the people in the system:
## 1. What attributes are common to all people who interact with the store, and which ones are specific to each particular type of person? How is this distinction reflected in a class hierarchy?
**Answer:** All persons share the attributes name, id, and phone. Customers also have email and purchaseHistory, while sellers have employeeCode and workShift. This difference is represented by a hierarchy in which Person is the base class and Customer and Seller are the subclasses.
## 2. Should there be a class representing a "generic person" without specifying their role? Why or why not? What implication does this decision have regarding the possibility of instantiating that class? 
**Answer:** Yes, a Person class should exist because it allows for grouping the common attributes and behaviors of customers and salespeople. However, it must be 
abstract because the system does not need to register a generic person, 
but rather people with a specific role. Being abstract, it cannot be instantiated 
directly and serves only as a base for its subclasses.
# Regarding the system's products
## 3. What characteristics do all products sold by the store have in common, regardless of their type? What characteristics are specific to each product type?
**Answer:** All products share ID, title, price, and stock. Video games add characteristics such as platform, genre, and age rating, while consoles have brand, model, and generation. Therefore, Product should be the base class, with VideoGame and Console as its subclasses.
## 4. Each product type must be able to provide a description that incorporates its specific characteristics. How should this behavior be declared in the base class to ensure that all subclasses implement it in their own way? 
**Answer:** The Product class must declare an abstract method to retrieve the 
description, because each product type needs to construct it using its 
own characteristics. Subclasses implement this method differently 
using polymorphism and the @Override annotation. This ensures that 
each product provides its own description.
# On sales and relationships between entities
## 5. A sale involves a customer, a salesperson, and one or more products. What types of relationships exist between the class representing the sale and the other classes in the system? Are these relationships inheritance, association, composition, or another type? Justify your answer.
**Answer:** Sale relates to Customer, Seller, and Product through associations, because a Sale uses objects from those classes but does not inherit from them. A Sale belongs to a Customer, is handled by a Seller, and involves one or more Products. The relationship between Sale and Product can be represented as aggregation because the products exist independently of the sale. If a sale is deleted, the products should still exist in the system, for example, in the inventory. Therefore, this relationship is better represented as aggregation rather than composition.
## 6. Should the sale be responsible for calculating its own total, or should this responsibility fall to another class? Justify your decision. 
**Answer:** The responsibility for calculating the total should belong to Sale, because 
the total is an inherent characteristic of the transaction. The sale can iterate 
through the purchased products and sum their prices, thereby keeping the 
transaction data linked to the corresponding behavior.
# Regarding business constraints
## 7. How does the design ensure that a sale cannot be recorded without at least one product? At what point in the system should this rule be validated? 
**Answer:** The design must prevent a sale from being recorded without products. This 
rule should be validated primarily at the service layer, before saving 
the sale. If the product collection is empty, the service must reject the 
operation. The context expressly states that a sale must contain 
at least one product.
## 8. How is the automatic inventory update reflected in the design when a sale is recorded? Which classes are involved in this operation?
**Answer:** When recording a sale, the system must verify that sufficient stock exists and subsequently deduct the quantity sold. The classes primarily involved in this operation are Sale, Product, SaleService, and the product persistence layer. The business logic should reside in SaleService, while data modification and storage are handled by the corresponding persistence classes.
# On layered organization
## 9. The system must be organized into four layers: model, persistence, services, and user interface. What types of classes belong to each layer? What criteria determine which layer a class should be placed in?
**Answer:** The model layer contains business entities; the persistence layer (DAO) contains classes that save and retrieve information from files; the service layer contains business rules; and the view layer contains the console menu and user interaction. A class's placement depends on the responsibility it performs, avoiding the mixing of responsibilities across layers.
## 10. Why should the logic for saving and retrieving data from files not be located within domain classes? What problems arise when these responsibilities are mixed? 
**Answer:** Model classes should not save or read files because doing so 
would mix business logic with persistence. This would result in classes 
that are more difficult to maintain, test, and modify. For example, if the 
storage format changes, it should not be necessary to modify the classes 
representing customers, products, or sales.
## 11. What dependencies are permitted between the layers, and which are prohibited? Justify the rationale behind the permitted dependencies. 
**Answer:** The permitted dependencies between layers are View -> Service, where the user interface uses services to execute operations; Service -> Model and DAO, where services use model entities and access the persistence layer to save or retrieve information; and DAO -> Model, since the persistence layer works with domain entities. The Model layer does not depend on any other layer, as it must remain independent. Dependencies that violate this structure are prohibited. For example, the View accessing the DAO directly, or the Model depending on the View, Service, or DAO. This arrangement allows for the separation of responsibilities, reduces coupling, and facilitates system maintenance.