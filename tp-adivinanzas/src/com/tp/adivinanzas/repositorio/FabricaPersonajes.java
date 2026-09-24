package com.tp.adivinanzas.repositorio;

import com.tp.adivinanzas.modelo.ColorPelo;
import com.tp.adivinanzas.modelo.Genero;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.datafaker.Faker;

/**
 * Fabrica de personajes iniciales.
 *
 * Genera automaticamente las combinaciones de atributos necesarias y usa
 * Datafaker solo para los nombres. Los IDs definitivos no se asignan aca:
 * se dejan en 0 y luego los completa el repositorio.
 */
public final class FabricaPersonajes {
    private static final int CANTIDAD_PERSONAJES = 23;

    private final Faker faker;

    public FabricaPersonajes() {
        this.faker = new Faker(Locale.forLanguageTag("es-AR"));
    }

    /**
     * Crea la lista de personajes recorriendo combinaciones posibles de genero,
     * calvicie, lentes, color de pelo y barba hasta llegar a 23.
     *
     * Costo: O(n), donde n es la cantidad de personajes a crear. La validacion
     * final tambien es O(n).
     */
    public List<Personaje> crearPersonajes() {
        List<Personaje> personajes = new ArrayList<Personaje>();
        Set<String> nombresUsados = new HashSet<String>();

        for (Genero genero : Genero.values()) {
            for (boolean calvo : new boolean[] {false, true}) {
                for (boolean lentes : new boolean[] {false, true}) {
                    for (ColorPelo colorPelo : coloresValidos(calvo)) {
                        for (boolean barba : new boolean[] {false, true}) {
                            if (personajes.size() == CANTIDAD_PERSONAJES) {
                                validarPersonajes(personajes);
                                return personajes;
                            }

                            personajes.add(crear(
                                    generarNombre(genero, nombresUsados),
                                    genero,
                                    calvo,
                                    lentes,
                                    colorPelo,
                                    barba));
                        }
                    }
                }
            }
        }

        validarPersonajes(personajes);
        return personajes;
    }

    /**
     * Devuelve los colores validos segun si el personaje es calvo.
     *
     * Costo: O(1).
     */
    private ColorPelo[] coloresValidos(boolean calvo) {
        if (calvo) {
            return new ColorPelo[] {ColorPelo.NINGUNO};
        }

        return new ColorPelo[] {ColorPelo.COLORADO, ColorPelo.NEGRO, ColorPelo.AMARILLO};
    }

    /**
     * Genera un nombre unico usando Datafaker y un HashSet para detectar
     * repetidos.
     *
     * Costo esperado: O(1) por intento, porque HashSet.add es O(1) promedio.
     */
    private String generarNombre(Genero genero, Set<String> nombresUsados) {
        String nombre;
        do {
            if (genero == Genero.MASCULINO) {
                nombre = faker.name().maleFirstName();
            } else {
                nombre = faker.name().femaleFirstName();
            }
        } while (!nombresUsados.add(nombre));

        return nombre;
    }

    /**
     * Crea el personaje con id provisorio 0. El id real se asigna despues en
     * RepositorioPersonajes.
     *
     * Costo: O(1).
     */
    private Personaje crear(String nombre, Genero genero, boolean calvo, boolean lentes, ColorPelo colorPelo, boolean barba) {
        return new Personaje(0, nombre, genero, calvo, lentes, colorPelo, barba);
    }

    /**
     * Verifica que haya exactamente 23 personajes y que no existan dos con la
     * misma combinacion de atributos.
     *
     * Costo: O(n), usando HashSet para detectar duplicados en O(1) promedio.
     */
    private void validarPersonajes(List<Personaje> personajes) {
        if (personajes.size() != CANTIDAD_PERSONAJES) {
            throw new IllegalStateException("La fabrica debe crear exactamente " + CANTIDAD_PERSONAJES + " personajes.");
        }

        Set<String> combinaciones = new HashSet<String>();
        for (Personaje personaje : personajes) {
            String clave = personaje.getGenero() + "|"
                    + personaje.isCalvo() + "|"
                    + personaje.tieneLentes() + "|"
                    + personaje.getColorPelo() + "|"
                    + personaje.tieneBarba();

            if (!combinaciones.add(clave)) {
                throw new IllegalStateException("Combinacion de atributos duplicada: " + clave);
            }
        }
    }
}
