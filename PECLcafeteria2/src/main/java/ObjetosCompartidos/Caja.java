package ObjetosCompartidos;

/**
 *
 * @author Sergio
 */

import java.util.concurrent.Semaphore;

public final class Caja {

    // Semáforo para controlar el aforo (10 clientes simultáneos)
    private Semaphore aforoCaja = new Semaphore(10, true);

    // Variable compartida para ingresos totales
    private double ingresosTotales = 0.0;

    // Precios
    private double PRECIO_CAFE = 1.50;
    private double PRECIO_ROSQUILLA = 2.50;

    /**
     * Cliente entra a la caja (bloquea si hay 10 clientes).
     */
    public void entrarCaja(String idCliente) throws InterruptedException {
        aforoCaja.acquire();
        Logger.log(idCliente + " accede a la caja");
        System.out.println(idCliente + " accede a la caja");
    }

    /**
     * Cliente sale de la caja (libera el aforo).
     */
    public void salirCaja(String idCliente) {
        aforoCaja.release();
    }

    /**
     * Proceso de pago: calcula total, actualiza ingresos y simula tiempo de pago
     * (2–5 s).
     */
    public void pagar(String idCliente, int cafes, int rosquillas) throws InterruptedException {
        double total = (cafes * PRECIO_CAFE) + (rosquillas * PRECIO_ROSQUILLA);

        // Simular tiempo de pago (2–5 s)
        Thread.sleep((int) (2000 + (5000 - 2000) * Math.random()));

        // Actualizar ingresos de forma segura
        synchronized (this) {
            ingresosTotales += total;
        }

        System.out.println(idCliente + " paga " + total + " € por " + cafes + " cafes y " + rosquillas + " rosquillas");
    }

    /**
     * Obtener ingresos totales actuales.
     */
    public synchronized double getIngresosTotales() {
        return ingresosTotales;
    }

    /**
     * Obtiene el número de personas actualmente en la caja.
     */
    public int getClientesEnCaja() {
        return 10 - aforoCaja.availablePermits();
    }
}
