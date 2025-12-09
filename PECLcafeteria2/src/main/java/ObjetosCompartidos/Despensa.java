package ObjetosCompartidos;

/**
 *
 * @author Sergio
 */

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Despensa – recurso compartido entre Cocineros (productores) y Vendedores
 * (consumidores).
 * Almacén con capacidad lógica ilimitada de unidades (cafés y rosquillas).
 * Sincronización: ReentrantLock + Condition (una por tipo).
 */
public class Despensa {

    // Semáforo con 20 permisos
    private Semaphore aforoVendedores = new Semaphore(50, true);
    private Semaphore aforoCocineros = new Semaphore(50, true);

    // Estanterías como listas de Strings
    private ArrayList<String> estanteriaCafes = new ArrayList<>();
    private ArrayList<String> estanteriaRosquillas = new ArrayList<>();

    // --- Café ---
    private Lock lockCafe = new ReentrantLock(true); // justo
    private int contadorCafe = 0;
    private Condition cafeAvailable = lockCafe.newCondition();

    // --- Rosquilla ---
    private Lock lockRosquilla = new ReentrantLock(true); // justo
    private int contadorRosquilla = 0;
    private Condition rosquillaAvailable = lockRosquilla.newCondition();

    /** Entra al mostrador (máx 5 clientes simultáneos). */
    public void pasarVendedoresDespensa(String id, int nc, int nr) throws InterruptedException {
        int tomadosCafe = 0;
        int tomadosRosquillas = 0;
        aforoVendedores.acquire();
        Logger.log(id + " entra en la despensa");
        System.out.println(id + " entra en la despensa");
        try {
            // Tomar cafés
            lockCafe.lock();
            try {
                while (estanteriaCafes.isEmpty() && nc > 0) {
                    cafeAvailable.await();
                }
                // Cogemos el máximo posible hasta cubrir la demanda (Parcial o Total)
                int disponibles = estanteriaCafes.size();
                int aCoger = Math.min(disponibles, nc);

                for (int i = 0; i < aCoger; i++) {
                    estanteriaCafes.remove(0);
                    contadorCafe--;
                    tomadosCafe++;
                }
            } finally {
                lockCafe.unlock();
            }

            // Tomar rosquillas
            lockRosquilla.lock();
            try {
                while (estanteriaRosquillas.isEmpty() && nr > 0) {
                    rosquillaAvailable.await();
                }
                int disponibles = estanteriaRosquillas.size();
                int aCoger = Math.min(disponibles, nr);

                for (int i = 0; i < aCoger; i++) {
                    String comanda = estanteriaRosquillas.remove(0);
                    contadorRosquilla--;
                    tomadosRosquillas++;
                    System.out.println("Vendedor retira: " + comanda);
                }
            } finally {
                lockRosquilla.unlock();
            }
            Logger.log(
                    id + " ha cogido " + tomadosCafe + " cafés y " + tomadosRosquillas + " rosquillas (Parcial/Total)");
            System.out.println(
                    id + " ha cogido " + tomadosCafe + " cafés y " + tomadosRosquillas + " rosquillas (Parcial/Total)");
            Thread.sleep((int) (1000 + (3000 - 1000) * Math.random())); // tiempo en despensa
        } finally {
            aforoVendedores.release();
        }
    }

    /** Entra al mostrador (máx 5 clientes simultáneos). */
    public void pasarCocinerosDespensa(String id, int nc, int nr) throws InterruptedException {
        aforoCocineros.acquire();
        Logger.log(id + " entra en la despensa para depositar");
        System.out.println(id + " entra en la despensa para depositar");

        // Depositar cafés
        lockCafe.lock();
        try {
            for (int i = 0; i < nc; i++) {
                contadorCafe++;
                estanteriaCafes.add("Cafe-" + contadorCafe);
            }
            cafeAvailable.signalAll();
        } finally {
            lockCafe.unlock();
        }

        // Depositar rosquillas
        lockRosquilla.lock();
        try {
            for (int i = 0; i < nr; i++) {
                contadorRosquilla++;
                estanteriaRosquillas.add("Rosquilla-" + contadorRosquilla);
            }
            rosquillaAvailable.signalAll();
        } finally {
            lockRosquilla.unlock();
        }

        System.out.println("Cocinero" + id + " ha colocado los productos en la despensa");
        Logger.log("Cocinero" + id + " ha colocado los productos en la despensa");
        aforoCocineros.release();
    }

    /**
     * Obtiene el número de vendedores actualmente en la despensa.
     */
    public int getVendedoresEnDespensa() {
        return 50 - aforoVendedores.availablePermits();
    }

    /**
     * Obtiene el número de cocineros actualmente en la despensa.
     */
    public int getCocineroEnDespensa() {
        return 50 - aforoCocineros.availablePermits();
    }

    /** Lectura thread-safe del número de rosquillas almacenadas. */
    public int getRosquillas() {
        lockRosquilla.lock();
        try {
            return estanteriaRosquillas.size();
        } finally {
            lockRosquilla.unlock();
        }
    }

    /** Lectura thread-safe del número de cafés almacenados. */
    public int getCafes() {
        lockCafe.lock();
        try {
            return estanteriaCafes.size();
        } finally {
            lockCafe.unlock();
        }
    }
}
