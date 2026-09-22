package com.tp.adivinanzas.persistencia;

import com.tp.adivinanzas.repositorio.OrdenadorMergeSort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion del DAO sobre un archivo CSV.
 *
 * HashMap en memoria: registrar una victoria y consultar el marcador de un
 * jugador son O(1) promedio. Cargar y guardar son O(n) porque recorren todos
 * los registros.
 *
 * Listar el marcador ordenado reutiliza el MergeSort propio (B1) en vez de
 * Collections.sort, para mantener un unico algoritmo de ordenamiento en todo
 * el proyecto.
 */
public class RecordArchivoDAO implements RecordDAO {
    private static final String RUTA_ARCHIVO = "datos/records.csv";
    private static final String SEPARADOR = ";";

    private final Map<String, Integer> records;
    private final File archivo;

    public RecordArchivoDAO() {
        this(RUTA_ARCHIVO);
    }

    public RecordArchivoDAO(String ruta) {
        this.records = new HashMap<String, Integer>();
        this.archivo = new File(ruta);
    }

    @Override
    public void cargar() {
        records.clear();

        if (!archivo.exists()) {
            return;
        }

        BufferedReader lector = null;
        try {
            lector = new BufferedReader(new FileReader(archivo));
            String linea;

            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue;
                }

                String[] datos = linea.split(SEPARADOR);
                if (datos.length < 2) {
                    continue;
                }

                try {
                    records.put(datos[0], Integer.parseInt(datos[1].trim()));
                } catch (NumberFormatException e) {
                    // Linea corrupta: se ignora en vez de romper la carga.
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de records: " + e.getMessage());
        } finally {
            cerrar(lector);
        }
    }

    @Override
    public void guardar() {
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }

        PrintWriter escritor = null;
        try {
            escritor = new PrintWriter(new FileWriter(archivo));
            for (Map.Entry<String, Integer> entrada : records.entrySet()) {
                escritor.println(entrada.getKey() + SEPARADOR + entrada.getValue());
            }
        } catch (IOException e) {
            System.out.println("No se pudo escribir el archivo de records: " + e.getMessage());
        } finally {
            if (escritor != null) {
                escritor.close();
            }
        }
    }

    @Override
    public void mostrar() {
        List<RecordJugador> marcador = listarOrdenado();

        if (marcador.isEmpty()) {
            System.out.println("Todavia no hay partidas ganadas registradas.");
            return;
        }

        int posicion = 1;
        for (RecordJugador record : marcador) {
            System.out.println(posicion + ". " + record);
            posicion++;
        }
    }

    @Override
    public void registrarVictoria(String nombre) {
        records.put(nombre, records.getOrDefault(nombre, 0) + 1);
        guardar();
    }

    @Override
    public List<RecordJugador> listarOrdenado() {
        List<RecordJugador> marcador = new ArrayList<RecordJugador>();
        for (Map.Entry<String, Integer> entrada : records.entrySet()) {
            marcador.add(new RecordJugador(entrada.getKey(), entrada.getValue()));
        }

        Comparator<RecordJugador> porVictoriasDesc =
                Comparator.comparingInt(RecordJugador::getPartidasGanadas).reversed();

        return OrdenadorMergeSort.ordenar(marcador, porVictoriasDesc);
    }

    private void cerrar(BufferedReader lector) {
        if (lector == null) {
            return;
        }
        try {
            lector.close();
        } catch (IOException e) {
            // Nada util que hacer al cerrar.
        }
    }
}
