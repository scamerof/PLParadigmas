package ObjetosCompartidos;
/**
 *
 * @author Sergio
 */

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cocina: zona donde los cocineros preparan productos.
 * Aforo máximo: 100 cocineros simultáneamente.
 */
public class Cocina {

    // Semáforo para controlar el aforo (100 cocineros)
    private static Semaphore aforoCocina = new Semaphore(100, true);
    private final ArrayList<String> cocinerosDentro = new ArrayList<>();
    private final Lock cerrojoLista = new ReentrantLock();
    /**
     * Permite que un cocinero entre a la cocina.
     * Bloquea si el aforo está completo.
     */
    public void pasarCocinaCocineros(String id, int cafes, int rosquillas) throws InterruptedException {
        try {
        aforoCocina.acquire();

        cerrojoLista.lock();
        try { cocinerosDentro.add(id); } finally { cerrojoLista.unlock(); }

        Logger.log(id + " entra en la cocina");
        System.out.println(id + " entra en cocina (permits restantes=" + aforoCocina.availablePermits() + ")");
        Logger.log(id + " produce " + cafes + " cafes y " + rosquillas + " rosquillas");
        System.out.println(id + " produce " + cafes + " cafes y " + rosquillas + " rosquillas");
        Thread.sleep((int)(5000 + (10000 - 5000) * Math.random())); // tiempo de preparación
        } finally{
        cerrojoLista.lock();
        try { cocinerosDentro.remove(id); } finally { cerrojoLista.unlock(); }

        aforoCocina.release();
        }
    }
    
    public String getCocinerosString() {
            cerrojoLista.lock();
            try { return cocinerosDentro.toString(); } finally { cerrojoLista.unlock(); }
        }
}
