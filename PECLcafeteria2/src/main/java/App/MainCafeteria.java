package App;



/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import Actores.Cliente;
import Actores.Cocinero;
import Actores.Vendedor;

import ObjetosCompartidos.Parque;
import ObjetosCompartidos.EntradaCafeteria;
import ObjetosCompartidos.Mostrador;
import ObjetosCompartidos.Caja;
import ObjetosCompartidos.Despensa;
import ObjetosCompartidos.Cocina;
import ObjetosCompartidos.AreaConsumo;
import ObjetosCompartidos.Logger;
import ObjetosCompartidos.PauseController;
import ObjetosCompartidos.SalaDescanso;
import RMI.CafeteriaRemotaImpl;  // Importar RMI
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 *
 * @author Sergio
 */




public class MainCafeteria {

        public static void main(String[] args) {
            
            // --- Recursos compartidos ---
            Parque parque = new Parque();                                   // controla parque
            EntradaCafeteria cafeteria = new EntradaCafeteria();           // controla zona previa
            Mostrador mostrador = new Mostrador();                        // controla stock en mostrador
            Despensa despensa = new Despensa();                          // controla stock en despensa
            Cocina cocina = new Cocina();                               // controla acceso a cocina
            Caja cajaPago = new Caja();                                // controla pagos
            AreaConsumo areaConsumo = new AreaConsumo();              // controla área de consumición
            PauseController pauseController = new PauseController(); // controla pause/resume
            SalaDescanso salaDescanso = new SalaDescanso();         // controla sala de descanso


                // 2. INICIAR SERVIDOR RMI
            try {
                // Puerto por defecto 1099
                LocateRegistry.createRegistry(1099);
                
                CafeteriaRemotaImpl objetoRemoto = new CafeteriaRemotaImpl(
                    parque, mostrador, despensa, cajaPago, areaConsumo, cocina, salaDescanso, pauseController
                );
                
                Naming.rebind("//localhost/Cafeteria", objetoRemoto);
                Logger.log("Servidor RMI iniciado en //localhost/Cafeteria");
            } catch (Exception e) {
                System.err.println("Error iniciando RMI: " + e.getMessage());
            }

            // Inicializar stock inicial razonable
            try {
                despensa.pasarCocinerosDespensa("INIT", 250, 250); // llena la despensa
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                mostrador.pasarVendedoresMostrador("INIT", 125, 125); // llena el mostrador
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Logger.log("Stock inicial: despensa 250/250, mostrador 125/125");

            // --- Crear y mostrar GUI en EDT ---
            CafeteriaGUI gui = new CafeteriaGUI(cafeteria, mostrador, despensa, cajaPago, areaConsumo, cocina, pauseController, salaDescanso, parque);
            gui.mostrar();
            
            // Registrar inicio del sistema
            Logger.log("SISTEMA DE CAFETERIA INICIADO");

            
            // --- Lanzar COCINEROS en paralelo ---
            new Thread(() -> {
                for (int i = 1; i <= 500; i++) {
                    String idCocinero = String.format("B-%04d", i);
                    Cocinero cocinero = new Cocinero(idCocinero, despensa, cocina, pauseController, salaDescanso);
                    new Thread(cocinero).start();

                    // Pausa entre 1 y 2 segundos
                    try {
                        Thread.sleep((int)(1000 + (2000 - 1000) * Math.random()));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();

            // --- Lanzar VENDEDORES---
            new Thread(() -> {
                for (int i = 1; i <= 500; i++) {
                    String idVendedor = String.format("V-%04d", i);
                    Vendedor vendedor = new Vendedor(idVendedor, despensa, mostrador, pauseController, salaDescanso);
                    new Thread(vendedor).start();

                    // Pausa aleatoria entre 0.5 y 2.5 segundos
                    try {
                        Thread.sleep((int)(500 + (2500 - 500) * Math.random()));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();

            // --- Lanzar CLIENTES también en paralelo ---
            new Thread(() -> {
                for (int i = 1; i <= 8000; i++) {
                    String idCliente = String.format("C-%04d", i);
                    Cliente cliente = new Cliente(idCliente, parque, cafeteria, mostrador, cajaPago, areaConsumo, pauseController);
                    new Thread(cliente).start();

                    // Pausa entre 1 y 3 segundos
                    try {
                        Thread.sleep((int)(1000 + (3000 - 1000) * Math.random()));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();

    }
}