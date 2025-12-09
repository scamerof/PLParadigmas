package App;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import Actores.Cliente;
import Actores.Cocinero;
import Actores.Vendedor;

import ObjetosCompartidos.EntradaCafeteria;
import ObjetosCompartidos.Mostrador;
import ObjetosCompartidos.Caja;
import ObjetosCompartidos.Despensa;
import ObjetosCompartidos.Cocina;
import ObjetosCompartidos.AreaConsumo;
import ObjetosCompartidos.Logger;
import ObjetosCompartidos.PauseController;

/**
 *
 * @author Sergio
 */




public class MainCafeteria {

        public static void main(String[] args) {
            
            // --- Recursos compartidos ---
            EntradaCafeteria cafeteria = new EntradaCafeteria();           // controla zona previa y mostrador
            Mostrador mostrador = new Mostrador();                        // controla stock en mostrador
            Despensa despensa = new Despensa();                          // controla stock en despensa
            Cocina cocina = new Cocina();                               // controla acceso a cocina
            Caja cajaPago = new Caja();                                // controla pagos
            AreaConsumo areaConsumo = new AreaConsumo();              // controla área de consumición
            PauseController pauseController = new PauseController(); // controla pause/resume
            
            // Registrar inicio del sistema
            Logger.log("SISTEMA DE CAFETERIA INICIADO");

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
            CafeteriaGUI gui = new CafeteriaGUI(cafeteria, mostrador, despensa, cajaPago, areaConsumo, cocina, pauseController);
            gui.mostrar();
            
            // --- Lanzar COCINEROS en paralelo ---
            new Thread(() -> {
                for (int i = 1; i <= 500; i++) {
                    String idCocinero = String.format("B-%04d", i);
                    Cocinero cocinero = new Cocinero(idCocinero, despensa, cocina, pauseController);
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
                    Vendedor vendedor = new Vendedor(idVendedor, despensa, mostrador, pauseController);
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
                    Cliente cliente = new Cliente(idCliente, cafeteria, mostrador, cajaPago, areaConsumo, pauseController);
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