package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICafeteriaRemota extends Remote {
    
    // Control
    public void pausarReanudar() throws RemoteException;
    public boolean estaPausado() throws RemoteException;

    // Clientes
    public int getClientesParque() throws RemoteException;
    public int getClientesMostrador() throws RemoteException;
    public int getClientesCaja() throws RemoteException;
    public int getClientesConsumicion() throws RemoteException;
    
    // Cocineros
    public int getCocinerosCocina() throws RemoteException;
    public int getCocinerosDespensa() throws RemoteException;
    
    // Vendedores
    public int getVendedoresDespensa() throws RemoteException;
    public int getVendedoresMostrador() throws RemoteException;
    
    // Sala Descanso (Suma de ambos)
    public int getPersonalSalaDescanso() throws RemoteException;
    
    // Stock Despensa
    public int getCafesDespensa() throws RemoteException;
    public int getRosquillasDespensa() throws RemoteException;
    
    // Stock Mostrador
    public int getCafesMostrador() throws RemoteException;
    public int getRosquillasMostrador() throws RemoteException;
    
    // Dinero
    public double getRecaudacion() throws RemoteException;
}