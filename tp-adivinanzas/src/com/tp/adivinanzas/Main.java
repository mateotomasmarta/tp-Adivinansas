package com.tp.adivinanzas;

import com.tp.adivinanzas.jugadores.JugadorMaquina;
import com.tp.adivinanzas.jugadores.JugadorHumano;
import com.tp.adivinanzas.modelo.Personaje;
import com.tp.adivinanzas.modelo.Partida;
import com.tp.adivinanzas.motor.SimuladorMaquinavsMaquina;
import com.tp.adivinanzas.repositorio.RepositorioPersonajes;
import com.tp.adivinanzas.persistencia.RecordArchivoDAO;

import java.util.Scanner;
import java.util.Random;
import java.util.List;

public class Main{
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        RepositorioPersonajes repositorio = RepositorioPersonajes.getInstancia();
        List<Personaje> personajes = repositorio.listarTodos();
        RecordArchivoDAO recordDAO = new RecordArchivoDAO();

        System.out.println("Bienvenido al juego ADIVINA QUIÉN");
        
        System.out.println("Ingrese el nombre de jugador: ");
        String nombreJugador = scanner.nextLine().trim();
        if (nombreJugador.isEmpty()){
            nombreJugador = "Jugador 1";
        }

        ConsolaJuego consola = new ConsolaJuego();

        boolean salir = false;
        while(!salir){
            System.out.println("MENÚ PRINCIPAL");
            System.out.println("Usuario actual: " + nombreJugador);
            System.out.println("1. Modo humano vs máquina");
            System.out.println("2. Simulación máquina vs máquina");
            System.out.println("3. Ver historial de records");
            System.out.println("4. Salir");
            System.out.println("Elige una opción");

            int opcion = pedirOpcionValida(scanner, 1, 4);

            switch(opcion) {
                case 1:
                    ejecutarHumanovsMaquina(nombreJugador, consola, personajes, recordDAO);
                    break;
                case 2:
                    ejecutarSimulacionMaquinavsMaquina(personajes);
                    break;
                case 3:
                    mostrarRecords(recordDAO);
                    break;
                case 4:
                    salir = true;
                    System.out.println("Gracias por jugar");
                    break;
            }
        }
    }

    public static void ejecutarHumanovsMaquina(String nombreJugador, ConsolaJuego consola, List<Perosnaje> personajes, RecordArchivoDAO recordDAO){
        //hacer esta funcion    
    }

    public static void ejecutarSimulacionMaquinavsMaquina(List<Personaje> personajes){
        JugadorMaquina m1 = new JugadorMaquina("Maquina 1");
        JugadorMaquina m2 = new JugadorMaquina("Maquina 2");

        asignarPersonajesDistintos(m1,m2, personajes);

        SimuladorMaquinavsMaquina simulador = new SimuladorMaquinavsMaquina();
        simulador.simular(m1, m2);
    }

    private static void mostrarRecords(RecordArchivoDAO recordDAO){
        System.out.println("Historial de records");
        recordDAO.cargar();
        recordDAO.mostrar();
    }

    private static void asignarPersonajesDistintos(JugadorMaquina m1, JugadorMaquina m2, List<Personaje> personajes){
        Random random = new Random();
        Personaje p1 = personajes.get(random.nextInt(personajes.size()));
        Personaje p2;

        do{
            p2 = personajes.get(random.nextInt(personajes.size()));
        } while( p1.getId() == p2.getId());

        m1.setPersonajeSecreto(p1);
        m2.setPersonajeSecreto(p2);

        System.out.println(m1.getNombre() + " asignó su personaje secreto" + p1.getNombre());
        System.out.println(m2.getNombre() + " asignó su personaje secreto" + p2.getNombre());
    }

    private static int pedirOpcionValida(Scanner scanner, int min, int max) {
        while (true) {
            try {
                int opcion = Integer.parseInt(scanner.nextLine().trim());
                if (opcion >= min && opcion <= max) {
                    return opcion;
                }
            } catch (NumberFormatException e) {
            // atrapa el error y pasa directamente a mostrar el mensaje de reintento.
            }

            System.out.print("Opción inválida. Reintentá (" + min + "-" + max + "): ");
        }
    }
}


