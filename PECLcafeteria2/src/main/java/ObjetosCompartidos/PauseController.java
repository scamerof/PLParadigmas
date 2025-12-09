package ObjetosCompartidos;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * Controlador de pausa/reanudación del sistema.
 * Permite pausar y reanudar todos los hilos del programa.
 */
public class PauseController {
    
    private boolean paused = false;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition unpausedCondition = lock.newCondition();
    
    /**
     * Pausa el sistema (los hilos esperarán en checkPause()).
     */
    public void pause() {
        lock.lock();
        try {
            paused = true;
            Logger.log("SISTEMA PAUSADO");
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Reanuda el sistema (despierta todos los hilos en pausa).
     */
    public void resume() {
        lock.lock();
        try {
            paused = false;
            unpausedCondition.signalAll();
            Logger.log("SISTEMA REANUDADO");
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Verifica si el sistema está pausado.
     * Si está pausado, el hilo se bloquea hasta que se reanude.
     * Debe llamarse periódicamente en los hilos (Cliente, Cocinero, Vendedor).
     */
    public void checkPause() throws InterruptedException {
        lock.lock();
        try {
            while (paused) {
                unpausedCondition.await();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Obtiene el estado actual de pausa.
     */
    public boolean isPaused() {
        lock.lock();
        try {
            return paused;
        } finally {
            lock.unlock();
        }
    }
}
