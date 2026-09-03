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

public final class FabricaPersonajes {
    private static final int CANTIDAD_PERSONAJES = 23;

    private final Faker faker;

    public FabricaPersonajes() {
        this.faker = new Faker(Locale.forLanguageTag("es-AR"));
    }

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

    private ColorPelo[] coloresValidos(boolean calvo) {
        if (calvo) {
            return new ColorPelo[] {ColorPelo.NINGUNO};
        }

        return new ColorPelo[] {ColorPelo.COLORADO, ColorPelo.NEGRO, ColorPelo.AMARILLO};
    }

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

    private Personaje crear(String nombre, Genero genero, boolean calvo, boolean lentes, ColorPelo colorPelo, boolean barba) {
        return new Personaje(0, nombre, genero, calvo, lentes, colorPelo, barba);
    }

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
