package ObjetosCompartidos;

import java.util.ArrayList;

/**
 *
 * @author Sergio
 */

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Caja {

    // Semáforo para controlar el aforo (10 clientes simultáneos)
    private Semaphore aforoCaja = new Semaphore(10, true);

    // Lista y su cerrojo
    private final ArrayList<String> clientesEnCaja = new ArrayList<>();
    private final Lock cerrojoLista = new ReentrantLock();

    // Cerrojo para proteger EXCLUSIVAMENTE el acceso a la variable de ingresos
    private final Lock cajaLock = new ReentrantLock(true);
    private final Lock cerrojo = new ReentrantLock(true);

    // Variable compartida para ingresos totales
    private double ingresosTotales = 0.0;
    private double PRECIO_CAFE = 1.50;
    private double PRECIO_ROSQUILLA = 2.50;

/**
     * Cliente entra a la caja, ocupa un puesto, realiza el pago y actualiza ingresos.
     * El aforo se mantiene ocupado hasta que se llame a salirCaja().
     */
    public void pasarPorCaja(String idCliente, int cafes, int rosquillas) throws InterruptedException {
// 1. Entrar (Adquirir aforo)
        aforoCaja.acquire();

        cerrojoLista.lock();
        try { clientesEnCaja.add(idCliente); } finally { cerrojoLista.unlock(); }

        try {
            Logger.log(idCliente + " accede a la caja");
            System.out.println(idCliente + " accede a la caja");

            // 2. Lógica de Pago (Simulación de tiempo)
            double total = (cafes * PRECIO_CAFE) + (rosquillas * PRECIO_ROSQUILLA);
            Thread.sleep((int)(2000 + (5000 - 2000) * Math.random()));

            // 3. Actualizar ingresos (Sección crítica protegida por Lock)
            cajaLock.lock();
            try {
                ingresosTotales += total;
            } finally {
                cajaLock.unlock();
            }
            String mensaje = String.format("%s paga %.2f € por %d cafes y %d rosquillas", idCliente, total, cafes, rosquillas);
            System.out.println(mensaje);
            Logger.log(idCliente + " ha realizado el pago");
        } finally {
            cerrojoLista.lock();
            try { clientesEnCaja.remove(idCliente); } finally { cerrojoLista.unlock(); }

            aforoCaja.release();
        }
    }  
    
    /**
     * Obtener ingresos totales actuales (Lectura thread-safe).
     */
    public double getIngresosTotales() {
        cerrojo.lock();
        try {
            return ingresosTotales;
        } finally {
            cerrojo.unlock();
        }
    }

    public String getClientesString() {
            cerrojoLista.lock();
            try { return clientesEnCaja.toString(); } finally { cerrojoLista.unlock(); }
        }

    
    public int getClientesEnCaja() {
        cerrojoLista.lock();
        try { return clientesEnCaja.size(); } finally { cerrojoLista.unlock(); }
    }
}