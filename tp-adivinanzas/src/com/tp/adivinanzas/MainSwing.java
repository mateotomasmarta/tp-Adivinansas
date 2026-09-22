package com.tp.adivinanzas;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.Jugador;
import com.tp.adivinanzas.jugadores.JugadorHumano;
import com.tp.adivinanzas.jugadores.JugadorMaquina;
import com.tp.adivinanzas.jugadores.estrategias.Estrategia;
import com.tp.adivinanzas.jugadores.estrategias.EstrategiaAsertiva;
import com.tp.adivinanzas.jugadores.estrategias.EstrategiaConservadora;
import com.tp.adivinanzas.modelo.Personaje;
import com.tp.adivinanzas.motor.EstadoPartida;
import com.tp.adivinanzas.motor.HistorialPreguntas;
import com.tp.adivinanzas.motor.ObservadorMaquina;
import com.tp.adivinanzas.motor.Partida;
import com.tp.adivinanzas.motor.ResultadoPregunta;
import com.tp.adivinanzas.motor.SimuladorMaquinaVsMaquina;
import com.tp.adivinanzas.persistencia.RecordArchivoDAO;
import com.tp.adivinanzas.persistencia.RecordDAO;
import com.tp.adivinanzas.repositorio.RepositorioPersonajes;

import javax.swing.SwingUtilities;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Version app (Swing) del mismo Facade que Main (consola): arma los mismos
 * subsistemas (repositorio, jugadores, estrategias, motor, persistencia) y
 * solo cambia como se lee/muestra la informacion. La ventana vive en el hilo
 * de Swing (EDT); este flujo -identico en estructura a Main.main()- corre en
 * un hilo aparte para poder bloquearse esperando cada click sin congelar la
 * interfaz.
 */
public class MainSwing {
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        VentanaJuego ventana = new VentanaJuego();
        SwingUtilities.invokeLater(() -> ventana.setVisible(true));

