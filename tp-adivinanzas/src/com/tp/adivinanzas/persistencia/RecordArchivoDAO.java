package persistencia;

import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class RecordArchivoDAO implements RecordDAO{
    private final Map<String, Integer> records;

    public RecordArchivoDAO(){
        records = new HashMap<>();
    }

    @Override
    public void cargar() {
        File archivo = new File("records.txt");

        try{
            BufferedReader br = new BufferedReader(new FileReader(archivo));

            String linea;

            while((linea = br.readLine()) != null){
                String[] datos = linea.split(";");

                String nombre = datos[0];
                int victorias = Integer.parseInt(datos[1]);

                records.put(nombre, victorias);
            }
            br.close();
        } catch (IOException e){
            System.out.println("No se pudo cargar el archivo");
        }
    }

    @Override
    public void guardar() {
        File archivo = new File("records.txt");

        try{
            FileWriter fw = new FileWriter(archivo);
            PrintWriter pw = new PrintWriter(fw);

            for(Map.Entry<String, Integer> entrada : records.entrySet()){
                pw.println(entrada.getKey() + ";" + entrada.getValue());
            }
            pw.close();
        } catch (IOException e){
            System.out.println("No se pudo escribir el archivo");
        }      
    }

    @Override
    public void mostrar() {
        for (Map.Entry<String, Integer> entrada : records.entrySet()) {
            System.out.println(
                    entrada.getKey() + ": " + entrada.getValue() + " victorias"
            );
        }
    }

    @Override
    public void registrarVictoria(String nombre) {
        records.put(nombre, records.getOrDefault(nombre, 0) + 1);
    }
}