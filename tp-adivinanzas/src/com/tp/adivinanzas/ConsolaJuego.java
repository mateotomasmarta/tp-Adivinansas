package com.tp.adivinanzas;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.InterfazHumano;
import com.tp.adivinanzas.modelo.Personaje;
import com.tp.adivinanzas.motor.ResultadoPregunta;

import java.util.List;
import java.util.Scanner;

/**
 * Implementacion de consola de InterfazHumano (B8).
 *
 * Toda la lectura pasa por nextLine() y se parsea a mano. Mezclar nextInt()
 * con nextLine() sobre el mismo Scanner deja el salto de linea en el buffer y
 * hace que la siguiente lectura devuelva vacio; por eso aca se usa un unico
 * criterio. Ademas el Scanner se recibe por constructor: dos Scanner distintos
 * sobre System.in se roban los datos bufferizados entre si.
 */
public class ConsolaJuego implements InterfazHumano {
    private final Scanner scanner;

    public ConsolaJuego(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Filtro elegirFiltro(String nombreJugador, List<Filtro> disponibles) {
        System.out.println();
        System.out.println(nombreJugador + ", elegi una pregunta:");

        for (int i = 0; i < disponibles.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + disponibles.get(i).getDescripcion());
        }

        int opcion = pedirEntero(1, disponibles.size());
        return disponibles.get(opcion - 1);
    }

    @Override
    public Personaje elegirPersonaje(String nombreJugador, List<Personaje> disponibles) {
        System.out.println();
        System.out.println(nombreJugador + ", elegi un personaje por ID:");

        for (Personaje personaje : disponibles) {
            System.out.println("  " + personaje.getId() + " - " + personaje.getNombre()
                    + " (" + personaje.getGenero()
                    + (personaje.isCalvo() ? ", calvo" : ", pelo " + personaje.getColorPelo())
                    + (personaje.tieneLentes() ? ", lentes" : "")
                    + (personaje.tieneBarba() ? ", barba" : "") + ")");
        }

        while (true) {
            int id = pedirEntero(1, Integer.MAX_VALUE);

            for (Personaje personaje : disponibles) {
                if (personaje.getId() == id) {
                    return personaje;
                }
            }
            System.out.println("Ese ID no esta entre los disponibles.");
        }
    }

    public void mostrarResultadoTurno(ResultadoPregunta resultado) {
        System.out.println("Respuesta: " + (resultado.isRespuestaAfirmativa() ? "SI" : "NO"));
    }

    /**
     * Lectura robusta: itera hasta obtener un entero en rango. Es un bucle y
     * no una llamada recursiva a proposito -una recursion aca crece la pila con
     * cada error de tipeo del usuario y puede terminar en StackOverflowError-.
     */
    public int pedirEntero(int min, int max) {
        while (true) {
            if (max == Integer.MAX_VALUE) {
                System.out.print("Ingresa el ID: ");
            } else {
                System.out.print("Opcion (" + min + "-" + max + "): ");
            }

            if (!scanner.hasNextLine()) {
                throw new IllegalStateException("No hay mas entrada disponible.");
            }

            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // Entrada no numerica: se reintenta.
            }
            System.out.println("Entrada invalida.");
        }
    }
}
