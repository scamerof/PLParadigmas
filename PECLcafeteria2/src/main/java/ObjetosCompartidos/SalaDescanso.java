package ObjetosCompartidos;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SalaDescanso {
    
    // Listas normales
    private final ArrayList<String> cocineros = new ArrayList<>();
    private final ArrayList<String> vendedores = new ArrayList<>();
    
    private final Lock lock = new ReentrantLock(); // Reutilizamos este lock para todo

    public void entrarCocinero(String id) {
        lock.lock();
        try {
            cocineros.add(id);
            System.out.println(id + " entra a la sala de descanso");
        } finally { lock.unlock(); }
    }

    public void salirCocinero(String id) {
        lock.lock();
        try {
            cocineros.remove(id);
            System.out.println(id + " sale de la sala de descanso");
        } finally { lock.unlock(); }
    }

    public void entrarVendedor(String id) {
        lock.lock();
        try {
            vendedores.add(id);
            System.out.println(id + " entra a la sala de descanso");
        } finally { lock.unlock(); }
    }

    public void salirVendedor(String id) {
        lock.lock();
        try {
            vendedores.remove(id);
            System.out.println(id + " sale de la sala de descanso");
        } finally { lock.unlock(); }
    }

    public String getCocinerosString() {
        lock.lock();
        try { return cocineros.toString(); } finally { lock.unlock(); }
    }

    public String getVendedoresString() {
        lock.lock();
        try { return vendedores.toString(); } finally { lock.unlock(); }
    }

    public int getCocineros() {
        lock.lock(); try { return cocineros.size(); } finally { lock.unlock(); }
    }
    public int getVendedores() {
        lock.lock(); try { return vendedores.size(); } finally { lock.unlock(); }
    }
}