        Thread hiloJuego = new Thread(() -> ejecutar(ventana), "hilo-juego");
        hiloJuego.setDaemon(true);
        hiloJuego.start();
    }

    private static void ejecutar(VentanaJuego ventana) {
        RepositorioPersonajes repositorio = RepositorioPersonajes.getInstancia();
        List<Personaje> personajes = repositorio.listarTodos();

        RecordDAO recordDAO = new RecordArchivoDAO();
        recordDAO.cargar();

        ventana.log("=== ADIVINA QUIEN ===");
        String nombreJugador = ventana.pedirTexto("Ingresa tu nombre de jugador:", "Jugador 1").trim();
        if (nombreJugador.isEmpty()) {
            nombreJugador = "Jugador 1";
        }
        ventana.log("Usuario actual: " + nombreJugador);

        InterfazSwing interfaz = new InterfazSwing(ventana);
        boolean salir = false;

        while (!salir) {
            ventana.log("\n--- MENU PRINCIPAL ---");
            String opcion = ventana.pedirEleccion(List.of(
                    "Jugar contra las maquinas",
                    "Simulacion maquina vs maquina",
                    "Ver marcador de records",
                    "Salir"), s -> s);

            switch (opcion) {
                case "Jugar contra las maquinas":
                    jugarHumanoVsMaquinas(nombreJugador, ventana, interfaz, personajes, recordDAO);
                    break;
                case "Simulacion maquina vs maquina":
                    simularMaquinaVsMaquina(ventana, personajes);
                    break;
                case "Ver marcador de records":
                    mostrarRecords(ventana, recordDAO);
                    break;
                default:
                    salir = true;
                    ventana.log("\nGracias por jugar.");
                    ventana.limpiarAccion();
                    break;
            }
        }
    }

    private static void jugarHumanoVsMaquinas(String nombreJugador, VentanaJuego ventana, InterfazSwing interfaz,
                                              List<Personaje> personajes, RecordDAO recordDAO) {
        ventana.log("\nElegi tu personaje secreto. No lo vas a poder cambiar durante la partida.");
        Personaje secretoHumano = interfaz.elegirPersonaje(nombreJugador, personajes);
        ventana.log("Elegiste: " + secretoHumano.getNombre());

        List<Personaje> secretos = elegirDistintos(personajes, 2, secretoHumano);

        Estrategia estrategiaM1 = new EstrategiaAsertiva();
        Estrategia estrategiaM2 = new EstrategiaConservadora();

        JugadorHumano humano = new JugadorHumano(nombreJugador, secretoHumano, interfaz);
        JugadorMaquina maquina1 = new JugadorMaquina("Maquina 1", secretos.get(0), estrategiaM1, personajes);
        JugadorMaquina maquina2 = new JugadorMaquina("Maquina 2", secretos.get(1), estrategiaM2, personajes);

        HistorialPreguntas historial = new HistorialPreguntas();
        historial.agregarObservador(new ObservadorMaquina(maquina2, "Maquina 1", nombreJugador));

        Partida partida = new Partida(humano, maquina1, historial);

        // Candidatos de arriesgar del humano: arranca con todos y se va
        // filtrando con cada respuesta que le da Maquina 1, para no obligar
        // a la persona a llevar la cuenta de memoria como en la consola.
        List<Personaje> candidatosHumano = new ArrayList<Personaje>(personajes);

        ventana.log("\nEmpieza la partida. Tu objetivo: adivinar el personaje de Maquina 1.");
        ventana.log("Maquina 2 escucha las preguntas de Maquina 1 (ventaja informativa).");

        while (partida.getEstado() == EstadoPartida.EN_CURSO) {
            Jugador actual = partida.getJugadorActual();

            if (actual == humano) {
                turnoHumano(partida, humano, ventana, interfaz, candidatosHumano);
            } else {
                turnoMaquina(partida, maquina1, estrategiaM1, ventana);
            }
        }

        ventana.log("\nGano: " + partida.getGanador().getNombre()
                + " en " + partida.getTurnosJugados() + " turnos.");
        ventana.log("Candidatos que le quedaban a Maquina 2 escuchando: "
                + maquina2.getCandidatos().size());

        if (partida.getGanador() == humano) {
            recordDAO.registrarVictoria(nombreJugador);
            ventana.log("Victoria registrada en el marcador.");
        } else {
            ventana.log("El personaje de Maquina 1 era: "
                    + maquina1.getPersonajeSecreto().getNombre());
        }

        ventana.esperarContinuar("Volver al menu");
    }

    private static void turnoHumano(Partida partida, JugadorHumano humano, VentanaJuego ventana,
                                    InterfazSwing interfaz, List<Personaje> candidatosHumano) {
        ventana.log("\n--- Tu turno ---");
        ventana.log("Te quedan " + candidatosHumano.size() + " personajes posibles para arriesgar.");
        String opcion = ventana.pedirEleccion(
                List.of("Hacer una pregunta", "Arriesgar el personaje"), s -> s);

        if (opcion.equals("Arriesgar el personaje")) {
            Personaje intento = interfaz.elegirPersonajeOVolver("Tu apuesta", candidatosHumano);
            if (intento == null) {
                ventana.log("Volviste atras sin arriesgar.");
                return;
            }
            boolean acierto = partida.realizarAdivinanza(intento);
            ventana.log(acierto ? "Acertaste!" : "No era ese personaje.");
            if (!acierto) {
                candidatosHumano.remove(intento);
            }
        } else {
            Filtro filtro = humano.elegirPregunta();
            ResultadoPregunta resultado = partida.realizarPregunta(filtro);
            ventana.log("Respuesta: " + (resultado.isRespuestaAfirmativa() ? "SI" : "NO"));
            candidatosHumano.removeIf(p -> filtro.cumple(p) != resultado.isRespuestaAfirmativa());
            ventana.log("Te quedan " + candidatosHumano.size() + " personajes posibles.");
        }
    }

    private static void turnoMaquina(Partida partida, JugadorMaquina maquina, Estrategia estrategia,
                                     VentanaJuego ventana) {
        ventana.log("\n--- Turno de " + maquina.getNombre() + " ---");
        ventana.log("Le quedan " + maquina.getCandidatos().size() + " candidatos posibles.");

        if (estrategia.debeArriesgar(maquina.getCandidatos())) {
            Personaje intento = maquina.arriesgarPersonaje();
            ventana.log(maquina.getNombre() + " arriesga: tu personaje es " + intento.getNombre() + "?");
            boolean acierto = partida.realizarAdivinanza(intento);
            ventana.log(acierto ? "Adivino." : "Fallo, pierde el turno.");
        } else {
            Filtro filtro = maquina.elegirPregunta();
            ventana.log(maquina.getNombre() + " pregunta: " + filtro.getDescripcion());
            ResultadoPregunta resultado = partida.realizarPregunta(filtro);
            ventana.log("Respuesta: " + (resultado.isRespuestaAfirmativa() ? "SI" : "NO"));
        }
    }

    private static void simularMaquinaVsMaquina(VentanaJuego ventana, List<Personaje> personajes) {
        List<Personaje> secretos = elegirDistintos(personajes, 2, null);

        Estrategia estrategiaM1 = new EstrategiaAsertiva();
        Estrategia estrategiaM2 = new EstrategiaConservadora();

        JugadorMaquina maquina1 = new JugadorMaquina("Maquina 1", secretos.get(0), estrategiaM1, personajes);
        JugadorMaquina maquina2 = new JugadorMaquina("Maquina 2", secretos.get(1), estrategiaM2, personajes);

        ventana.log("\nPersonaje secreto de Maquina 1: " + secretos.get(0).getNombre());
        ventana.log("Personaje secreto de Maquina 2: " + secretos.get(1).getNombre());

        String salida = capturarSalida(() ->
                new SimuladorMaquinaVsMaquina(estrategiaM1, estrategiaM2).simular(maquina1, maquina2));
        ventana.log(salida);

        ventana.esperarContinuar("Volver al menu");
    }

    private static void mostrarRecords(VentanaJuego ventana, RecordDAO recordDAO) {
        recordDAO.cargar();
        ventana.log("\n--- MARCADOR ---");
        String salida = capturarSalida(recordDAO::mostrar);
        ventana.log(salida);
        ventana.esperarContinuar("Volver al menu");
    }

    /**
     * SimuladorMaquinaVsMaquina y RecordDAO ya saben imprimir su propio
     * relato (println): en vez de reescribir esa logica para la ventana, se
     * captura la salida y se vuelca a la bitacora. UTF-8 explicito en ambos
     * extremos para no depender del code page de la consola.
     */
    private static String capturarSalida(Runnable accion) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            accion.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static List<Personaje> elegirDistintos(List<Personaje> personajes, int cantidad, Personaje excluido) {
        List<Personaje> disponibles = new ArrayList<Personaje>(personajes);
        if (excluido != null) {
            disponibles.remove(excluido);
        }

        List<Personaje> elegidos = new ArrayList<Personaje>();
        for (int i = 0; i < cantidad; i++) {
            elegidos.add(disponibles.remove(RANDOM.nextInt(disponibles.size())));
        }
        return elegidos;
    }
}
