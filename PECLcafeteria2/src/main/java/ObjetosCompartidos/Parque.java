package ObjetosCompartidos;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Parque {

    // Lista para monitorizar IDs
    private final ArrayList<String> clientesEnParque = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    public void entrar(String id) {
        lock.lock();
        try {
            clientesEnParque.add(id);
            Logger.log(id + " esta en el parque");
            System.out.println(id + " esta en el parque");

        } finally {
            lock.unlock();
        }
    }

    public void salir(String id) {
        lock.lock();
        try {
            clientesEnParque.remove(id);
            // System.out.println(id + " sale del parque");
        } finally {
            lock.unlock();
        }
    }

    // Para la GUI local (devuelve String de IDs)
    public String getClientesString() {
        lock.lock();
        try {
            return clientesEnParque.toString();
        } finally {
            lock.unlock();
        }
    }

    // Para RMI (devuelve cantidad numérica)
    public int getNumeroClientes() {
        lock.lock();
        try {
            return clientesEnParque.size();
        } finally {
            lock.unlock();
        }
    }
}