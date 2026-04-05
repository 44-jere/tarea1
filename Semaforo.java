import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase Semaforo que sincroniza la carrera de caballos.
 * Maneja los estados: ROJO, VERDE, AMARILLO.
 * Los caballos solo pueden avanzar cuando el semaforo está en VERDE.
 */
public class Semaforo {

    public enum Estado { ROJO, VERDE, AMARILLO }

    private Estado estadoActual = Estado.ROJO;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition esVerde = lock.newCondition();

    public Estado getEstado() {
        lock.lock();
        try {
            return estadoActual;
        } finally {
            lock.unlock();
        }
    }

    /** Cambia el estado y notifica a los caballos si pasa a VERDE */
    public void setEstado(Estado nuevoEstado) {
        lock.lock();
        try {
            this.estadoActual = nuevoEstado;
            if (nuevoEstado == Estado.VERDE) {
                esVerde.signalAll(); // despierta a todos los caballos
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Bloquea el hilo del caballo hasta que el semáforo esté en VERDE.
     * Retorna false si el hilo fue interrumpido.
     */
    public boolean esperarVerde() throws InterruptedException {
        lock.lock();
        try {
            while (estadoActual != Estado.VERDE) {
                esVerde.await();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Retorna true si el semáforo está actualmente en VERDE */
    public boolean isVerde() {
        lock.lock();
        try {
            return estadoActual == Estado.VERDE;
        } finally {
            lock.unlock();
        }
    }
}
