package ObjetosCompartidos;

/**
 *
 * @author Sergio
 */

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EntradaCafeteria {

    private Semaphore aforoZonaPrevia = new Semaphore(20, true);; // máx 20

    private final ArrayList<String> clientesEnCola = new ArrayList<>();
    // Cerrojo para proteger la lista 
    private final Lock cerrojoLista = new ReentrantLock();

    /** Entra en la zona previa (cola antes del mostrador). */
    public void entrarZonaPrevia(String id) throws InterruptedException {
        aforoZonaPrevia.acquire();
        // Protección de escritura
        cerrojoLista.lock();
        try {
            clientesEnCola.add(id);
        } finally {
            cerrojoLista.unlock();
        }
        Logger.log(id + " entra en la zona previa");
        System.out.println(id + " esta en zona previa (permits restantes=" + aforoZonaPrevia.availablePermits() + ")");
        //Thread.sleep((int) (1000 + (3000 - 1000) * Math.random())); // opcional: tiempo en cola previa
    }

    /**
     * El cliente LIBERA su lugar porque ya ha pasado al mostrador.
     */
    public void salirZonaPrevia(String id) {
        cerrojoLista.lock();
        try {
            clientesEnCola.remove(id);
        } finally {
            cerrojoLista.unlock();
        }
        aforoZonaPrevia.release();
    }

    public String getClientesString() {
            cerrojoLista.lock();
            try {
                return clientesEnCola.toString();
            } finally {
                cerrojoLista.unlock();
            }
        }
}
