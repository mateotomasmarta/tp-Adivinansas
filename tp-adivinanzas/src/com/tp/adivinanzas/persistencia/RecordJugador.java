package persistencia;

public class RecordJugador{
    private String nombre;
    private int partidasGanadas;

    public RecordJugador(String nombre, int partidasGanadas){
        this.nombre = nombre;
        this.partidasGanadas = partidasGanadas;
    }

    public String getNombre(){
        return nombre;
    }

    public int getPartidasGanadas(){
        return partidasGanadas;
    }
}

