package com.tp.adivinanzas.motor;

import java.util.ArrayList;
import java.util.List;

/**
 * Sujeto del patron Observer y registro cronologico de preguntas respondidas.
 */
public final class HistorialPreguntas {
    private final List<ResultadoPregunta> resultados = new ArrayList<ResultadoPregunta>();
    private final List<ObservadorPreguntas> observadores = new ArrayList<ObservadorPreguntas>();

    public void agregarObservador(ObservadorPreguntas observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void quitarObservador(ObservadorPreguntas observador) {
        observadores.remove(observador);
    }

    public void registrar(ResultadoPregunta resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("El resultado es obligatorio.");
        }
        resultados.add(resultado);

        for (ObservadorPreguntas observador : observadores) {
            if (observador.debeObservar(resultado.getPregunta())) {
                observador.alResponderPregunta(resultado);
            }
        }
    }

    public List<ResultadoPregunta> listarResultados() {
        return new ArrayList<ResultadoPregunta>(resultados);
    }

    public int cantidad() {
        return resultados.size();
    }
}
