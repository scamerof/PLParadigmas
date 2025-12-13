package RMI;

import ObjetosCompartidos.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CafeteriaRemotaImpl extends UnicastRemoteObject implements ICafeteriaRemota {

    private final Parque parque;
    private final Mostrador mostrador;
    private final Despensa despensa;
    private final Caja caja;
    private final AreaConsumo areaConsumo;
    private final Cocina cocina;
    private final SalaDescanso salaDescanso;
    private final PauseController pauseController;

    public CafeteriaRemotaImpl(Parque parque, Mostrador mostrador, Despensa despensa, Caja caja, AreaConsumo areaConsumo, Cocina cocina, SalaDescanso salaDescanso, PauseController pauseController) throws RemoteException {
        super();
        this.parque = parque;
        this.mostrador = mostrador;
        this.despensa = despensa;
        this.caja = caja;
        this.areaConsumo = areaConsumo;
        this.cocina = cocina;
        this.salaDescanso = salaDescanso;
        this.pauseController = pauseController;
    }

    @Override
    public void pausarReanudar() throws RemoteException {
        if (pauseController.isPaused()) {
            pauseController.resume();
        } else {
            pauseController.pause();
        }
    }

    @Override
    public boolean estaPausado() throws RemoteException {
        return pauseController.isPaused();
    }

    @Override
    public int getClientesParque() throws RemoteException {
        return parque.getNumeroClientes();
    }

    @Override
    public int getClientesMostrador() throws RemoteException {
        return mostrador.getClientesEnMostrador();
    }

    @Override
    public int getClientesCaja() throws RemoteException {
        return caja.getClientesEnCaja();
    }

    @Override
    public int getClientesConsumicion() throws RemoteException {
        return areaConsumo.getClientesEnArea();
    }

    @Override
    public int getCocinerosCocina() throws RemoteException {
        return cocina.getCocineroEnCocina();
    }

    @Override
    public int getCocinerosDespensa() throws RemoteException {
        return despensa.getCocineroEnDespensa();
    }

    @Override
    public int getVendedoresDespensa() throws RemoteException {
        return despensa.getVendedoresEnDespensa();
    }

    @Override
    public int getVendedoresMostrador() throws RemoteException {
        return mostrador.getVendedoresEnMostrador();
    }

    @Override
    public int getPersonalSalaDescanso() throws RemoteException {
        return salaDescanso.getCocineros() + salaDescanso.getVendedores();
    }

    @Override
    public int getCafesDespensa() throws RemoteException {
        return despensa.getCafes();
    }

    @Override
    public int getRosquillasDespensa() throws RemoteException {
        return despensa.getRosquillas();
    }

    @Override
    public int getCafesMostrador() throws RemoteException {
        return mostrador.getCafes();
    }

    @Override
    public int getRosquillasMostrador() throws RemoteException {
        return mostrador.getRosquillas();
    }

    @Override
    public double getRecaudacion() throws RemoteException {
        return caja.getIngresosTotales();
    }
}