# LOC Clicker

Juego de escritorio en Java Swing sobre productividad PSP, LOC y gestión de defectos.

## Requisitos

- JDK 11 o superior
- Linux/macOS: `./compilar.sh`
- Windows: `compilar.bat`

Los scripts compilan el juego, crean el JAR y lo ejecutan. Las pruebas de balance pueden ejecutarse después de compilar:

```sh
javac -encoding UTF-8 -cp out -d out tests/BalanceTest.java
java -cp out BalanceTest
```

La prueba de interfaz `UiSmokeTest` requiere una pantalla disponible o Xvfb.

## Estructura de carpetas

```text
ConsolesLocClicker/   Reglas, interfaz, eventos, minijuegos y guardado
ConsolesLocClicker/core/
					  Reglas puras y balance
ConsolesLocClicker/ui/
					  Ventanas y paneles Swing
ConsolesLocClicker/events/
					  Managers de eventos
ConsolesLocClicker/events/catalog/
					  Catalogos y clases seleccionables de eventos
ConsolesLocClicker/minigames/
					  Minijuegos y QTE
ConsolesLocClicker/persistence/
					  Guardado y bitacora PSP
tests/                Balance sin interfaz y smoke test con Swing Robot
assets/               Iconos, imagenes y futuros sonidos del juego
partidas/             Guardados locales, se crea al guardar una campaña
compilar.sh           Compilacion y ejecucion en Linux/macOS
compilar.bat          Compilacion y ejecucion en Windows
```

Los archivos `out/`, `LOCClicker.jar` y `manifest.txt` son generados y no deben editarse.
El codigo fuente vive en `ConsolesLocClicker/`; cada archivo debe permanecer en el paquete
que corresponde a su subcarpeta.

## Arquitectura del juego

- `Stats.java` contiene las reglas y el estado, sin depender de Swing.
- `GameConfig.java` concentra nombres, precios, tiempos y balance.
- `Frame.java` conecta el reloj con los paneles y controla el ciclo de partida.
- `StatsPanel.java` muestra las metricas PSP, el nivel de empresa y el avance contra plan.
- `DefectManager.java` concentra los eventos de defectos y su resolucion.
- `ContextDialog.java` contiene la introduccion narrativa que aparece al comenzar.
- `StabilityMinigame.java` contiene el reto rapido para recuperar estabilidad.
- `CodeFixMinigame.java` obliga a localizar la linea concreta del defecto.
- `EventManager.java` agenda hackatones y crisis de la fase de crecimiento.
- `UnexpectedEventManager.java` agenda incidentes inevitables que restan LOC y estabilidad.
- `FrenzyQte.java` muestra el círculo amarillo que activa el multiplicador temporal.
- `SaveManager.java` y `SavePanel.java` guardan campañas en `partidas/` por nombre de empresa.
- `StaffPanel.java` es la tienda de empleados, retención y equipo de trabajo.

## Como se conectan las piezas

`Frame` crea `Stats`, los paneles y los managers. Cada segundo llama a `Stats.tick()` y
refresca la interfaz. `Stats` no conoce Swing: calcula LOC, precios, fases, estabilidad,
deuda y guardados. Esa separacion permite probar el balance sin abrir ventanas.

`DefectManager` agenda defectos y registra la bitacora. Cada defecto abre una ventana grande
y selecciona un solo minijuego al azar desde `RepairEventCatalog`: `CodeFixMinigame` relaciona
el numero de linea reportado con la opcion correcta o `StabilityMinigame` propone un reto
rapido de estabilidad. Ambos reciben callbacks para resolver el defecto sin conocer el resto
de la aplicacion.

`EventManager` se activa desde la fase 2 y puede mostrar hackatones o crisis. Las crisis
consumen LOC, despues abren el minijuego de estabilidad. La estabilidad cambia el rendimiento:
por debajo de 100% reduce LOC/s; por encima de 100% lo aumenta y puede iniciar un frenesí de
produccion de varios segundos. El frenesí se anuncia como multiplicador x2 y tiene un
enfriamiento para que no domine la partida.

`UnexpectedEventManager` funciona en paralelo a los minijuegos: cada 60-90 segundos lanza un
incidente operativo inevitable en fase 2 o superior. El incidente resta un porcentaje de LOC,
baja la estabilidad a un rango intermedio y aumenta la deuda técnica. El jugador solo puede
registrarlo; los minijuegos sirven para recuperarse después, no para cancelar la pérdida.

## Carpeta de eventos

`ConsolesLocClicker/events/catalog/` es el punto de extensión para contenido seleccionable:

- `RepairEvent` define un minijuego capaz de resolver un defecto.
- `RepairEventCatalog` elige uno al azar para cada defecto. Actualmente contiene
	`CodeLineRepairEvent` y `StabilityRepairEvent`.
- `NegativeEvent` define una consecuencia inevitable sin solución.
- `NegativeEventCatalog` elige entre caída del mercado, subida de precios, recorte
	presupuestario y rotación de personal. Estos eventos son inevitables y se muestran con
	cartel rojo; no se mezclan con la selección de minijuegos.

Para agregar un minijuego nuevo, crea una clase que implemente `RepairEvent` y añádela a
`RepairEventCatalog`. Para agregar un evento negativo, implementa `NegativeEvent`, aplica su
efecto mediante métodos de `Stats` y añádelo a `NegativeEventCatalog`. No es necesario cambiar
`Frame` ni el reloj central.

