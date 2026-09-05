\# AI Usage Log — Developer 1 (Product Module)

\*\*Student:\*\* Ronald



\---



1\. \*\*"¿Qué me toca exactamente como Desarrollador 1 del módulo de productos, según el documento del taller?"\*\*

&#x20;  La IA me explicó que mi responsabilidad son 5 clases atravesando las 3 capas (model, persistence, service): la clase abstracta de productos, sus dos derivadas, la persistencia y el servicio — y que el resto (personas, ventas, la interfaz y el Main) es responsabilidad de mis compañeros.



2\. \*\*"¿Cómo clono el repositorio y creo mi propia rama sin tocar main o develop directamente?"\*\*

&#x20;  Me guio paso a paso en PowerShell: `git clone`, `git checkout develop`, `git pull origin develop`, y luego `git checkout -b feature/product-module` para crear mi rama de trabajo siguiendo el modelo Git Flow que exige el taller.



3\. \*\*"Hice `git commit` pero el código en GitHub aparece vacío, con solo la plantilla que genera NetBeans. ¿Qué pasó?"\*\*

&#x20;  Identificamos que había hecho `git add`/`commit` antes de guardar realmente el archivo en NetBeans (`Ctrl+S`). Aprendí a verificar siempre con `cat archivo.java` en PowerShell antes de comitear, para confirmar que el contenido en disco coincidiera con lo que veía en el editor.



4\. \*\*"Al hacer `git push` a mi rama nueva no aparecía en GitHub. ¿Por qué?"\*\*

&#x20;  La rama existía solo localmente porque nunca se había hecho el primer `push`. Usé `git push -u origin feature/product-module` para crearla en el remoto y vincularla con mi rama local.



5\. \*\*"El diagrama de clases que mandó el líder no tiene setters en `Product`, pero mi código sí. ¿Debo dejarlos?"\*\*

&#x20;  Comparamos el diagrama oficial contra mi código y confirmamos que el diagrama solo definía getters (más `reduceStock()` para modificar el stock), así que quité los cuatro setters para mantener la clase fiel al diseño acordado por el equipo.



6\. \*\*"Al compilar el proyecto completo me sale `error: release version 26 not supported`. ¿Es un error mío?"\*\*

&#x20;  La IA me explicó que ese error viene de la configuración `maven.compiler.release` en el `pom.xml`, un archivo de configuración general del proyecto que no me corresponde modificar a mí — le avisé al líder para que lo ajustara.



7\. \*\*"Un compañero le sugirió al líder trabajar en un proyecto aparte y copiar el código después para evitar conflictos de Git. ¿Eso es necesario?"\*\*

&#x20;  Le pregunté a la IA si eso tenía sentido técnicamente. Me explicó que Git fusiona archivos, no carpetas, así que dos módulos con carpetas del mismo nombre (`persistence`, por ejemplo) no generan conflicto si tienen archivos distintos — el equipo decidió seguir trabajando directo en el repo real.





9\. \*\*"¿Qué significan los prefijos `feat:`, `fix:`, `refactor:` en los commits, y cuándo se usa cada uno?"\*\*

&#x20;  Me explicó la convención Conventional Commits que exige el taller: `feat:` para funcionalidad nueva, `fix:` para corregir errores, `refactor:` para reorganizar sin cambiar comportamiento, y `docs:`/`chore:` para documentación y mantenimiento.





