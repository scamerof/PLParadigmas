package ObjetosCompartidos;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Área de consumo: donde los clientes consumen sus productos.
 * Aforo máximo: 30 clientes simultáneamente.
 */
public class AreaConsumo {

    // Semáforo para controlar el aforo (30 clientes)
    private Semaphore aforoAreaConsumo = new Semaphore(30, true);
    private final ArrayList<String> clientesComiendo = new ArrayList<>();
    private final Lock cerrojoLista = new ReentrantLock();

    /**
     * Cliente entra al área de consumo.
     */
    public void pasarAreaConsumo(String idCliente) throws InterruptedException {
        try {
            aforoAreaConsumo.acquire();

            cerrojoLista.lock();
            try { clientesComiendo.add(idCliente); } finally { cerrojoLista.unlock(); }

            Logger.log(idCliente + " entra al area de consumo");
            System.out.println(idCliente + " esta al area de consumo (permits restantes="+ aforoAreaConsumo.availablePermits() + ")");
            Thread.sleep((int) (10000 + (15000 - 10000) * Math.random())); // tiempo de consumo
        } finally {
            cerrojoLista.lock();
            try { clientesComiendo.remove(idCliente); } finally { cerrojoLista.unlock(); }

            aforoAreaConsumo.release();
        }
    }

    public String getClientesString() {
            cerrojoLista.lock();
            try { return clientesComiendo.toString(); } finally { cerrojoLista.unlock(); }
        
    }
}