/**
 * Hilo que controla la secuencia del semáforo durante la carrera.
 *
 * Secuencia:
 *  - Al iniciar: VERDE (caballos corren durante 30 segundos)
 *  - A los 25s:  AMARILLO (advertencia, caballos se detienen)
 *  - A los 30s:  ROJO (fin de carrera)
 *
 * Notifica al listener cuando cambia de estado.
 */
public class HiloSemaforo extends Thread {

    public interface SemaforoListener {
        void onEstadoCambiado(Semaforo.Estado estado);
        void onCarreraTerminada();
    }

    private static final int DURACION_TOTAL_MS   = 30_000; // 30 segundos
    private static final int DURACION_VERDE_MS   = 25_000; // 25s en verde
    private static final int DURACION_AMARILLO_MS = 5_000; // 5s en amarillo

    private final Semaforo semaforo;
    private final SemaforoListener listener;

    public HiloSemaforo(Semaforo semaforo, SemaforoListener listener) {
        this.semaforo = semaforo;
        this.listener = listener;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            // --- VERDE ---
            semaforo.setEstado(Semaforo.Estado.VERDE);
            listener.onEstadoCambiado(Semaforo.Estado.VERDE);

            Thread.sleep(DURACION_VERDE_MS);

            if (isInterrupted()) return;

            // --- AMARILLO ---
            semaforo.setEstado(Semaforo.Estado.AMARILLO);
            listener.onEstadoCambiado(Semaforo.Estado.AMARILLO);

            Thread.sleep(DURACION_AMARILLO_MS);

            if (isInterrupted()) return;

            // --- ROJO ---
            semaforo.setEstado(Semaforo.Estado.ROJO);
            listener.onEstadoCambiado(Semaforo.Estado.ROJO);

            listener.onCarreraTerminada();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