El frenesí no se activa automáticamente: `Stats` genera una oportunidad y `FrenzyQte` dibuja
tres círculos amarillos consecutivos y un círculo dorado final, cada uno en una posición
distinta. Un clic fuera o un timeout pierde la oportunidad. Al completar la cadena, la
estabilidad vuelve a 100% y la producción se multiplica por x2 durante ocho segundos.

## Puntos de extension

La fase 1 termina al comprar CMMI Nivel 5. Ese hito abre la fase 2, que exige producir una
meta creciente y conservar la estabilidad por encima del minimo. `Stats.advancePhase()` deja
la estructura preparada para seguir escalando sin un final fijo; `GameConfig.java` controla
el crecimiento de las metas. Los nuevos productores y mejoras se agregan a `GameConfig.java`
y se muestran automaticamente en `StorePanel.java`.

La estabilidad representa la capacidad de la empresa para seguir entregando y la deuda tecnica
crece cuando los defectos escapan. Los minijuegos pueden implementarse como dialogos Swing
independientes, siguiendo el patron de `StabilityMinigame`: reciben `Stats`, aplican una
recompensa o penalizacion y llaman a una funcion de refresco al terminar. Los mini eventos
siguen el patron de `EventManager`, de modo que cada equipo pueda añadir eventos sin modificar
el reloj central.

En la fase 2 las compras aumentan de precio y las crisis exigen pagar una mitigacion antes de
abrir su minijuego. Los defectos no tienen parche directo: siempre se resuelven seleccionando
la linea correcta en `CodeFixMinigame`. La pestaña `Partida` guarda el estado actual como un
archivo `.properties` dentro de `partidas/`; el nombre se limpia para evitar rutas invalidas.

## Guia para agregar contenido

### Nueva mejora o productor

1. Edita los arreglos de nombres, descripciones, produccion y precios en `GameConfig.java`.
2. Mantén el mismo indice en todos los arreglos.
3. `Stats` crea los `Item` y `StorePanel` los muestra automaticamente.
4. Añade un escenario a `BalanceTest.java` si cambia el tiempo esperado de la partida.

### Nuevo minijuego

1. Crea una clase independiente en `ConsolesLocClicker/minigames/`, preferiblemente con un metodo
	estatico `show(...)` que reciba `Stats`, `Runnable onChange`, `Runnable onSuccess` y
	`Runnable onFailure`.
2. No modifiques el reloj de `Frame`; el minijuego debe usar `javax.swing.Timer`.
3. Llama a `onChange.run()` despues de cambiar estabilidad, LOC, deuda o recompensas.
4. Conecta el boton desde `DefectManager` o `EventManager`.
5. Prueba el resultado en `UiSmokeTest` si necesita una ventana real.

### Nuevo evento

1. Decide si el evento se puede resolver con habilidad o si es una consecuencia inevitable.
2. Para un minijuego, implementa `RepairEvent` en `events/catalog/` y registralo en `RepairEventCatalog`.
3. Para una consecuencia inevitable, implementa `NegativeEvent` en `events/catalog/` y registralo en
	`NegativeEventCatalog`.
4. Define su efecto en `Stats` y su texto en la clase del evento.
5. No edites el reloj central; los managers se encargan de programar y cerrar las ventanas.

Para eventos negativos usa la carpeta `events/catalog/NegativeEventCatalog`; son distintos de los
eventos con minijuego porque sus consecuencias se aplican inmediatamente y no se pueden evitar.

### Personal y equipo

La pestaña `Equipo` centraliza tres mejoras: `Computadora de trabajo` aumenta producción y
clics, `Capacitacion continua` aumenta producción y `Plan de retencion` reduce el daño de la
rotación. Los eventos de rotación retiran al empleado de mayor nivel disponible; los compañeros
pueden añadir nuevas mejoras como clases `Item` y exponerlas desde `StaffPanel`.

### Assets y sonidos

Coloca imagenes y sonidos en `assets/`. El script de compilacion copia esa carpeta al JAR.
Para una imagen usa `getResource("/assets/nombre.png")`. Para sonido, crea una clase como
`AudioManager` que cargue `Clip` desde `/assets/nombre.wav`; los paneles solo deben llamar
metodos como `playClick()` o `playAlert()`, sin conocer rutas ni archivos. Evita guardar
recursos generados dentro de `out/`.

### Guardado

`SaveManager` usa `Properties` versionadas. Si se agrega un campo nuevo a `Stats`, añade su
lectura y escritura en `saveProperties` y `loadProperties`, usando un valor por defecto para
que las partidas antiguas sigan funcionando.

## Validacion

```sh
rm -rf out && mkdir out
javac -encoding UTF-8 -d out $(find ConsolesLocClicker -name '*.java' -print)
javac -encoding UTF-8 -cp out -d out tests/BalanceTest.java tests/UiSmokeTest.java
java -cp out BalanceTest
java -cp out UiSmokeTest
```

`UiSmokeTest` necesita una pantalla X11 o `xvfb-run`. `BalanceTest` es la comprobacion rapida
que debe ejecutarse antes de cambiar precios, metas, estabilidad o recompensas.
