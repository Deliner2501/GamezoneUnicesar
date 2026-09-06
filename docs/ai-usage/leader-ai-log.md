# PROMPTS AND RESPONSES
## 1. How should I organize the GitHub repository and branches for a team project using Git Flow?
**Answer:** The repository can use main as the stable branch, develop as the integration branch, and feature/* branches for individual tasks. Feature branches should be created from develop and merged back through Pull Requests.
## 2. How do I create a Pull Request from a feature branch to the develop branch?
**Answer:** First, the feature branch should be pushed to GitHub. Then, a Pull Request can be created with develop as the base branch. The Pull Request should explain the changes and be reviewed by another team member before merging.
## 3. What should I do if two team members make changes in the same file and a Git conflict appears?
**Answer:** Git marks the parts of the file that have conflicts. The changes should be reviewed manually, keeping the correct version or combining both changes when necessary. After resolving the conflict, the file can be added and committed.
## 4. How can I make a good and atomic commit for a change in my project?
**Answer:** An atomic commit should contain one clear and related change. The commit message should briefly describe what was changed, instead of including several unrelated modifications.
## 5. What is the correct way to use Conventional Commits in a Java project?
**Answer:** Conventional Commits use prefixes to describe the type of change. For example, feat can be used for a new feature, fix 
## 6. How should I organize the packages of my Maven project according to a layered architecture?
**Answer:** The project can have a main package such as com.gamezone with the subpackages model, persistence, service, and ui. Main.java can remain in the root package.
## 7. What is the correct dependency direction between the model, persistence, service, and UI layers?
**Answer:** The UI layer depends on the service layer. The service layer depends on the model and persistence layers. The persistence layer depends on the model, while the model should remain independent.
## 8. I have an error in my Maven project. How can I understand what the error message means?
**Answer:** The first step is to read the error message and identify the file, class, or dependency related to the problem. The error should be analyzed before making changes to the project.
## 9. Can you review this part of my Java code and tell me if there are things I could improve?
**Answer:** The code can be reviewed for things such as naming, organization, visibility of attributes, duplicated code, and whether it follows the project's requirements. The final changes should be made by the programmer after understanding the suggestions.
## 10. What validations should a sale service normally perform before registering a sale?
**Answer:**The service should verify that the information required for the sale is valid before registering it. For example, it can check that the customer and products exist and that the necessary information is available.
## 11. What things should I check when reviewing a teammate's Pull Request?
**Answer:** The changes should be checked to make sure they correspond to the purpose of the feature, follow the project's structure and rules, and do not introduce unnecessary changes. The code should also be understandable before approving the Pull Request.
## 12. What are some good English naming conventions for Java classes, methods, and variables?
**Answer:** Java classes normally use PascalCase, while methods and variables use camelCase. Names should be clear and describe their purpose. For example, Customer, createSale(), and totalAmount.

