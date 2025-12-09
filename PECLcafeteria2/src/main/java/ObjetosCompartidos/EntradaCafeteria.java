package ObjetosCompartidos;

/**
 *
 * @author Sergio
 */

import java.util.concurrent.Semaphore;

public class EntradaCafeteria {

    private Semaphore aforoZonaPrevia = new Semaphore(20, true);; // máx 20

    /** Entra en la zona previa (cola antes del mostrador). */
    public void entrarZonaPrevia(String id) throws InterruptedException {
        aforoZonaPrevia.acquire();
        Logger.log(id + " entra en la zona previa");
        System.out.println(id + " esta en zona previa (permits restantes=" + aforoZonaPrevia.availablePermits() + ")");
        Thread.sleep((int) (1000 + (3000 - 1000) * Math.random())); // opcional: tiempo en cola previa
    }

    /**
     * El cliente LIBERA su lugar porque ya ha pasado al mostrador.
     */
    public void salirZonaPrevia(String id) {
        aforoZonaPrevia.release();
        // No hace falta loguear mucho aquí, porque coincide con entrar al mostrador
    }

    /** Obtiene el número de personas en la zona previa. */
    public int getClientesEnZonaPrevia() {
        return 20 - aforoZonaPrevia.availablePermits();
    }

}
