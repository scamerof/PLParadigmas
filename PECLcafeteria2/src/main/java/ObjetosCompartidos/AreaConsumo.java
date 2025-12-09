package ObjetosCompartidos;

import java.util.concurrent.Semaphore;

/**
 * Área de consumo: donde los clientes consumen sus productos.
 * Aforo máximo: 30 clientes simultáneamente.
 */
public class AreaConsumo {

    // Semáforo para controlar el aforo (30 clientes)
    private Semaphore aforoAreaConsumo = new Semaphore(30, true);

    /**
     * Cliente entra al área de consumo.
     */
    public void pasarAreaConsumo(String idCliente) throws InterruptedException {
        try {
            aforoAreaConsumo.acquire();
            Logger.log(idCliente + " entra al area de consumo");
            System.out.println(idCliente + " esta al area de consumo (permits restantes="+ aforoAreaConsumo.availablePermits() + ")");
            Thread.sleep((int) (10000 + (15000 - 10000) * Math.random())); // tiempo de consumo
        } finally {
            aforoAreaConsumo.release();
        }
    }

    /**
     * Devuelve el número actual de clientes en el área.
     */
    public int getClientesEnArea() {
        return 30 - aforoAreaConsumo.availablePermits();
    }
}
