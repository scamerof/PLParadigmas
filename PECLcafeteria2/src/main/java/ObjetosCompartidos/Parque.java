package ObjetosCompartidos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Parque {
    private int contadorClientes = 0;
    private final Lock lock = new ReentrantLock();

    public void entrar(String id) {
        lock.lock();
        try {
            contadorClientes++;
            // System.out.println(id + " entra al parque");
        } finally { lock.unlock(); }
    }

    public void salir(String id) {
        lock.lock();
        try {
            contadorClientes--;
            // System.out.println(id + " sale del parque");
        } finally { lock.unlock(); }
    }

    public int getNumeroClientes() {
        lock.lock();
        try {
            return contadorClientes;
        } finally { lock.unlock(); }
    }
}