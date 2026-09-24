# Adivina Quien - Documentacion tecnica

Trabajo practico de Programacion III (UADE). Juego de adivinanzas por turnos
entre un jugador humano y dos maquinas con estrategias distintas, con un modo
espectador maquina vs maquina que hace visible el razonamiento de cada una.

Esta documentacion justifica los patrones de diseño y las decisiones
algoritmicas del proyecto. Cada bloque describe que resuelve, como lo resuelve,
que estructuras de datos usa y cual es su complejidad.

---

## Indice

1. [Resumen de patrones aplicados](#1-resumen-de-patrones-aplicados)
2. [Tabla general de complejidades](#2-tabla-general-de-complejidades)
3. [B0 - Modelo de datos](#3-b0---modelo-de-datos)
4. [B1 - Repositorio y algoritmos de ordenamiento y busqueda](#4-b1---repositorio-y-algoritmos-de-ordenamiento-y-busqueda)
5. [B2 - Filtros (Specification)](#5-b2---filtros-specification)
6. [B3 - Jugadores (Template Method)](#6-b3---jugadores-template-method)
7. [B4 - Estrategias (Strategy): greedy y minimax](#7-b4---estrategias-strategy-greedy-y-minimax)
8. [B5 - Observer entre Maquina 1 y Maquina 2](#8-b5---observer-entre-maquina-1-y-maquina-2)
9. [B6 - Motor de partida](#9-b6---motor-de-partida)
10. [B7 - Persistencia del marcador (DAO)](#10-b7---persistencia-del-marcador-dao)
11. [B8 - Modo espectador y capa de interfaz](#11-b8---modo-espectador-y-capa-de-interfaz)
12. [Analisis de eficiencia: medicion empirica](#12-analisis-de-eficiencia-medicion-empirica)
13. [Algoritmos considerados y NO aplicados](#13-algoritmos-considerados-y-no-aplicados)
14. [Recursividad: riesgos y como los evitamos](#14-recursividad-riesgos-y-como-los-evitamos)
15. [Bitacora de decisiones](#15-bitacora-de-decisiones)
16. [Limitaciones conocidas](#16-limitaciones-conocidas)

---

## 1. Resumen de patrones aplicados

| Patron | Clases | Que problema resuelve |
|---|---|---|
| **Singleton** | `RepositorioPersonajes` | Todos los jugadores deben ver el mismo tablero de 23 personajes |
| **Factory** | `FabricaPersonajes` | Centraliza la creacion de los personajes y valida que sean distinguibles |
| **Specification** | `Filtro` y sus implementaciones | Cada pregunta del juego es un objeto con una unica condicion |
| **Template Method** | `Jugador` | Fija el paso que no puede variar (`responder`) y deja abstractos los que si |
| **Strategy** | `Estrategia`, `EstrategiaAsertiva`, `EstrategiaConservadora` | Dos maquinas con formas distintas de jugar sin tocar el motor |
| **Observer** | `HistorialPreguntas`, `ObservadorPreguntas`, `ObservadorMaquina` | M2 conoce las preguntas de M1 sin que M1 conozca a M2 |
| **DAO** | `RecordDAO`, `RecordArchivoDAO` | Aisla el "como" se persiste del "que" se persiste |
| **Facade** | `Main`, `MainSwing` | Unico punto que conoce y cablea todos los subsistemas |
| **Dependency Inversion** | `Estrategia`, `InterfazHumano` | Los modulos de alto nivel fijan la abstraccion que necesitan |

Criterio general: no se aplico ningun patron "de adorno". Cada uno resuelve un
requisito concreto del enunciado. Por ejemplo, se evaluo usar **Command** para
representar las acciones del turno y se descarto: el historial que necesitaba
el Observer se resuelve con una `List<ResultadoPregunta>`, y agregar una
jerarquia de comandos habria sumado clases sin resolver nada nuevo.

---

## 2. Tabla general de complejidades

`n` = cantidad de candidatos (23 al inicio), `f` = cantidad de filtros (7),
`r` = cantidad de registros en el marcador.

| Operacion | Temporal | Espacial | Donde |
|---|---|---|---|
| Crear y validar los 23 personajes | O(n) | O(n) | `FabricaPersonajes` |
| Ordenamiento inicial (MergeSort) | O(n log n) | O(n) | `OrdenadorMergeSort` |
| Buscar personaje por ID | O(log n) | O(1) | `BuscadorBinario` |
| Evaluar un filtro sobre un personaje | O(1) | O(1) | `Filtro.cumple` |
| Aplicar un filtro a los candidatos | O(n) | O(n) | `JugadorMaquina.actualizarCandidatos` |
| Elegir pregunta - **estrategia asertiva** | O(n · f) | O(1) | `EstrategiaAsertiva` |
| Elegir pregunta - **estrategia conservadora** | O(f) | O(1) | `EstrategiaConservadora` |
| Notificar observadores | O(o) | O(1) | `HistorialPreguntas` |
| Cambiar de turno | O(1) | O(1) | `Partida` |
| Registrar o consultar una victoria | O(1) promedio | O(1) | `RecordArchivoDAO` (HashMap) |
| Cargar o guardar el marcador | O(r) | O(r) | `RecordArchivoDAO` |
| Listar el marcador ordenado | O(r log r) | O(r) | `RecordArchivoDAO` + MergeSort |
| **Turnos de una partida - asertiva** | O(log n) | — | ver B4 |
| **Turnos de una partida - conservadora** | O(f) hasta O(n) | — | ver B4 |

La diferencia importante esta en las dos ultimas filas: no en el costo de cada
turno, sino en **cuantos turnos hacen falta**. Es la metrica que realmente
decide quien gana.

---

## 3. B0 - Modelo de datos

El modelo define los enums `Genero` y `ColorPelo`. En `ColorPelo` se incluye
`NINGUNO` para representar a los personajes calvos. Esta decision evita usar
`null`, por lo que los filtros pueden comparar enums sin agregar validaciones
especiales ni exponerse a `NullPointerException` en tiempo de ejecucion.

`Personaje` es inmutable: todos sus atributos son `final`, no tiene setters y
solo puede modificarse creando otra instancia (`conId`). `equals` y `hashCode`
usan el `id`, porque dentro del juego el identificador es la identidad del
personaje.

Campos: `id`, `nombre`, `genero`, `calvo`, `lentes`, `colorPelo`, `barba`.

### El problema del espacio de atributos

El enunciado pide 23 personajes distinguibles y propone cuatro atributos. Con
esos cuatro, el espacio de combinaciones validas es:

```
2 generos × 2 lentes × (1 calvo + 3 colores de pelo) = 16 combinaciones
```

Como se necesitan **23 personajes** y solo existen **16 combinaciones**, es
matematicamente imposible que todos sean distinguibles. Si dos personajes
comparten la combinacion exacta de atributos, una maquina que filtra candidatos
nunca puede separarlos: la partida queda irresoluble.

Se agrego `barba` como quinto atributo, lo que eleva el espacio a
`2 × 2 × 2 × 4 = 32` combinaciones, suficiente para los 23 personajes con
margen.

Esta es una correccion a la consigna, no un capricho de diseño: sin ella la
garantia de unicidad no se puede cumplir.

### Fabrica y validacion

`FabricaPersonajes` (patron Factory) genera los 23 personajes recorriendo
sistematicamente las combinaciones de atributos. Usa la libreria Datafaker
**solo para los nombres**, nunca para decidir caracteristicas: si los atributos
fueran aleatorios no habria garantia de unicidad.

La fabrica valida dos invariantes y lanza excepcion si alguna falla:

- que existan exactamente 23 personajes;
- que no haya dos personajes con la misma combinacion de
  `genero + calvo + lentes + colorPelo + barba`.

Ademas `Personaje` valida en su constructor la coherencia entre calvicie y
color de pelo: un calvo debe tener `ColorPelo.NINGUNO` y un no calvo debe tener
un color real. Asi el estado invalido no es representable.

Los personajes salen de la fabrica con `id = 0`; el `RepositorioPersonajes` les
asigna el ID definitivo despues de ordenarlos (ver B1).

---

## 4. B1 - Repositorio y algoritmos de ordenamiento y busqueda

`RepositorioPersonajes` usa **Singleton** porque todos los jugadores y maquinas
deben consultar exactamente el mismo tablero. La instancia se obtiene con
`getInstancia()` y el constructor es privado.

### Flujo de carga

1. Pide los personajes a `FabricaPersonajes` (vienen sin ID).
2. Los ordena por genero con `OrdenadorMergeSort`.
3. Asigna los IDs del 1 al 23 segun la posicion final.

**El orden de estos pasos es la decision clave del bloque.** Al asignar el ID
*despues* de ordenar, la lista queda ordenada por ID por construccion. Esa es
exactamente la precondicion que habilita la busqueda binaria. Si los IDs se
asignaran antes de ordenar, quedarian desordenados y la busqueda deberia ser
lineal, O(n).

### MergeSort

`OrdenadorMergeSort` es una implementacion propia, sin `Collections.sort()`.
Recibe una `List<T>` y un `Comparator<T>`, por lo que se reutiliza con
distintos criterios: ordenar personajes por genero (B1) y ordenar el marcador
por victorias (B7). Un unico algoritmo de ordenamiento en todo el proyecto.

Propiedades:

- **Divide y venceras**: parte la lista en dos mitades, ordena cada una
  recursivamente y las combina en el `merge`.
- **O(n log n) en mejor, promedio y peor caso.** La profundidad de la
  recursion es `log n` y cada nivel hace `n` comparaciones. No depende de como
  venga la entrada.
- **O(n) de espacio auxiliar**, por las listas intermedias. Es el precio que se
  paga por la garantia anterior.
- **Estable**: ante dos elementos que el comparador considera iguales, conserva
  primero el de la mitad izquierda (la comparacion usa `<= 0`).

La estabilidad importa aca: al ordenar por genero, los personajes que comparten
genero mantienen el orden en que los genero la fabrica, de modo que la carga es
reproducible entre ejecuciones.

### Busqueda binaria

`BuscadorBinario.buscarPorId` es **iterativa**. En cada vuelta calcula el punto
medio y descarta la mitad del rango:

```java
int medio = inicio + (fin - inicio) / 2;
```

- **Complejidad O(log n)**: el indicador en el codigo es que el rango se
  **divide** en cada paso en vez de avanzar de a uno. Ese es el patron que
  distingue O(log n) de O(n) a simple vista.
- Con 23 personajes necesita como maximo **5 comparaciones**, porque
  `log2(23) ≈ 4.52` y el techo es 5.
- Se escribio iterativa y no recursiva a proposito: usa O(1) de espacio en vez
  de O(log n) de pila, y la logica es igual de clara.
- `inicio + (fin - inicio) / 2` en lugar de `(inicio + fin) / 2` evita el
  desbordamiento de entero en listas muy grandes. Con n = 23 es irrelevante,
  pero es la forma correcta de escribirlo.

**Precondicion critica:** la lista debe estar ordenada por ID. Si alguien
rompiera ese orden, la busqueda devolveria resultados incorrectos **sin lanzar
ningun error**, que es el tipo de bug mas dificil de encontrar. Por eso el
repositorio expone la lista con `Collections.unmodifiableList`: nadie puede
reordenarla desde afuera.

---

## 5. B2 - Filtros (Specification)

`Filtro` es una **Specification**: encapsula una unica condicion sobre un
`Personaje` (`cumple`) y sabe describirse como pregunta de si/no
(`getDescripcion`). Las implementaciones concretas son `FiltroGenero`,
`FiltroCalvicie`, `FiltroLentes`, `FiltroColorPelo` y `FiltroBarba`.

`FiltroBarba` no estaba en el diseño inicial, pero es necesario: en B0 se
agrego `barba` justamente para que los 23 personajes fueran distinguibles. Si
nunca se pudiera preguntar por barba, dos personajes identicos en los otros
cuatro atributos serian indistinguibles para la maquina y la garantia de
unicidad de B0 no serviria de nada en el juego real.

### Catalogo de preguntas

`CatalogoFiltros` enumera todas las preguntas de si/no posibles:

- un filtro por cada atributo booleano (preguntar por el negativo no aporta
  informacion nueva: la respuesta "no" a "¿usa lentes?" ya es la respuesta "si"
  a "¿no usa lentes?");
- un `FiltroColorPelo` por cada color real (`COLORADO`, `NEGRO`, `AMARILLO`).
  No hay filtro para `NINGUNO` porque ese valor ya queda determinado por la
  respuesta de `FiltroCalvicie`.

En total, **7 filtros**.

### Identidad por referencia

Los filtros se crean una sola vez y se guardan en una lista estatica
inmutable. Esto no es solo para evitar crear objetos en cada turno: es
**necesario para que la maquina no repita preguntas**.

Los filtros no implementan `equals`, asi que cuando `JugadorMaquina` usa
`removeAll` o `contains` sobre `filtrosUsados`, la comparacion es por
referencia. Como el catalogo siempre devuelve los mismos objetos, funciona.

**Consecuencia para quien trabaje sobre el codigo:** los filtros del juego
siempre deben salir de `CatalogoFiltros`, nunca crearse con `new` en otro lado.
Un filtro creado aparte seria un objeto distinto y la maquina podria repetir la
misma pregunta indefinidamente.

### Estructuras y complejidad

- `List<Filtro>` en `CatalogoFiltros`, envuelta en
  `Collections.unmodifiableList`: nadie puede agregar ni sacar filtros por error.

| Operacion | Complejidad |
|---|---|
| `cumple()` | O(1) - compara un solo atributo |
| Aplicar un filtro a n candidatos | O(n) |
| `listarTodos()` | O(1) - devuelve la lista ya construida |

---

## 6. B3 - Jugadores (Template Method)

`Jugador` es una clase abstracta que define el esqueleto del comportamiento
comun a cualquier participante, con un paso fijo y dos que varian:

- **`responder(Filtro)` es `final`.** Este es el "oraculo": aplica el filtro
  sobre el `personajeSecreto` propio y devuelve el booleano. Al ser `final`,
  ninguna subclase -ni `JugadorHumano` ni `JugadorMaquina`- puede
  sobreescribirlo para mentir sobre su propio personaje. La unica forma de que
  una respuesta sea incorrecta seria un bug en el `Filtro`, nunca una decision
  estrategica del jugador.
- **`elegirPregunta()` y `arriesgarPersonaje()` son abstractos**: son los pasos
  que dependen de si detras hay una persona o un algoritmo.

Ese reparto es el patron **Template Method**: la clase base fija que es lo que
no puede variar y delega el resto.

### Por que la maquina no puede hacer trampa

El requisito del enunciado ("la maquina no puede acceder directamente a la
variable del personaje elegido por el jugador humano") se cumple **por diseño,
no por convencion**:

- el `personajeSecreto` es privado y `final`;
- la unica via de informacion hacia el rival es `responder(Filtro)`, que
  devuelve un `boolean`;
- `responderAdivinanza(Personaje)` comprueba un intento y tambien devuelve solo
  un booleano, sin entregar el personaje.

La `Estrategia` nunca recibe el personaje secreto del rival: solo la lista de
candidatos y los filtros disponibles. Toda la garantia queda concentrada en un
unico metodo `final`, que es facil de auditar.

### Dependency Inversion en las dos subclases

Ninguna de las dos subclases resuelve sola su paso variable: ambas delegan en
una abstraccion inyectada.

- **`JugadorMaquina`** delega en `Estrategia`. Mantiene su propio pool de
  `candidatos` (arranca con los 23) y lo reduce con
  `actualizarCandidatos(Filtro, boolean)` cada vez que obtiene una respuesta,
  descartando a quienes no cumplan la condicion igual que el personaje real.
  Tambien lleva `filtrosUsados` para no repetir preguntas.
- **`JugadorHumano`** delega en `InterfazHumano`. No hace `System.out` ni
  `Scanner` por si mismo: le pide a la interfaz que elija un filtro o un
  personaje. Por eso el mismo `JugadorHumano` funciona sin cambios con la
  consola (`ConsolaJuego`) y con la interfaz grafica (`InterfazSwing`).

`Estrategia` e `InterfazHumano` se declaran en el paquete de jugadores y no en
los paquetes que las implementan, porque son `JugadorMaquina` y `JugadorHumano`
-los modulos de alto nivel- quienes fijan la abstraccion que necesitan. Esa es
la forma correcta de aplicar **Dependency Inversion**: la abstraccion pertenece
a quien la consume, no a quien la implementa.

---

## 7. B4 - Estrategias (Strategy): greedy y minimax

Este bloque resuelve el requisito central del enunciado: **dos jugadores
maquina con formas diferentes de jugar, una mas asertiva que la otra.**

### El patron

`Estrategia` es la abstraccion de la que depende `JugadorMaquina`. Define tres
decisiones:

```java
Filtro elegirFiltro(List<Personaje> candidatos, List<Filtro> filtrosDisponibles);
Personaje elegirPersonaje(List<Personaje> candidatos);
default boolean debeArriesgar(List<Personaje> candidatos);
```

El tercero merece una aclaracion: las dos maquinas no se diferencian solo en
**que** preguntan, sino tambien en **cuando dejan de preguntar**. Sin
`debeArriesgar` esa mitad de la diferencia no tendria donde vivir y la decision
quedaria hardcodeada en el motor, rompiendo el patron. Se declaro como metodo
`default` para no romper implementaciones previas de la interfaz.

`JugadorMaquina` no conoce las clases concretas: se le inyecta una `Estrategia`
por constructor. Cambiar el comportamiento de una maquina es cambiar el objeto
que se le pasa, sin tocar ni una linea del motor. Ese es el aporte de
**Strategy**.

### Maquina 1: greedy con criterio minimax

`EstrategiaAsertiva` evalua en cada turno **todos** los filtros disponibles y
elige el que minimiza el peor caso posible:

```
mejorFiltro = argmin ( max( |responden SI| , |responden NO| ) )
```

Para cada filtro cuenta cuantos candidatos responderian que si y cuantos que
no, y se queda con aquel cuyo grupo mas grande sea lo mas chico posible. Es
decir: **elige la pregunta que mejor parte el conjunto por la mitad, pase lo
que pase.**

**Por que es greedy.** Optimiza el turno actual sin simular los turnos
siguientes: mira un solo nivel hacia adelante y no construye el arbol de
decision completo. El optimo global exigiria explorar todas las secuencias
posibles de preguntas, lo que tiene costo exponencial. Este criterio local
resulta casi optimo porque la funcion de particion es monotona: partir mas
cerca de la mitad nunca puede empeorar la profundidad esperada del arbol.

Esta tension -greedy barato y casi optimo, frente a exhaustivo caro y exacto-
es la decision algoritmica principal del trabajo.

**Relacion con la busqueda binaria.** Si cada pregunta parte el espacio de
candidatos por la mitad, el comportamiento es el mismo que el de una busqueda
binaria, solo que sobre un **espacio de atributos** en vez de un espacio de
indices. De ahi sale la cota teorica:

```
techo( log2(23) ) = 5 preguntas
```

No es una analogia: es literalmente la misma reduccion logaritmica que en
`BuscadorBinario`, con la diferencia de que aca el "corte" no siempre cae justo
en la mitad, porque depende de como esten distribuidos los atributos.

**Cuando arriesga.** Solo con un unico candidato. La justificacion: con `k`
candidatos, arriesgar acierta con probabilidad `1/k` y consume un turno;
preguntar tambien consume un turno pero **garantiza** reducir el conjunto. Como
todos los personajes son distinguibles entre si (B0), si dos candidatos
sobrevivieron a las mismas respuestas necesariamente difieren en algun filtro
todavia sin usar: siempre existe una pregunta util, y por lo tanto nunca
conviene adivinar a ciegas.

**Complejidad:** O(n · f) por turno, con n candidatos y f filtros. Con n = 23 y
f = 7 son unas 161 evaluaciones: el costo de pensar es despreciable frente al
costo de preguntar de mas.

### Maquina 2: guion fijo

`EstrategiaConservadora` es la contracara deliberada, y se diferencia en los
dos ejes:

1. **Que preguntar.** Recorre un orden de preferencia definido de antemano y
   toma el primer filtro del guion que siga disponible, **sin mirar como parte
   el conjunto de candidatos.** Puede elegir filtros muy desbalanceados (por
   ejemplo 4 contra 19) e incluso filtros que en ese momento no descartan a
   nadie, quemando el turno.
2. **Cuando arriesgar.** Es impaciente: apenas los candidatos bajan del umbral
   (3 por defecto) tira una adivinanza al azar en vez de terminar de
   discriminar. Acierta con probabilidad `1/k` y, si falla, regala el turno.

**Complejidad:** O(f) por turno -apenas recorre el guion- contra O(n · f) de la
asertiva. Ahorra en el costo de decidir y lo paga en cantidad de turnos.

**Ese contraste es el punto del bloque: menos computo por turno no significa
mejor algoritmo.** Sin garantia de corte por la mitad, la reduccion deja de ser
logaritmica y en el peor caso degenera hacia un recorrido lineal.

### Propiedad que conviene remarcar en la defensa

La conservadora acepta un guion inyectable por constructor. Eso permite
demostrar empiricamente que **su rendimiento depende enteramente de que tan
bueno sea el guion que le toco**: con el orden del catalogo promedia 6.26
turnos y con el orden invertido 7.22 (ver seccion 12).

La asertiva, en cambio, da el mismo resultado sin importar como este ordenado
el catalogo, porque lo evalua entero en cada turno. **No es que sea "un poco
mejor": es que no depende de la suerte.**

### Estructuras de datos

- `List<Personaje> candidatos`: lista dinamica que se reconstruye filtrada en
  cada turno. Se crea una lista nueva en vez de eliminar in situ para no
  invalidar iteradores mientras se recorre.
- `List<Filtro> filtrosDisponibles`: copia del catalogo menos los ya usados.
- `Random` con semilla inyectable en la conservadora, para que las
  simulaciones sean reproducibles.

---

## 8. B5 - Observer entre Maquina 1 y Maquina 2

### Objetivo

Permitir que Maquina 2 aproveche las preguntas realizadas por Maquina 1 durante
la partida contra el jugador, **sin acceder al personaje secreto** y sin que M1
tenga que conocer la existencia de M2.

Se resolvio con el patron **Observer**. M1 juega su partida normalmente
mientras M2 recibe unicamente la informacion publica que se genera: el filtro
usado y la respuesta obtenida.

### Clases del bloque

- `Pregunta`: pregunta realizada en un turno (turno, autor, destinatario, filtro).
- `ResultadoPregunta`: una pregunta junto con su respuesta.
- `ObservadorPreguntas`: interfaz de los observadores.
- `HistorialPreguntas`: sujeto observable; almacena resultados y notifica.
- `ObservadorMaquina`: observador concreto que usa M2.

### Funcionamiento

Cuando M1 pregunta, `Partida` obtiene la respuesta del jugador, crea un
`ResultadoPregunta` y lo registra en `HistorialPreguntas`. El historial recorre
los observadores y, mediante `debeObservar()`, decide cuales deben recibir esa
informacion. `ObservadorMaquina` verifica que la pregunta haya sido hecha por
M1 y este dirigida al jugador humano.

Si se cumple, M2 incorpora el conocimiento:

```java
maquina.incorporarConocimiento(
        resultado.getPregunta().getFiltro(),
        resultado.isRespuestaAfirmativa());
```

A partir del filtro y la respuesta, M2 descarta de su lista los personajes que
ya no pueden ser el secreto del jugador. Tambien marca ese filtro como conocido
para no gastar un turno preguntando algo cuya respuesta ya escucho.

### Persistencia del conocimiento entre partidas

M2 se crea antes de que empiece la partida contra M1 y queda registrada como
observadora:

```java
HistorialPreguntas historial = new HistorialPreguntas();
historial.agregarObservador(
        new ObservadorMaquina(maquina2, "Maquina 1", nombreJugador));
```

Si el jugador vence a M1, se inicia una segunda partida reutilizando **la misma
instancia** de M2:

```java
Partida partida2 = new Partida(humano, maquina2);
```

Al reutilizar el objeto, M2 conserva los candidatos que ya descarto y los
filtros que aprendio. Esa es, concretamente, la ventaja que pide el enunciado.

### Justificacion del patron

Se eligio Observer para evitar una dependencia directa entre M1 y M2. M1 no
necesita conocer a M2 ni invocar metodos sobre ella: su unica responsabilidad
es jugar su partida. `HistorialPreguntas` actua de intermediario.

**Alternativa descartada:** pasarle a M2 la lista de preguntas por parametro.
Funcionaria, pero obligaria a que alguien -el motor o M1- supiera que M2 existe
y tuviera que acordarse de actualizarla. Con Observer, agregar un segundo
observador (por ejemplo, un registro para la traza) no requiere tocar nada de
la logica existente.

### Estructuras y complejidad

`HistorialPreguntas` usa listas para los resultados y para los observadores.
`JugadorMaquina` mantiene su lista de candidatos y la de filtros conocidos.

| Operacion | Complejidad |
|---|---|
| `actualizarCandidatos()` | O(n) - recorre los candidatos una vez |
| Notificar observadores | O(o) - con un unico observador, constante en la practica |

---

## 9. B6 - Motor de partida

### Objetivo

Coordinar una partida entre exactamente dos jugadores: controlar turnos,
procesar preguntas y adivinanzas, registrar el historial y determinar cuando
termina.

El motor **no** decide estrategias, **no** crea personajes, **no** maneja la
interfaz y **no** guarda records. Esas responsabilidades viven en otros
bloques. Esa separacion es lo que permite que el mismo `Partida` sirva sin
cambios para consola, Swing y el modo espectador.

### Datos que mantiene

Los dos jugadores, el indice del turno actual, la cantidad de turnos jugados,
el estado (`EN_CURSO` o `FINALIZADA`), el ganador y el historial de preguntas.

### Realizar una pregunta

```java
realizarPregunta(Filtro filtro)
```

Obtiene al jugador actual y a su rival, y consulta:

```java
destinatario.responder(filtro);
```

Quien pregunta recibe solamente un booleano: nunca accede al personaje secreto
del rival. Si quien pregunto es una maquina, se actualiza su lista de
candidatos. Despues se crea la `Pregunta`, se arma el `ResultadoPregunta`, se
registra en el historial (lo que dispara las notificaciones del Observer), se
incrementa el contador de turnos y se cambia de jugador.

### Realizar una adivinanza

```java
realizarAdivinanza(Personaje candidato)
```

La comprobacion se hace con `destinatario.responderAdivinanza(candidato)`, que
tambien devuelve solo un booleano.

Si acierta, el jugador actual queda como ganador y el estado pasa a
`FINALIZADA`. Si falla, la partida continua; cuando quien fallo es una maquina,
el personaje incorrecto se elimina de sus candidatos antes de cambiar el turno.
Asi se cumple el requisito de que "se pierde cuando gana el otro": una
adivinanza equivocada no termina la partida, cuesta el turno.

### Manejo de turnos

Como siempre hay exactamente dos participantes, se usa un arreglo fijo y un
indice:

```java
Jugador[] jugadores;
indiceTurno = 1 - indiceTurno;
```

Como los unicos valores posibles son 0 y 1, esa operacion alterna entre ambos.
El rival se obtiene con `jugadores[1 - indiceTurno]`. Se eligio un arreglo y no
una estructura dinamica porque la cantidad de participantes es fija y no cambia
durante la partida.

### Validaciones

Antes de cada accion se verifica que la partida siga `EN_CURSO`: una vez que
hay ganador no se permiten nuevas acciones. Ademas se valida en el constructor
que ambos jugadores existan, que sean objetos distintos, que tengan nombres
distintos y que el historial no sea `null`. Estas validaciones evitan estados
inconsistentes que serian dificiles de diagnosticar despues.

### Flujo entre M1 y M2

Una `Partida` representa siempre un enfrentamiento entre dos jugadores. Por eso
el modo contra las maquinas se modela como dos partidas consecutivas:

1. Jugador vs M1.
2. Durante esa partida, M2 observa las preguntas de M1.
3. Si el jugador pierde contra M1, termina el juego.
4. Si vence a M1, empieza automaticamente una partida contra M2.
5. El jugador mantiene el mismo personaje secreto (no puede cambiarlo, como
   pide el enunciado).
6. M2 conserva el conocimiento adquirido observando.
7. El record se registra solamente si el jugador vence a **las dos** maquinas.

### Complejidad

| Operacion | Complejidad |
|---|---|
| Obtener jugador actual / rival / cambiar turno | O(1) |
| `realizarPregunta()` con maquina | O(n) por el filtrado de candidatos |
| `realizarAdivinanza()` - comprobacion | O(1) |
| `realizarAdivinanza()` - descartar candidato de un `ArrayList` | O(n) en el peor caso |

El peor caso de las acciones principales del motor es **O(n)**, dominado por
las operaciones sobre la lista de candidatos.

---

## 10. B7 - Persistencia del marcador (DAO)

### Objetivo

Proveer un mecanismo de persistencia para almacenar y listar el historial
acumulado de victorias, cumpliendo el requisito de "un marcador record que
indique junto al nombre del usuario cuantas partidas gano".

### Clases

- **`RecordDAO`** (interfaz): define el contrato -`cargar`, `guardar`,
  `mostrar`, `registrarVictoria`, `listarOrdenado`-. El resto del juego depende
  de esta interfaz, nunca del archivo. Cambiar CSV por una base de datos seria
  escribir otra implementacion sin tocar el resto del proyecto: ese es el
  aporte del patron **DAO**.
- **`RecordJugador`**: encapsula un registro del marcador (nombre y partidas
  ganadas). Inmutable, solo getters y un `toString()` formateado.
- **`RecordArchivoDAO`**: implementacion sobre archivo CSV en `datos/records.csv`.

### Decisiones de implementacion

**Carga (`cargar`).** Usa `BufferedReader` sobre `FileReader`. Antes de
procesar verifica que el archivo exista: si no existe, la aplicacion continua
en un estado valido con el marcador vacio, en vez de fallar en el primer
arranque. Cada linea se parsea descartando espacios y lineas en blanco. Las
lineas corruptas se capturan con `try-catch` sobre `NumberFormatException` y se
ignoran, sin interrumpir la carga del resto: un archivo parcialmente dañado no
hace perder todos los records.

**Guardado (`guardar`).** Usa `FileWriter` y `PrintWriter`. Antes de escribir
invoca `archivo.getParentFile().mkdirs()` para crear el directorio `datos/` si
no existe.

**Registro (`registrarVictoria`).** Incrementa con
`records.getOrDefault(nombre, 0) + 1` y guarda inmediatamente. Persistir en el
momento evita perder victorias si el programa se cierra sin pasar por el menu
de salida.

**Listado ordenado (`listarOrdenado`).** Reutiliza `OrdenadorMergeSort` con un
`Comparator` por victorias descendente, en lugar de `Collections.sort`. Asi hay
un unico algoritmo de ordenamiento en todo el proyecto.

### Estructuras de datos

- `Map<String, Integer> records`: un `HashMap` que relaciona nombre de jugador
  con cantidad de victorias. Se eligio porque la operacion mas frecuente
  -registrar o consultar la victoria de un jugador- es un acceso por clave.
- `List<RecordJugador>`: lista temporal construida en `listarOrdenado()` y
  procesada por MergeSort.

### Complejidad

| Operacion | Complejidad | Motivo |
|---|---|---|
| Consultar o registrar una victoria | O(1) promedio | Acceso por clave en `HashMap` |
| Cargar / guardar en disco | O(r) | Recorre todos los registros |
| `listarOrdenado()` | O(r log r) | Dominado por MergeSort |

Sobre el O(1) del `HashMap`: es **promedio**, no garantizado. En el peor caso,
con muchas colisiones de hash, degrada a O(n) (O(log n) desde Java 8, que
convierte los buckets muy cargados en arboles). Con la cantidad de jugadores de
este juego la diferencia es irrelevante, pero conviene enunciarlo bien.

---

## 11. B8 - Modo espectador y capa de interfaz

### Modo espectador (`SimuladorMaquinaVsMaquina`)

Cumple el requisito del enunciado de "un modo de Maquina vs maquina en el cual
se puedan presenciar todos los procesos realizados por la misma". No alcanza
con mostrar el resultado: hay que hacer visible **por que** cada maquina elige
cada pregunta.

En cada turno la traza muestra:

- cuantos candidatos le quedan a la maquina que juega;
- la tabla de evaluacion de cada filtro disponible: cuantos responden SI,
  cuantos NO y cual seria el peor caso resultante;
- que pregunta eligio y con que peor caso, comparado con el mejor peor caso que
  habia disponible;
- cuantos candidatos descarto la respuesta.

Para la asertiva la tabla se titula "evalua cada corte y elige el de menor peor
caso". Para la conservadora se titula **"esta estrategia no la consulta"**, y
la linea de la pregunta elegida informa cuanto perdio, por ejemplo:

```
Pregunta elegida: ¿Tu personaje es de genero MASCULINO?
  [peor caso 16, pudiendo haber elegido uno de 12]
```

Esa comparacion hace medible la diferencia entre las dos estrategias dentro de
la propia partida, no solo en las simulaciones agregadas.

### Implementacion recursiva

El recorrido de turnos es **recursivo** a proposito, porque una partida es un
camino descendente por el arbol de decision.

**Casos base:**

1. `partida.getEstado() == EstadoPartida.FINALIZADA`: una maquina adivino.
   Imprime el ganador y el total de turnos.
2. `numeroTurno > LIMITE_TURNOS` (fijado en 60): cota de seguridad ante una
   estrategia que no converja.

**Paso recursivo (`simularTurnoRecursivo`):** identifica atacante y defensor,
procesa la jugada y se autoinvoca con el estado actualizado y el contador
incrementado.

**Reduccion, no division.** Cada llamada trabaja sobre un espacio de candidatos
estrictamente menor: el problema se **reduce** a un unico subproblema mas
chico. Es distinto de MergeSort, que **divide** el problema en dos subproblemas
que se resuelven por separado y despues se combinan. Es la diferencia entre
"reduccion" y "divide y venceras" propiamente dicho.

### Estructuras

- `List<Personaje> candidatos`: obtenida de `atacante.getCandidatos()`.
- `List<Filtro> disponibles`: copia de `CatalogoFiltros.listarTodos()` menos
  `maquina.getFiltrosUsados()`, recalculada en cada turno.

### Complejidad del modo espectador

| Aspecto | Complejidad | Aclaracion |
|---|---|---|
| Costo por turno | O(n · f) | Se evaluan los f filtros sobre los n candidatos para armar la tabla |
| Profundidad de la recursion - M1 | O(log n) | Cada pregunta parte el espacio por la mitad |
| Profundidad de la recursion - M2 | O(f), peor caso hacia O(n) | No garantiza corte por mitad |
| Costo total de una partida | O(n · f) | Ver abajo |

Sobre el costo total: los candidatos decrecen turno a turno, y en el caso de la
asertiva lo hacen aproximadamente a la mitad. La suma
`n + n/2 + n/4 + ... ≈ 2n` es una **serie geometrica**, por lo que el trabajo
acumulado sigue siendo O(n · f) y no O(n · f · log n).

Es importante ser preciso aca: la reduccion a la mitad **solo esta garantizada
para la estrategia asertiva**. La conservadora no evalua cortes, asi que su
decrecimiento puede ser mucho mas lento y su profundidad no es logaritmica.

### Capa de interfaz: consola y Swing

El proyecto ofrece dos formas de ejecucion que comparten **todo** el dominio:

- **`Main`** (consola) y **`MainSwing`** (aplicacion de ventana) son dos
  **Facade** equivalentes: arman los mismos subsistemas -repositorio,
  jugadores, estrategias, motor, persistencia- y solo cambian en como se lee y
  se muestra la informacion.
- **`ConsolaJuego`** e **`InterfazSwing`** son dos implementaciones de
  `InterfazHumano`. `JugadorHumano` no distingue si del otro lado hay una
  consola o una ventana: solo pide un `Filtro` o un `Personaje` y espera la
  respuesta.
- **`VentanaJuego`** es la ventana unica de la aplicacion: una bitacora del
  juego arriba y, abajo, la unica accion posible en cada momento.

Que la GUI se haya podido agregar sin modificar ni una linea del dominio es la
mejor evidencia de que la separacion de responsabilidades funciona. Fue
exactamente el objetivo de que `JugadorHumano` dependiera de `InterfazHumano` y
no de `Scanner`.

**Nota sobre concurrencia:** la ventana vive en el hilo de eventos de Swing
(EDT), mientras que el flujo de la partida corre en un hilo aparte para poder
bloquearse esperando cada click sin congelar la interfaz. La comunicacion entre
ambos usa una `BlockingQueue`.

### Detalle de implementacion: lectura por consola

`ConsolaJuego` lee siempre con `nextLine()` y parsea a mano, nunca con
`nextInt()`. Mezclar ambos sobre el mismo `Scanner` deja el salto de linea en
el buffer y hace que la siguiente lectura devuelva vacio: es un error clasico
de Java que rompia la partida en el primer turno. Ademas el `Scanner` se recibe
por constructor, porque dos `Scanner` distintos sobre `System.in` se roban los
datos bufferizados entre si.

La validacion de entrada se hace con un **bucle**, no con una llamada
recursiva. Una recursion ahi crece la pila con cada error de tipeo del usuario
y puede terminar en `StackOverflowError` (ver seccion 14).

---

## 12. Analisis de eficiencia: medicion empirica

`BancoDeSimulaciones` mide las estrategias aisladas del resto del juego: no usa
`Partida` ni jugadores, solo hace que una `Estrategia` intente identificar un
personaje secreto conocido y cuenta los turnos. Asi la metrica refleja la
calidad de la decision y no el orden de los turnos ni la suerte del sorteo
inicial. Cada estrategia se prueba contra **los 23 personajes** como secreto.

### Resultados

Cota teorica inferior: `techo(log2(23)) = 5` preguntas + 1 adivinanza = 6 turnos.

**A - Comparacion pura del criterio de eleccion** (ambas preguntan hasta aislar
un unico candidato, para medir solo la calidad del corte):

| Estrategia | Promedio | Min | Max | Fallos |
|---|---|---|---|---|
| Asertiva (greedy minimax) | **5.74** | 4 | 7 | 0 |
| Conservadora, guion del catalogo | 6.26 | 4 | 7 | 0 |
| Conservadora, guion invertido | 7.22 | 6 | 8 | 0 |

**B - Efecto de arriesgar antes de tiempo** (umbral 3):

| Estrategia | Promedio | Min | Max | Fallos |
|---|---|---|---|---|
| Conservadora, catalogo, umbral 3 | 5.67 | 4 | 7 | **816** de 920 |
| Conservadora, invertido, umbral 3 | 6.05 | 5 | 7 | **810** de 920 |

### Tres conclusiones

**1. La conservadora es fragil al orden; la asertiva no.** La diferencia entre
6.26 y 7.22 es la *misma estrategia* con distinto guion. La asertiva da 5.74 sin
importar como este ordenado el catalogo, porque lo evalua entero en cada turno.
El argumento no es "la asertiva es un poco mejor", sino que **la conservadora
depende de tener suerte con el orden y la asertiva no depende de nada.**

**2. El promedio puede mentir.** Con umbral 3, la conservadora promedia 5.67
turnos, mejor que la asertiva. Pero acumula **816 adivinanzas fallidas en 920
partidas**: en un juego real cada fallo le regala el turno al rival. La metrica
"turnos hasta acertar" premia al que tuvo suerte. Hay que mirar promedio **y**
tasa de fallo juntos.

**3. La cota log2(23) = 5 no se alcanza, y sabemos por que.** Esa cota supone
que en cada turno existe una pregunta que parte el conjunto exactamente al
medio. Con estos 7 filtros no existe: el mejor primer corte es barba (11 contra
12, peor caso 12) y de ahi en adelante empeora. Por eso el maximo medido es 7
turnos y no 6. La brecha entre la cota teorica y la medicion real se explica
por la distribucion concreta de atributos, no por un defecto del algoritmo.

### Comportamiento observado en partida real

En una ejecucion del modo espectador, M1 (asertiva) redujo su espacio a **un
unico candidato**, y M2 (conservadora) gano igual **arriesgando con 3
candidatos, es decir con 33% de probabilidad**. Es un buen ejemplo de que un
resultado puntual no valida un algoritmo: hay que mirar la distribucion sobre
muchas partidas, que es justamente lo que mide el banco.

---

## 13. Algoritmos considerados y NO aplicados

Esta seccion documenta alternativas evaluadas y descartadas, con el criterio de
la decision.

### QuickSort

**Por que no.** Dos razones:

1. **La entrada viene parcialmente ordenada.** Los personajes llegan agrupados
   por genero desde la fabrica, que es justamente el peor caso de QuickSort con
   pivote ingenuo (primer o ultimo elemento): las particiones quedan
   desbalanceadas y la complejidad degrada de O(n log n) a **O(n²)**.
2. **No es estable.** El intercambio de elementos no adyacentes puede alterar
   el orden relativo de elementos equivalentes, lo que rompia la
   reproducibilidad de la carga.

**Matiz honesto:** el primer problema se mitiga con pivote aleatorio o
mediana-de-tres, que son practica estandar. Con n = 23 la diferencia en
segundos es nula. La eleccion de MergeSort se sostiene sobre todo por la
estabilidad y por la garantia de O(n log n) en todos los casos.

### Bubble Sort

**Por que no.** O(n²) en promedio y en el peor caso, sin ninguna ventaja
compensatoria. Su unico punto fuerte es que con una bandera de corte temprano
detecta una lista ya ordenada en **O(n)**, mejor que MergeSort en ese caso
particular.

Ese caso no se da aca: la lista de la fabrica no esta ordenada por el criterio
final, y la lista del marcador cambia con cada victoria. Si hubiera que ordenar
repetidamente una coleccion muy chica y casi siempre ordenada, seria una opcion
razonable: **con n muy chico, la constante importa mas que el orden
asintotico.** No es el caso.

### Busqueda binaria recursiva

**Por que no.** Se implemento iterativa. La version recursiva es igual de
legible pero consume O(log n) de pila en vez de O(1). Con 23 elementos la
diferencia es despreciable, pero no habia ninguna razon para pagarla.

### Precomputar el arbol de decision completo

**Por que no.** Seria el optimo global: construir todo el arbol y elegir la
secuencia de preguntas que minimiza la profundidad. El costo es **exponencial**
en la cantidad de filtros, porque hay que explorar todas las ordenaciones
posibles.

Es el **trade-off espacio/tiempo** clasico: precomputar el arbol daria
decisiones instantaneas a costa de memoria y de un calculo inicial caro;
recalcular la mejor pregunta en cada turno cuesta O(n · f) pero no ocupa
memoria. Con n = 23 y f = 7 el recalculo son ~161 operaciones por turno, asi
que se eligio recalcular. La decision se invertiria si el universo de
personajes fuera mucho mas grande y las partidas muy frecuentes.

### Command para las acciones del turno

**Por que no.** Se evaluo modelar preguntas y adivinanzas como objetos Command
para obtener el historial "gratis". Se descarto porque el historial que
necesita el Observer se resuelve con una `List<ResultadoPregunta>`, y la
jerarquia de comandos habria agregado clases sin resolver ningun requisito
nuevo. Se prefirieron pocos patrones bien justificados antes que muchos
decorativos.

---

## 14. Recursividad: riesgos y como los evitamos

El proyecto usa recursion en dos lugares: `OrdenadorMergeSort` y
`SimuladorMaquinaVsMaquina`. Los cuatro riesgos clasicos y como se tratan:

### 1. Caso base ausente o inalcanzable

- **MergeSort:** caso base `elementos.size() <= 1`. Se alcanza siempre porque
  cada llamada recibe una sublista estrictamente mas chica.
- **Simulador:** dos casos base -partida finalizada y `numeroTurno > 60`-. El
  segundo es una red de seguridad: si una estrategia mal implementada nunca
  convergiera, el primero no se alcanzaria nunca.

### 2. Reduccion insuficiente del problema

Cada llamada debe acercarse al caso base. MergeSort parte la lista al medio
(reduccion garantizada). El simulador trabaja sobre un espacio de candidatos
que se achica turno a turno.

**Contraejemplo real del proyecto:** una version previa de `ConsolaJuego`
validaba la entrada del usuario llamandose a si misma cuando el ID era
invalido. **No habia reduccion del problema**: cada error de tipeo agregaba un
marco de pila sin acercarse a ningun caso base. Se reemplazo por un bucle
`while`. Es el ejemplo de manual de recursion mal usada: cuando no hay
reduccion, corresponde una iteracion.

### 3. Explosion de llamadas

Una recursion puede repetir el mismo subproblema muchas veces. El caso canonico
es Fibonacci ingenuo:

```java
fib(n) = fib(n-1) + fib(n-2)
```

Su arbol de llamadas tiene ramificacion 2 y profundidad n, de modo que el costo
es **O(2ⁿ)**: `fib(50)` es inviable. Se corrige de dos formas:

- **Memoizacion:** guardar los resultados ya calculados en un `Map` o arreglo,
  y devolverlos en vez de recalcular. Baja a **O(n)** tiempo y O(n) espacio.
- **Version iterativa:** un bucle con dos variables. **O(n)** tiempo y **O(1)**
  espacio.

En este proyecto no aparece el problema: MergeSort genera subproblemas
**disjuntos** (cada elemento pertenece a una sola mitad), asi que nunca resuelve
el mismo dos veces, y el simulador hace una sola llamada por turno. No hace
falta memoizar.

### 4. StackOverflowError

Cada llamada ocupa un marco de pila. La pila de la JVM soporta del orden de
miles de marcos.

- MergeSort con n = 23: profundidad `log2(23) ≈ 5`.
- Simulador: profundidad acotada en 60 por construccion.

Ninguno se acerca al limite. El riesgo real era el bucle de validacion de
entrada mencionado arriba, que dependia de cuantas veces se equivocara el
usuario: potencialmente ilimitado.

### Divide y venceras vs. reduccion

Dos formas distintas de recursion que conviene no confundir:

| | **Divide y venceras** | **Reduccion** |
|---|---|---|
| Subproblemas por llamada | Dos o mas | Uno |
| Hay paso de combinacion | Si (`merge`) | No |
| Ejemplo en el proyecto | `OrdenadorMergeSort` | `SimuladorMaquinaVsMaquina` |
| Otro ejemplo | QuickSort | Busqueda binaria, factorial, palindromo |

La busqueda binaria se suele clasificar como divide y venceras, pero
estrictamente es **reduccion**: parte el rango en dos y descarta una mitad, de
modo que solo continua con un subproblema y no combina nada. Es la razon de que
sea O(log n) y no O(n log n).

---

## 15. Bitacora de decisiones

### Modelo: el quinto atributo

Al armar los 23 personajes se detecto que el espacio de atributos del enunciado
solo ofrece 16 combinaciones. Se agrego `barba` para llegar a 32 y se incorporo
una validacion en la fabrica que lanza excepcion ante cualquier duplicado, de
modo que el problema no pueda reaparecer silenciosamente.

### Motor: de varias clases a dos metodos

La primera version representaba las acciones del turno con varias clases
especificas para preguntas, adivinanzas y resultados. Al implementarlo se vio
que agregaba clases sin resolver nada: el juego solo tiene dos acciones
posibles. Se simplifico a `realizarPregunta(Filtro)` y
`realizarAdivinanza(Personaje)`.

### Estrategias: el segundo eje de decision

La interfaz `Estrategia` original solo cubria **que** preguntar. Al integrarla
con el simulador se vio que la condicion para arriesgar estaba hardcodeada en
el motor (`if candidatos.size() == 1`), lo que pisaba la decision de la
estrategia e impedia que la conservadora fuera realmente impaciente. Se agrego
`debeArriesgar()` como metodo `default`, de modo que las dos maquinas se
diferencien tambien en **cuando** dejan de preguntar sin romper codigo
existente.

### Flujo M1 a M2

Tras una aclaracion del profesor se corrigio la interpretacion del modo Jugador
vs Maquina. Se definio que una `Partida` siempre enfrenta a dos jugadores, y
que el humano primero juega contra M1 y solo si la vence enfrenta a M2,
reutilizando las mismas instancias para conservar el personaje secreto y el
conocimiento observado. El record pasó a registrarse solo despues de vencer a
ambas. El mismo flujo se aplico a `Main` y a `MainSwing`.

### Persistencia: unificar el ordenamiento

`listarOrdenado()` iba a usar `Collections.sort`. Se cambio a
`OrdenadorMergeSort` para que exista un unico algoritmo de ordenamiento en el
proyecto y no haya que justificar dos.

### Interfaz: de consola a Swing sin tocar el dominio

La GUI se agrego despues de tener el juego funcionando por consola. No requirio
modificar ninguna clase de `modelo`, `filtros`, `jugadores`, `motor`,
`repositorio` ni `persistencia`: alcanzo con una segunda implementacion de
`InterfazHumano` y un segundo Facade. Fue la verificacion practica de que la
separacion de responsabilidades estaba bien planteada.

---

## 16. Limitaciones conocidas

Registradas honestamente, con su impacto:

**Distribucion de personajes desbalanceada.** La fabrica toma los primeros 23
de la enumeracion sistematica, lo que produce 16 personajes masculinos y 7
femeninos. Consecuencia: `FiltroGenero` parte 16/7, muy lejos de la mitad, y es
casi siempre la peor pregunta disponible. Un reparto cercano a 12/11 mejoraria
el juego. Efecto secundario del mismo corte: quedan 3 personajes femeninos con
barba, coherentes con el modelo pero poco naturales.

**`getPersonajeSecreto()` es publico.** El diseño garantiza que la maquina no
puede espiar porque la `Estrategia` nunca recibe el personaje del rival, pero
el getter existe en `Jugador` y lo usa el Facade para mostrar el personaje al
terminar la partida. Deberia ser `protected` o de paquete, con un metodo
especifico para revelarlo una vez finalizada la partida.

**`JugadorHumano.elegirPregunta()` ofrece el catalogo completo**, incluidas
preguntas ya realizadas. No es un error -el enunciado permite preguntas
infinitas- pero permite al jugador gastar turnos sin obtener informacion nueva.
Las maquinas si llevan registro de filtros usados.

**La conservadora no detecta filtros inutiles.** Si el filtro que le toca por
guion no descarta a nadie en ese momento, igual lo usa y pierde el turno. Es
intencional -forma parte de lo que la hace menos asertiva- y la traza del modo
espectador lo señala explicitamente con "(no descarta a nadie)".
