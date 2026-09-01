package com.tp.adivinanzas.repositorio;

import com.tp.adivinanzas.modelo.ColorPelo;
import com.tp.adivinanzas.modelo.Genero;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FabricaPersonajes {
    private static final int CANTIDAD_PERSONAJES = 23;

    public List<Personaje> crearPersonajes() {
        List<Personaje> personajes = new ArrayList<Personaje>();

        personajes.add(crear("Pedro", Genero.MASCULINO, false, true, ColorPelo.NEGRO, true));
        personajes.add(crear("Ana", Genero.FEMENINO, false, false, ColorPelo.AMARILLO, false));
        personajes.add(crear("Luis", Genero.MASCULINO, true, false, ColorPelo.NINGUNO, false));
        personajes.add(crear("Maria", Genero.FEMENINO, false, true, ColorPelo.COLORADO, false));
        personajes.add(crear("Carlos", Genero.MASCULINO, false, false, ColorPelo.AMARILLO, true));
        personajes.add(crear("Sofia", Genero.FEMENINO, true, true, ColorPelo.NINGUNO, false));
        personajes.add(crear("Jorge", Genero.MASCULINO, false, true, ColorPelo.COLORADO, false));
        personajes.add(crear("Lucia", Genero.FEMENINO, false, false, ColorPelo.NEGRO, false));
        personajes.add(crear("Diego", Genero.MASCULINO, true, true, ColorPelo.NINGUNO, true));
        personajes.add(crear("Valeria", Genero.FEMENINO, false, true, ColorPelo.AMARILLO, true));
        personajes.add(crear("Martin", Genero.MASCULINO, false, false, ColorPelo.NEGRO, false));
        personajes.add(crear("Clara", Genero.FEMENINO, true, false, ColorPelo.NINGUNO, true));
        personajes.add(crear("Tomas", Genero.MASCULINO, false, false, ColorPelo.COLORADO, true));
        personajes.add(crear("Elena", Genero.FEMENINO, false, true, ColorPelo.NEGRO, true));
        personajes.add(crear("Ricardo", Genero.MASCULINO, true, true, ColorPelo.NINGUNO, false));
        personajes.add(crear("Paula", Genero.FEMENINO, false, false, ColorPelo.COLORADO, true));
        personajes.add(crear("Federico", Genero.MASCULINO, false, true, ColorPelo.AMARILLO, false));
        personajes.add(crear("Camila", Genero.FEMENINO, true, true, ColorPelo.NINGUNO, true));
        personajes.add(crear("Nicolas", Genero.MASCULINO, false, false, ColorPelo.NEGRO, true));
        personajes.add(crear("Rocio", Genero.FEMENINO, false, true, ColorPelo.AMARILLO, false));
        personajes.add(crear("Hector", Genero.MASCULINO, true, false, ColorPelo.NINGUNO, true));
        personajes.add(crear("Marta", Genero.FEMENINO, true, false, ColorPelo.NINGUNO, false));
        personajes.add(crear("Bruno", Genero.MASCULINO, false, true, ColorPelo.NEGRO, false));

        validarPersonajes(personajes);
        return personajes;
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
