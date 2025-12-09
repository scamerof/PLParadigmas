package ObjetosCompartidos;
/**
 *
 * @author Sergio
 */

import java.util.concurrent.Semaphore;

/**
 * Cocina: zona donde los cocineros preparan productos.
 * Aforo máximo: 100 cocineros simultáneamente.
 */
public class Cocina {

    // Semáforo para controlar el aforo (100 cocineros)
    private static Semaphore aforoCocina = new Semaphore(100, true);

    /**
     * Permite que un cocinero entre a la cocina.
     * Bloquea si el aforo está completo.
     */
    public void pasarCocinaCocineros(String id, int cafes, int rosquillas) throws InterruptedException {
        try {
        aforoCocina.acquire();
        Logger.log(id + " entra en la cocina");
        System.out.println(id + " entra en cocina (permits restantes=" + aforoCocina.availablePermits() + ")");
        Logger.log(id + " produce " + cafes + " cafes y " + rosquillas + " rosquillas");
        System.out.println(id + " produce " + cafes + " cafes y " + rosquillas + " rosquillas");
        Thread.sleep((int)(5000 + (10000 - 5000) * Math.random())); // tiempo de preparación
        } finally{
        aforoCocina.release();
        }
    }
    
    /**
     * Obtiene el número de cocineros actualmente en la cocina.
     */
    public int getCocineroEnCocina() {
        return 100 - aforoCocina.availablePermits();
    }


}
