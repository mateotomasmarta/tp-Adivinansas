package com.tp.adivinanzas;

import com.tp.adivinanzas.modelo.Personaje;
import com.tp.adivinanzas.repositorio.RepositorioPersonajes;

public class Main {
    public static void main(String[] args) {
        RepositorioPersonajes repositorio = RepositorioPersonajes.getInstancia();

        System.out.println("Personajes cargados: " + repositorio.cantidad());
        for (Personaje personaje : repositorio.listarTodos()) {
            System.out.println(personaje);
        }

        repositorio.buscarPorId(5)
                .ifPresent(personaje -> System.out.println("Busqueda binaria ID 5: " + personaje.getNombre()));
    }
}
