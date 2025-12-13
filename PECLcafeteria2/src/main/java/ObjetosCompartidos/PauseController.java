package ObjetosCompartidos;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// La clase Paso define un cerrojo con un Condition para la variable booleana pausado
// que es comprobada por un proceso.
// Si vale false (abierto) el proceso puede continuar. Si es true (pausado) el proceso se detiene.
public class PauseController{
    private boolean pausado = false;
    private Lock cerrojo = new ReentrantLock();
    private Condition parar = cerrojo.newCondition();

    public void checkPause() throws InterruptedException {
        try {
            cerrojo.lock();
            while (pausado) {
                try {
                    parar.await();
                } catch (InterruptedException ie) { }
            }
        } finally {
            cerrojo.unlock();
        }
    }

    public void resume() {
        try {
            cerrojo.lock();
            pausado = false;
            parar.signalAll(); // se cambia la condición por la que otros hilos podrían estar esperando
            Logger.log("SISTEMA REANUDADO");
        } finally {
            cerrojo.unlock();
        }
    }

    public void pause() {
        try {
            cerrojo.lock();
            pausado = true;
            Logger.log("SISTEMA PAUSADO");
        } finally {
            cerrojo.unlock();
        }
    }
    
    public boolean isPaused() {
        cerrojo.lock();
        try {
            return pausado;
        } finally {
            cerrojo.unlock();
        }
    }
}