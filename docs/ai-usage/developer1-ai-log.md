# AI Usage Log — Developer 1 (Product Module)

**Student:** Ronald

---

1. **"What exactly do I get as Developer 1 of the product module, according to the workshop document?" **

The AI explained to me that my responsibility is 5 classes through the 3 layers (model, persistence, service): the abstract class of products, their two derivatives, persistence and service - and that the rest (people, sales, the interface and the Main) is the responsibility of my colleagues.

2. **"How do I clone the repository and create my own branch without touching main or develop directly?" **

I was guided step by step in PowerShell: git clone, git checkout develop, git pull origin develop, and then git checkout -b feature/product-module to create my work branch following the Git Flow model required by the workshop.

3. **"I made git commit but the code on GitHub appears empty, with only the template that generates NetBeans. What happened?" **

We identified that he had done git add/commit before actually saving the file in NetBeans (Ctrl+S). I learned to always check with cat archivo.java in PowerShell before commiting, to confirm that the content on the disk matched what I saw in the editor.

4. **"When I push my new branch, it didn't appear on GitHub. Why?" **

The branch existed only locally because the first push had never been made. I used git push -u origin feature/product-module to create it on the remote and link it with my local branch.

5. **"The class diagram sent by the leader does not have setters in Product, but my code does. Should I leave them?" **

We compared the official diagram against my code and confirmed that the diagram only defined getters (plus reduceStock() to modify the stock), so I removed the four setters to keep the class faithful to the design agreed by the team.

6. **"When compiling the complete project I get error: release version 26 not supported. Is it my mistake?" **

The AI explained to me that this error comes from the maven.compiler.release configuration in the pom.xml, a general configuration file of the project that it is not up to me to modify - I warned the leader to adjust it.

7. **"A colleague suggested that the leader work on a separate project and copy the code later to avoid Git conflicts. Is that necessary?" **

I asked the AI if that made technical sense. He explained to me that Git merges files, not folders, so two modules with folders of the same name (persistence, for example) do not generate conflict if they have different files - the team decided to continue working directly on the real repo.

9. **"What do the prefixes feat:, fix:, refactor: mean in the commits, and when is each one used?" **

He explained to me the Conventional Commits convention that the workshop requires: feat: for new functionality, fix: to correct errors, refactor: to reorganize without changing behavior, and docs:/chore: for documentation and maintenance.