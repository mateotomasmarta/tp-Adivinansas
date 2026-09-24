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

`FabricaPersonajes` crea automaticamente los 23 personajes recorriendo combinaciones posibles de atributos. Usa Datafaker solo para generar nombres, no para decidir las caracteristicas del personaje. Los personajes salen con `id = 0` y el `RepositorioPersonajes` les asigna el ID definitivo despues de ordenarlos.

La fabrica valida dos condiciones:

- Que existan exactamente 23 personajes.
- Que no haya dos personajes con la misma combinacion de `genero + calvo + lentes + colorPelo + barba`.

## B1 - Repositorio y algoritmos

`RepositorioPersonajes` usa Singleton porque todos los jugadores y maquinas deben consultar el mismo tablero. La instancia se obtiene con `getInstancia()` y el constructor es privado.

Flujo de carga:

1. Pide los personajes generados a `FabricaPersonajes`.
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

## B5 - Observer entre Máquina 1 y Máquina 2

### Objetivo

El objetivo de este bloque es permitir que Máquina 2 obtenga información de las preguntas realizadas por Máquina 1 durante el primer enfrentamiento contra el jugador, sin acceder directamente al personaje secreto.

Para resolverlo se utilizó el patrón **Observer**. De esta forma, M1 puede jugar normalmente mientras M2 recibe únicamente la información pública que se genera durante la partida: el filtro utilizado y la respuesta obtenida.

### Funcionamiento

Las clases principales de este bloque son:

- `Pregunta`: representa una pregunta realizada durante un turno.
- `ResultadoPregunta`: contiene una pregunta junto con su respuesta.
- `ObservadorPreguntas`: interfaz que define el comportamiento de los observadores.
- `HistorialPreguntas`: almacena los resultados y notifica a los observadores.
- `ObservadorMaquina`: observador concreto utilizado por M2.

Cuando M1 realiza una pregunta, `Partida` obtiene la respuesta del jugador y crea un `ResultadoPregunta`. Luego, el resultado se registra en `HistorialPreguntas`.

El historial recorre los observadores registrados y, mediante `debeObservar()`, determina cuáles deben recibir esa información. En el caso de M2, `ObservadorMaquina` verifica que la pregunta haya sido realizada por M1 y esté dirigida al jugador humano.

Si se cumplen estas condiciones, M2 incorpora el conocimiento mediante:

```java
maquina.incorporarConocimiento(
        resultado.getPregunta().getFiltro(),
        resultado.isRespuestaAfirmativa());
```

A partir del filtro y la respuesta, M2 descarta de su lista los personajes que ya no pueden ser el personaje secreto del jugador. También guarda ese filtro como conocido para no repetir posteriormente una pregunta cuya respuesta ya obtuvo observando a M1.

De esta manera, M2 obtiene una ventaja para el segundo enfrentamiento sin conocer directamente el personaje secreto.

### Persistencia del conocimiento

M2 se crea antes de comenzar la partida contra M1 y queda registrada como observadora del historial:

```java
HistorialPreguntas historial = new HistorialPreguntas();
historial.agregarObservador(
        new ObservadorMaquina(maquina2, "Maquina 1", nombreJugador));
```

Si el jugador vence a M1, se inicia una nueva partida reutilizando el mismo objeto `maquina2`:

```java
Partida partida2 = new Partida(humano, maquina2);
```

Al reutilizar la misma instancia, M2 conserva los candidatos descartados y los filtros que aprendió durante la primera partida.

### Justificación del patrón Observer

Se eligió Observer para evitar una dependencia directa entre M1 y M2.

M1 no necesita conocer a M2 ni ejecutar métodos sobre ella. Su única responsabilidad es jugar su propia partida. `HistorialPreguntas` funciona como intermediario y notifica a los objetos interesados cuando se registra una nueva respuesta.

Esto reduce el acoplamiento entre las clases y permite que en el futuro puedan agregarse otros observadores sin modificar la lógica de M1.

### Estructuras de datos utilizadas

`HistorialPreguntas` utiliza listas para almacenar los resultados de las preguntas y los observadores registrados.

