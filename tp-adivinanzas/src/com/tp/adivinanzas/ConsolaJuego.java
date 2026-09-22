package com.tp.adivinanzas;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.InterfazHumano;
import com.tp.adivinanzas.modelo.Personaje;
import com.tp.adivinanzas.motor.Partida;
import com.tp.adivinanzas.motor.ResultadoPregunta;

import java.util.List;
import java.util.Scanner;

public class ConsolaJuego implements InterfazHumano {
    private final Scanner scanner;

    public ConsolaJuego() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public Filtro elegirFiltro(String nombreJugador, List<Filtro> disponibles) {
        System.out.println("\n" + nombreJugador + " elegí una pregunta:");

        for (int i = 0; i < disponibles.size(); i++) {
            System.out.println((i + 1) + ". " + disponibles.get(i).getDescripcion());
        }

        int opcion = pedirOpcionValida(1, disponibles.size());
        return disponibles.get(opcion - 1);
    }

    @Override
    public Personaje elegirPersonaje(String nombreJugador, List<Personaje> disponibles) {
        System.out.println("\n" + nombreJugador + " elegí tu personaje secreto:");

        for (Personaje personaje : disponibles) {
            System.out.println("ID: " + personaje.getId() + " - " + personaje.getNombre());
        }

        while (true) {
            System.out.print("Ingresá el ID: ");
            int id = scanner.nextInt();

            for (Personaje personaje : disponibles) {
                if (personaje.getId() == id) {
                    return personaje;
                }
            }
            System.out.println("ID inválido. Elegí nuevamente.");
        }
    }

    // Método para mostrar el turno cuando una persona interactúa en Partida
    public void mostrarResultadoTurno(ResultadoPregunta resultado) {
        System.out.println("Respuesta: " + (resultado.isRespuestaAfirmativa() ? "SÍ" : "NO"));
    }

    private int pedirOpcionValida(int min, int max) {
        System.out.print("Elegí una opción (" + min + "-" + max + "): ");
        int opcion = scanner.nextInt();
        while (opcion < min || opcion > max) {
            System.out.print("Opción inválida. Reintentá (" + min + "-" + max + "): ");
            opcion = scanner.nextInt();
        }
        return opcion;
    }
}