package com.tp.adivinanzas.jugadores;

import com.tp.adivinanzas.filtros.CatalogoFiltros;
import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.estrategias.Estrategia;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Delega en una Estrategia (B4) que filtro preguntar y que personaje
 * arriesgar. Mantiene su propia lista de candidatos, que va reduciendo con
 * cada respuesta del rival: la Estrategia nunca ve el personaje secreto
 * ajeno, solo el resultado publico de cada pregunta.
 */
public final class JugadorMaquina extends Jugador {
    private final Estrategia estrategia;
    private final List<Filtro> filtrosUsados = new ArrayList<Filtro>();
    private List<Personaje> candidatos;

    public JugadorMaquina(String nombre, Personaje personajeSecreto, Estrategia estrategia, List<Personaje> candidatosIniciales) {
        super(nombre, personajeSecreto);
        this.estrategia = Objects.requireNonNull(estrategia, "La estrategia es obligatoria.");
        Objects.requireNonNull(candidatosIniciales, "Los candidatos iniciales son obligatorios.");
        this.candidatos = new ArrayList<Personaje>(candidatosIniciales);
    }

    @Override
    public Filtro elegirPregunta() {
        List<Filtro> disponibles = new ArrayList<Filtro>(CatalogoFiltros.listarTodos());
        disponibles.removeAll(filtrosUsados);
        if (disponibles.isEmpty()) {
            throw new IllegalStateException("No quedan filtros disponibles para preguntar.");
        }

        Filtro elegido = estrategia.elegirFiltro(candidatos, disponibles);
        filtrosUsados.add(elegido);
        return elegido;
    }

    /**
     * Aplica la respuesta obtenida a una pregunta propia sobre el personaje
     * del rival, descartando del propio pool de candidatos a quienes no la
     * cumplan igual.
     */
    public void actualizarCandidatos(Filtro filtro, boolean respuesta) {
        List<Personaje> restantes = new ArrayList<Personaje>();
        for (Personaje candidato : candidatos) {
            if (filtro.cumple(candidato) == respuesta) {
                restantes.add(candidato);
            }
        }
        this.candidatos = restantes;
    }

    @Override
    public Personaje arriesgarPersonaje() {
        if (candidatos.isEmpty()) {
            throw new IllegalStateException("No quedan candidatos posibles: alguna respuesta fue inconsistente.");
        }
        return estrategia.elegirPersonaje(candidatos);
    }

    public List<Personaje> getCandidatos() {
        return Collections.unmodifiableList(candidatos);
    }
}