Por su parte, `JugadorMaquina` mantiene una lista de candidatos y una lista de filtros usados o conocidos. Estas listas se modifican durante la partida a medida que la máquina obtiene nueva información.

### Complejidad de B5

La operación principal del bloque es la incorporación de conocimiento en M2.

Si `n` representa la cantidad de candidatos, `actualizarCandidatos()` debe recorrer esa lista para conservar únicamente los personajes compatibles con la respuesta obtenida. Por lo tanto, su complejidad es:

`O(n)`

La notificación de observadores tiene complejidad `O(o)`, donde `o` representa la cantidad de observadores registrados. En la implementación actual existe un único observador para M2, por lo que este costo es constante en la práctica.

## B6 - Motor de partida

### Objetivo

El objetivo de este bloque es coordinar una partida entre exactamente dos jugadores.

La clase `Partida` controla los turnos, procesa preguntas y adivinanzas, registra las preguntas realizadas y determina cuándo termina el enfrentamiento.

El motor no decide qué estrategia utiliza cada máquina, no crea personajes, no maneja la interfaz y no guarda récords. Estas responsabilidades quedan separadas en otras partes del proyecto.

### Funcionamiento

`Partida` mantiene los siguientes datos principales:

- los dos jugadores;
- el jugador que posee el turno actual;
- la cantidad de turnos jugados;
- el estado de la partida;
- el ganador;
- el historial de preguntas.

El estado se representa con `EstadoPartida`, que puede ser `EN_CURSO` o `FINALIZADA`.

Las dos acciones principales del motor son `realizarPregunta()` y `realizarAdivinanza()`.

### Realizar una pregunta

El método:

```java
realizarPregunta(Filtro filtro)
```

obtiene al jugador actual y a su rival. Luego consulta la respuesta mediante:

```java
destinatario.responder(filtro);
```

De esta forma, quien pregunta recibe solamente una respuesta booleana y no necesita acceder directamente al personaje secreto del rival.

Si el jugador que realizó la pregunta es una máquina, se actualiza su lista de candidatos utilizando el filtro y la respuesta obtenida.

Después se crea una `Pregunta`, se genera su `ResultadoPregunta` y se registra en `HistorialPreguntas`. Finalmente se incrementa la cantidad de turnos y se cambia al siguiente jugador.

### Realizar una adivinanza

El método:

```java
realizarAdivinanza(Personaje candidato)
```

permite intentar adivinar directamente el personaje del rival.

La comprobación se realiza mediante:

```java
destinatario.responderAdivinanza(candidato);
```

Si la respuesta es correcta, el jugador actual queda registrado como ganador y el estado de la partida cambia a `FINALIZADA`.

Si la respuesta es incorrecta, la partida continúa. Cuando quien falló es una máquina, el personaje incorrecto se elimina de su lista de candidatos antes de cambiar el turno.

### Manejo de turnos

Como una partida siempre tiene exactamente dos participantes, se utiliza un arreglo:

```java
Jugador[] jugadores;
```

y un índice que indica qué jugador posee el turno.

El cambio se realiza con:

```java
indiceTurno = 1 - indiceTurno;
```

Como los únicos valores posibles son `0` y `1`, esta operación alterna directamente entre ambos jugadores.

El rival del jugador actual se obtiene de forma similar:

```java
jugadores[1 - indiceTurno]
```

### Estado y validaciones

Antes de realizar una pregunta o una adivinanza se verifica que la partida continúe en estado `EN_CURSO`. Una vez que existe un ganador, no se permiten nuevas acciones.

También se validan los datos necesarios para construir y utilizar una partida, por ejemplo:

- ambos jugadores deben existir;
- deben ser jugadores diferentes;
- deben tener nombres diferentes;
- el historial no puede ser `null`;
- el filtro o personaje recibido por los métodos no puede ser `null`.

Estas validaciones evitan que el motor quede en estados inconsistentes.

### Flujo entre M1 y M2

El enfrentamiento contra M1 y el enfrentamiento contra M2 se representan como dos objetos `Partida` distintos, ya que cada partida contiene solamente dos jugadores.

Primero se crea la partida entre el humano y M1. Si el jugador pierde, el flujo termina.

