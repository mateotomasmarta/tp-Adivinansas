package com.tp.adivinanzas;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.InterfazHumano;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.List;
import java.util.Scanner;

public class ConsolaJuego implements InterfazHumano{
    private final Scanner scanner;

    public ConsolaJuego(){
        scanner = new Scanner(System.in);
    }

    @Override
    public Filtro elegirFiltro(String nombreJugador, List<Filtro> disponibles){
        System.out.println(nombreJugador + " elegí una pregunta:");

        for(int i = 0; i < disponibles.size(); i++){
            System.out.println((i + 1) + "." + disponibles.get(i).getDescripcion());
        }

        int opcion = scanner.nextInt();

        while(opcion < 1 || opcion > disponibles.size()){
            System.out.println("Opción invalida, elija nuevamente");
            opcion = scanner.nextInt();
        }
        return disponibles.get(opcion - 1);

    }

    @Override
    public Personaje elegirPersonaje(String nombreJugador, List<Personaje> disponibles){
        System.out.println(nombreJugador + " elegí un personaje:");

        for(Personaje personaje : disponibles){
            System.out.println(personaje.getId() + "." + personaje.getNombre());
        }

        System.out.println("Ingresá el ID: ");
        int id = scanner.nextInt();

        for (Personaje personaje : disponibles){
            if(personaje.getId() == id){
                return personaje;
            }
        }
        System.out.println("ID inválido. Elegí nuevamente");
        return elegirPersonaje(nombreJugador, disponibles);
    }
}