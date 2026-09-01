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
