package ObjetosCompartidos;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * package ObjetosCompartidos;
 * 
 * import java.util.ArrayList;
 * import java.util.concurrent.Semaphore;
 * import java.util.concurrent.locks.Condition;
 * import java.util.concurrent.locks.Lock;
 * import java.util.concurrent.locks.ReentrantLock;
 * 
 * /**
 * Mostrador – recurso compartido entre Vendedores (que reponen) y Clientes (que
 * retiran).
 * Almacén con capacidad lógica ilimitada de unidades visibles (cafés y
 * rosquillas).
 * Sincronización: ReentrantLock + Condition (una por tipo).
 */
public class Mostrador {

    // Semáforos
    private static final Semaphore aforoVendedores = new Semaphore(20, true);
    private static final Semaphore aforoClientes = new Semaphore(5, true);

    // Estanterías como listas de Strings
    private final ArrayList<String> estanteriaCafes = new ArrayList<>();
    private final ArrayList<String> estanteriaRosquillas = new ArrayList<>();

    // Contadores para generar nombres únicos
    private int contadorCafe = 0;
    private int contadorRosquilla = 0;

    // Locks y condiciones por tipo de producto
    private final Lock lockCafe = new ReentrantLock(true);
    private final Condition cafeDisponible = lockCafe.newCondition();
    private final Lock lockRosquilla = new ReentrantLock(true);
    private final Condition rosquillaDisponible = lockRosquilla.newCondition();

    /**
     * Retira nc cafés y nr rosquillas del mostrador; espera si no hay suficiente.
     */
    public void pasarClienteMostrador(String id, int nc, int nr, EntradaCafeteria entrada) throws InterruptedException {
        if (nc < 0)
            throw new IllegalArgumentException("Cantidad negativa de cafés");
        if (nr < 0)
            throw new IllegalArgumentException("Cantidad negativa de rosquillas");

        aforoClientes.acquire();
        try {
            if (entrada != null) {
                entrada.salirZonaPrevia(id);
                Logger.log(id + " deja la zona previa y entra al mostrador");
                System.out.println(id + " deja zona previa y esta en el mostrador (permits restantes="
                        + aforoClientes.availablePermits() + ")");
            }

            // Tomar cafés
            lockCafe.lock();
            try {
                while (estanteriaCafes.size() < nc) {
                    cafeDisponible.await();
                }
                for (int i = 0; i < nc; i++) {
                    String comanda = estanteriaCafes.remove(0);
                    contadorCafe--;
                    System.out.println("Cliente retira: " + comanda);
                }
                Logger.log(id + " retira " + nc + " cafes");
            } finally {
                lockCafe.unlock();
            }

            // Tomar rosquillas
            lockRosquilla.lock();
            try {
                while (estanteriaRosquillas.size() < nr) {
                    rosquillaDisponible.await();
                }
                for (int i = 0; i < nr; i++) {
                    String comanda = estanteriaRosquillas.remove(0);
                    contadorRosquilla--;
                    System.out.println("Cliente retira: " + comanda);
                }
                Logger.log(id + " retira " + nr + " rosquillas");
            } finally {
                lockRosquilla.unlock();
            }
            Thread.sleep((int) (2000 + (4000 - 2000) * Math.random())); // tiempo en mostrador
        } finally {
            aforoClientes.release();
        }
    }

    /**
     * Coloca nc cafés y nr rosquillas en el mostrador y notifica a los clientes.
     */
    public void pasarVendedoresMostrador(String id, int nc, int nr) throws InterruptedException {
        if (nc < 0)
            throw new IllegalArgumentException("Cantidad negativa de cafés");
        if (nr < 0)
            throw new IllegalArgumentException("Cantidad negativa de rosquillas");

        aforoVendedores.acquire();
        try {
            System.out.println(
                    id + " esta en el mostrador (permits restantes=" + aforoVendedores.availablePermits() + ")");
            Logger.log(id + " entra en el mostrador");

            // Colocar cafés
            lockCafe.lock();
            try {
                for (int i = 0; i < nc; i++) {
                    contadorCafe++;
                    estanteriaCafes.add("Cafe-" + contadorCafe);
                }
                cafeDisponible.signalAll();
            } finally {
                lockCafe.unlock();
            }

            // Colocar rosquillas
            lockRosquilla.lock();
            try {
                for (int i = 0; i < nr; i++) {
                    contadorRosquilla++;
                    estanteriaRosquillas.add("Rosquilla-" + contadorRosquilla);
                }
                rosquillaDisponible.signalAll();
            } finally {
                lockRosquilla.unlock();
            }

            Thread.sleep((int) (1000 + (3000 - 1000) * Math.random())); // tiempo de colocación
            System.out.println(id + " ha colocado los productos en el mostrador");
            Logger.log(id + " ha colocado los productos en el mostrador");
        } finally {
            aforoVendedores.release();
        }
    }

    /** Lectura thread-safe del número de rosquillas en mostrador. */
    public int getRosquillas() {
        lockRosquilla.lock();
        try {
            return estanteriaRosquillas.size();
        } finally {
            lockRosquilla.unlock();
        }
    }

    /** Lectura thread-safe del número de cafés en mostrador. */
    public int getCafes() {
        lockCafe.lock();
        try {
            return estanteriaCafes.size();
        } finally {
            lockCafe.unlock();
        }
    }

    /** Obtiene el número de personas en el mostrador. */
    public int getClientesEnMostrador() {
        return 5 - aforoClientes.availablePermits();
    }

    /** Obtiene el número de vendedores en el mostrador. */
    public int getVendedoresEnMostrador() {
        return 20 - aforoVendedores.availablePermits();
    }
}