Si el jugador vence a M1, se crea una segunda partida:

```java
Partida partida2 = new Partida(humano, maquina2);
```

Se reutiliza el mismo objeto `humano`, por lo que mantiene el mismo personaje secreto, y también se reutiliza el mismo objeto `maquina2`, que conserva el conocimiento adquirido mientras observaba a M1.

El récord se registra solamente si el jugador logra vencer a las dos máquinas.

### Estructuras de datos utilizadas

La estructura principal propia de `Partida` es el arreglo de jugadores:

```java
Jugador[] jugadores;
```

Se utilizó un arreglo porque la cantidad de participantes es fija y siempre vale dos. No es necesario utilizar una estructura dinámica para una cantidad que no cambia durante la partida.

Además, `Partida` mantiene una referencia a `HistorialPreguntas`, utilizado para registrar las preguntas y permitir las notificaciones del Observer.

### Complejidad de B6

Las operaciones relacionadas con el manejo de turnos son constantes:

- obtener al jugador actual: `O(1)`;
- obtener al rival: `O(1)`;
- cambiar el turno: `O(1)`.

En `realizarPregunta()`, cuando quien pregunta es una máquina, debe actualizar su lista de candidatos. Si `n` es la cantidad de candidatos, esta operación tiene complejidad `O(n)`.

En `realizarAdivinanza()`, comprobar si el personaje es correcto es `O(1)`. Sin embargo, si una máquina falla debe eliminar al personaje de un `ArrayList`, lo que en el peor caso puede tener costo `O(n)`.

Por lo tanto, considerando las operaciones que modifican la lista de candidatos, el peor caso de las acciones principales del motor es:

`O(n)`

## Bitácora de P4 - B5 y B6

La parte P4 del trabajo se centró en dos responsabilidades: implementar el motor que coordina una partida y resolver la comunicación de información entre M1 y M2 mediante Observer.

### Primera etapa - Diseño del motor

En una primera versión se evaluó representar las posibles acciones de un turno mediante varias clases específicas para preguntas, adivinanzas y resultados.

Al avanzar con la implementación se observó que este diseño agregaba más clases y complejidad de la necesaria para las dos acciones reales del juego.

Por este motivo se simplificó el motor y se dejaron como operaciones principales de `Partida`:

```java
realizarPregunta(Filtro filtro)
realizarAdivinanza(Personaje candidato)
```

Con esta modificación, `Partida` quedó encargada solamente de coordinar jugadores, turnos, preguntas, adivinanzas y el estado del enfrentamiento.

### Segunda etapa - Implementación de Observer

El siguiente problema fue implementar la ventaja de M2.

La máquina debía conocer las preguntas y respuestas obtenidas previamente por M1, pero sin acceder directamente al personaje secreto del jugador.

Para resolverlo se implementó `HistorialPreguntas` como sujeto del patrón Observer y `ObservadorMaquina` como observador concreto de M2.

Cada vez que M1 realiza una pregunta al jugador, el resultado queda registrado en el historial. Si la pregunta corresponde a M1 y está dirigida al jugador humano, M2 recibe el filtro junto con la respuesta y actualiza su conocimiento.

Esto permitió cumplir la ventaja informativa de M2 sin generar una comunicación directa entre las dos máquinas.

### Tercera etapa - Ajuste del flujo M1 a M2

Después de una aclaración del profesor se corrigió la interpretación del modo Jugador vs Máquina.

Se definió que una `Partida` siempre representa un enfrentamiento entre dos jugadores. Por lo tanto, el jugador primero se enfrenta a M1 y solamente si la vence comienza una segunda partida contra M2.

El flujo implementado quedó:

1. Jugador vs M1.
2. Durante esa partida M2 observa las preguntas y respuestas obtenidas por M1.
3. Si el jugador pierde contra M1, termina el juego.
4. Si vence a M1, comienza automáticamente una nueva partida contra M2.
5. El jugador mantiene el mismo personaje secreto.
6. M2 mantiene el conocimiento adquirido durante la primera partida.
7. El récord se registra únicamente si el jugador vence también a M2.

