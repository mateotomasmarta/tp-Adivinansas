package com.tp.adivinanzas.persistencia;

import java.util.List;

/**
 * DAO: aisla el "como" se persiste (archivo, base, memoria) del "que" se
 * persiste. El resto del juego depende de esta interfaz, nunca del archivo.
 *
 * La implementacion usa un HashMap para acceso promedio O(1) a las
 * estadisticas de cada jugador por nombre. La persistencia completa es O(n),
 * porque guardar o cargar obliga a recorrer todos los registros.
 */
public interface RecordDAO {

    void cargar();

    void guardar();

    void mostrar();

    void registrarVictoria(String nombre);

    /**
     * Devuelve el marcador ordenado de mayor a menor cantidad de victorias.
     */
    List<RecordJugador> listarOrdenado();
}
