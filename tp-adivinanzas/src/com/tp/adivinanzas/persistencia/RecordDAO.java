//Utilizamos un HashMap para obtener acceso promedio O(1) a las estadísticas de cada jugador mediante su nombre.
//La persistencia completa tiene O(n), ya que para guardar o cargar el archivo debemos procesar todos los registros."

package persistencia;

public interface RecordDAO {

    void cargar();

    void guardar();

    void mostrar();

    void registrarVictoria(String nombre);
}