Para lograrlo se reutilizan los mismos objetos `humano` y `maquina2` al crear la segunda partida.

El nuevo flujo se aplicó tanto a la versión por consola (`Main`) como a la interfaz gráfica (`MainSwing`) para mantener el mismo comportamiento en ambas formas de ejecución.

También se corrigió el momento en el que se registra una victoria. Inicialmente se guardaba al vencer a M1; luego del cambio, la victoria se registra únicamente después de vencer a M1 y M2.

# Documentacion B7
El objetivo de este bloque es proporcionar un mecanismo de persistencia para almacenar y listar el historial acumulado de victorias de los jugadores.

RecordDAO(Interfaz): define el contrato para gestionar los records(cargar, guardar, mostrar, registrarVictorio y listarOrdenado)

RecordJugador: encapsula los datos de un registro en el marcador(nombre y partidasGanadas). Solo expone getters y un metodo toString() formateado para la presentacion

RecordArchivoDAO: lee y escribe en un archivo CSV

Cargar en memoria(cargar): utiliza BufferedReader y FileReader. Antes de procesar, verifica la existencia del archivo mediante FileExists().
Si el archivo no existe, la aplicacion continua en un estado valido con un marcador vacio. Cada linea se parsea descartando espacios, lineas en blanco o entradas incompletas. Las lineas corruptas se capturan con un bloque try-catch sobre NumberFormatException y se ignoran silenciosamente sin interrumpir la carga del resto de los registros.

Guardar(guardar): Utiliza FileWriter y PrintWriter. Antes de escribir, invoca archivo.getParentFile().mkdirs() para asegurar la creacion automatica del directorio datos/ si no existe.

Registro de victoria(registrarVictoria): incrementa en 1 las victorias del jugador en la estructura de memoria utilizando records.getOrDefault(nombre, 0) + 1

Estructura de datos utilizada:
Map<String, Integer> records: un HashMap utilizado para almacenar la relacion nombre de jugador -> cantidad de victorias

List<RecordJugador> lista dinamica temporal construida al invocar listarOrdenado() que luego es procesada por OrdenadorMergeSort

# Complejidad de B7
Consulta y registro individual: O(1) debido al acceso por clave en el HashMap
Carga y Guardado en disco: O(n) donde n es la cantidad de registros unicos almacenados en el archivo
Listado ordenado (listarOrdenado): O(n log n) impulsado por la complejidad temporal de MergeSort.


# Documentacion de B8
El bloque B8 proporciona un modo espectador en el que las dos maquinas compiten entre si. Su proposito principal es hacer visible el proceso de razonamiento de ambas maquinas, mostrando turno a turno como se reduce el espacio de busqueda

Es implementada con resursividad:
Casos base:
        1. Finalizacion de la partida: partida.getEstado() == EstadoPartida.FINALIZADA. Ocurre cuando una de las maquinas adivina correctamente el personaje rival. Imprime el ganador y el total de turnos jugados.
        2. Cota superior: numeroTurno > LIMITE_TURNOS(fijado en 60). Funciona como seguridad para evitar StackOverFlow

Paso recursivo(simularTurnoRecursivo): identifica al atacante y defensor del turno actual, procesa la jugada correspondiente y se autoinvoca pasando el estado actualizado y el contador de turnos incrementado

Estructuras utilizadas:
        1. List<Personaje> candidatos: lista dinamica obtenida en atacante.getCandidatos() que representa los personajes disponibles para esa maquina
        2. List<Filtro> disponibles: copia dinamica creada en cada turno a partir de CatalogoFiltros.listarTodos() a la cual se le remueven los filtros de maquina.getFiltrosUsados()

# Complejidad de B8
Por turno: O(fn) donde f es la cantidad de filtros disponibles y n los candidatos actuales. En cada turno se recorren los candidatos probando los filtros para ver si corresponde o no

Recursividad: O(log n) al acortar los candidatos en cada turno la pila recusiva no crece mucho; aunque usamos un limite de turnos para garantizar que la memoria no crezca infinitamente

Entonces en resumen la complejidad es O(fn) en el peor caso, ya que la cantidad de candidatos a evaluar cae exponencialmente turno a turno