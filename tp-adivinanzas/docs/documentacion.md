# Documentacion B0 y B1

## B0 - Modelo de datos

El modelo define los enums `Genero` y `ColorPelo`. En `ColorPelo` se incluye `NINGUNO` para representar a los personajes calvos. Esta decision evita usar `null`, por lo que los filtros pueden comparar enums sin agregar validaciones especiales ni exponerse a errores en runtime.

`Personaje` es inmutable: todos sus atributos son `final`, no tiene setters y solo puede cambiarse creando otra instancia. `equals` y `hashCode` usan el `id`, porque dentro del juego el identificador es la identidad del personaje.

Campos del personaje:

- `id`
- `nombre`
- `genero`
- `calvo`
- `lentes`
- `colorPelo`
- `barba`

Se agrego `barba` como quinto atributo porque el espacio original no alcanza para 23 personajes distinguibles:

`2 generos * 2 lentes * (1 calvo + 3 colores de pelo) = 16 combinaciones`

Como se necesitan 23 personajes, con solo genero, calvicie, lentes y color de pelo es matematicamente imposible evitar repetidos. Si dos personajes tienen la misma combinacion de atributos, una maquina que filtra candidatos no puede distinguirlos y la partida puede quedar irresoluble. Con `barba` el espacio sube a 32 combinaciones, suficiente para los 23 personajes.

`FabricaPersonajes` crea los 23 personajes hardcodeados y valida dos condiciones:

- Que existan exactamente 23 personajes.
- Que no haya dos personajes con la misma combinacion de `genero + calvo + lentes + colorPelo + barba`.

## B1 - Repositorio y algoritmos

`RepositorioPersonajes` usa Singleton porque todos los jugadores y maquinas deben consultar el mismo tablero. La instancia se obtiene con `getInstancia()` y el constructor es privado.

Flujo de carga:

1. Pide los personajes a `FabricaPersonajes`.
2. Los ordena por genero usando `OrdenadorMergeSort`.
3. Asigna los IDs del 1 al 23 segun la posicion final.

Ese orden es importante: al asignar el ID despues de ordenar, la lista queda ordenada por ID por construccion. Esa es la precondicion que permite usar busqueda binaria.

`OrdenadorMergeSort` implementa merge sort propio, sin `Collections.sort()`. Recibe una lista y un `Comparator`, por lo que puede reutilizarse con distintos criterios. Es estable porque, ante dos elementos iguales para el comparador, conserva primero el elemento de la mitad izquierda.

Propiedades defendibles de merge sort:

- Divide y venceras: parte la lista, ordena cada mitad y combina.
- Complejidad temporal `O(n log n)` en mejor, promedio y peor caso.
- Complejidad espacial `O(n)` por listas auxiliares.
- Es estable.
- Evita el peor caso de un QuickSort con pivote ingenuo sobre entradas parcialmente ordenadas.

`BuscadorBinario` busca por ID de forma iterativa. Su complejidad es `O(log n)` porque descarta la mitad del rango en cada paso. Con 23 personajes requiere como maximo 5 comparaciones, ya que `log2(23)` es aproximadamente 4.5.

La precondicion critica es que la lista debe estar ordenada por ID. Si alguien rompe ese orden, la busqueda binaria puede devolver resultados incorrectos sin avisar.

## B2 - Filtros (Specification)

`Filtro` es una Specification: encapsula una unica condicion sobre un `Personaje` (`cumple`) y sabe describirse como pregunta de si/no (`getDescripcion`). Las implementaciones concretas son `FiltroGenero`, `FiltroCalvicie`, `FiltroLentes`, `FiltroColorPelo` y `FiltroBarba`.

`FiltroBarba` no estaba en el set inicial de archivos, pero es necesaria: en B0 se agrego `barba` como quinto atributo del `Personaje` especificamente para que los 23 personajes sean distinguibles entre si. Si nunca se pudiera preguntar por barba, dos personajes identicos en genero, calvicie, lentes y color de pelo pero distintos en barba serian indistinguibles para la maquina, y la garantia de unicidad de B0 no serviria para nada en el juego real.

Los filtros se componen sin tocar las clases concretas, via los metodos default de la interfaz:

- `y(Filtro otro)`: AND logico entre dos filtros.
- `o(Filtro otro)`: OR logico.
- `negar()`: NOT logico.

Cada composicion devuelve una implementacion anonima de `Filtro` que delega `cumple` en los filtros originales y arma una descripcion combinada. Esto es Open/Closed: se pueden expresar preguntas compuestas ("es mujer y usa lentes") sin modificar `FiltroGenero` ni `FiltroLentes`.

`CatalogoFiltros` enumera todas las preguntas de si/no posibles: un filtro por cada atributo booleano (preguntar por el negativo no aporta informacion nueva) y un `FiltroColorPelo` por cada color real (`COLORADO`, `NEGRO`, `AMARILLO`). No incluye un filtro para `NINGUNO` porque ese valor ya queda determinado por la respuesta de `FiltroCalvicie`. En total expone 7 filtros, cacheados en una lista estatica para no recrear instancias en cada turno.

## B3 - Jugadores (Template Method)

`Jugador` es una clase abstracta que define el esqueleto del comportamiento comun a cualquier participante de la partida, con un metodo fijo y dos pasos que varian por subclase:

- `responder(Filtro filtro)` es **final**. Este es el "oraculo": aplica el filtro directamente sobre el `personajeSecreto` propio y devuelve el resultado. Al ser final, ninguna subclase -ni `JugadorHumano` ni `JugadorMaquina`- puede sobreescribirlo para mentir sobre su propio personaje. La unica forma de que una respuesta sea incorrecta seria un bug en el `Filtro`, nunca una decision estrategica del jugador.
- `elegirPregunta()` y `arriesgarPersonaje()` son abstractos: son los pasos que si varian.

Ninguna de las dos subclases hace su trabajo sola: ambas delegan la decision en una abstraccion inyectada, siguiendo Dependency Inversion.

- `JugadorMaquina` delega en una `Estrategia` (interfaz definida en este bloque, implementada en B4 por `EstrategiaAsertiva` y `EstrategiaConservadora`). Mantiene su propio pool de `candidatos` (arranca con todos los personajes) y lo reduce con `actualizarCandidatos(Filtro, boolean)` cada vez que el rival responde una de sus preguntas, descartando a quienes no cumplan la respuesta de la misma forma que el personaje real. Tambien lleva la lista de `filtrosUsados` para no repetir una pregunta ya hecha. La `Estrategia` nunca recibe el personaje secreto del rival, solo candidatos y filtros disponibles: toda la garantia de "no hacer trampa" queda concentrada en el `responder()` final de `Jugador`, no en la estrategia.
- `JugadorHumano` delega en `InterfazHumano` (misma idea que `Estrategia`, pero para resolver la interaccion con una persona en vez de un algoritmo). `JugadorHumano` no hace `System.out`/`Scanner` por si mismo: le pide a la `InterfazHumano` que elija un filtro o un personaje, pasandole las opciones disponibles. Quien implementa `InterfazHumano` con la consola real es `ConsolaJuego` (B8, Persona 5); asi ninguna de las dos clases de `Jugador` sabe nada sobre como se muestra la informacion en pantalla.

`Estrategia` e `InterfazHumano` las definimos en el bloque de jugadores en lugar de en los bloques que las implementan, porque son `JugadorMaquina` y `JugadorHumano` -los modulos de alto nivel- quienes fijan la abstraccion que necesitan (Dependency Inversion): no dependen de las implementaciones concretas, solo de estos contratos.
