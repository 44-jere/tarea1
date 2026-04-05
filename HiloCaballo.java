import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Hilo que representa un caballo en la carrera.
 * El caballo solo avanza mientras el semáforo esté en VERDE.
 * Se detiene cuando el semáforo cambia a AMARILLO o ROJO.
 */
public class HiloCaballo extends Thread {

    private final String nombre;
    private final JProgressBar barra;
    private final Semaforo semaforo;
    private volatile boolean corriendo = true;

    public HiloCaballo(String nombre, JProgressBar barra, Semaforo semaforo) {
        this.nombre = nombre;
        this.barra = barra;
        this.semaforo = semaforo;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            // Espera hasta que el semáforo esté en verde
            semaforo.esperarVerde();

            while (corriendo && !isInterrupted()) {
                // Solo avanza si el semáforo sigue en verde
                if (!semaforo.isVerde()) {
                    break; // detiene el caballo
                }

                // Avanza un paso aleatorio (simula velocidad distinta por caballo)
                int avance = (int) (Math.random() * 3) + 1;
                int valorActual = barra.getValue();
                int nuevoValor = Math.min(valorActual + avance, barra.getMaximum());

                SwingUtilities.invokeLater(() -> barra.setValue(nuevoValor));

                // Pausa corta entre pasos (~100ms)
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Detiene el avance del caballo */
    public void detener() {
        corriendo = false;
        interrupt();
    }

    public String getNombreCaballo() {
        return nombre;
    }
}
