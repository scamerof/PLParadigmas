/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Actores;

/**
 *
 * @author Sergio
 */

import ObjetosCompartidos.Despensa;
import ObjetosCompartidos.Mostrador;
import ObjetosCompartidos.Logger;
import ObjetosCompartidos.PauseController;

public class Vendedor implements Runnable {

    private final String id; // "V-XXXX"
    private final Despensa despensa; // stock en despensa
    private final Mostrador mostrador; // stock en mostrador
    private final PauseController pauseController; // control de pausa

    public Vendedor(String id, Despensa despensa, Mostrador mostrador, PauseController pauseController) {
        this.id = id;
        this.despensa = despensa;
        this.mostrador = mostrador;
        this.pauseController = pauseController;
    }

    @Override
    public void run() {
        while (true) {
            try {

                // 1) Descanso (5–10 s)
                Logger.log(id + " descansa en la sala");
                System.out.println(id + " descansa en la sala");
                Thread.sleep((int) (5000 + (10000 - 5000) * Math.random()));
                pauseController.checkPause();

                // 2) Trayecto a despensa (1–3 s)
                Logger.log(id + " se dirige a la despensa");
                System.out.println(id + " se dirige a la despensa");
                Thread.sleep((int) (1000 + (3000 - 1000) * Math.random()));
                pauseController.checkPause();

                // 3) Entrar a despensa (aforo 50 vendedores) y selecciona los productos
                int cafes = (int) (3 + Math.random() * 4); // 3–6 cafés
                int rosquillas = (int) (5 + Math.random() * 6); // 5–10 rosquillas
                Logger.log(id + " ha seleccionado " + cafes + " cafes y " + rosquillas + " rosquillas");
                System.out.println(id + " ha seleccionado " + cafes + " cafes y " + rosquillas + " rosquillas");

                // Esperar stock en despensa
                if (cafes > 0 || rosquillas > 0)
                despensa.pasarVendedoresDespensa(id, cafes, rosquillas);
                Logger.log(id + " sale de la despensa");
                System.out.println(id + " sale de la despensa");
                pauseController.checkPause();

                // 4) Trayecto al mostrador (2–5 s)
                Logger.log(id + " se dirige al mostrador");
                System.out.println(id + " se dirige al mostrador");
                Thread.sleep((int) (2000 + (5000 - 2000) * Math.random()));
                pauseController.checkPause();

                // 5) Entrar al mostrador (aforo 20 vendedores) y coloca productos en mostrador
                mostrador.pasarVendedoresMostrador(id, cafes, rosquillas);
                // Salir del mostrador
                Logger.log(id + " sale del mostrador");
                System.out.println(id + " sale del mostrador");
                pauseController.checkPause();
                
                // 6) Trayecto a sala descanso (2–5 s)
                Logger.log(id + " vuelve a la sala de descanso");
                System.out.println(id + " vuelve a la sala de descanso");
                Thread.sleep((int) (2000 + (5000 - 2000) * Math.random()));
                pauseController.checkPause();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.log(id + " interrumpido");
                System.err.println(id + " interrumpido");
            }
        }
    }
}