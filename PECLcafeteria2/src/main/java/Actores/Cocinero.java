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
import ObjetosCompartidos.Cocina;
import ObjetosCompartidos.Logger;
import ObjetosCompartidos.PauseController;

public class Cocinero implements Runnable {

    private final String id; // "B-XXXX"
    private final Despensa despensa; // stock en despensa
    private final Cocina cocina; // cocina compartida
    private final PauseController pauseController; // control de pausa

    public Cocinero(String id, Despensa despensa, Cocina cocina, PauseController pauseController) {
        this.id = id;
        this.despensa = despensa;
        this.cocina = cocina;
        this.pauseController = pauseController;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // 1) Descanso (5–10 s)
                Logger.log(id + " descansa en la sala");
                System.out.println(id + " descansa en la sala");
                Thread.sleep((int)(5000 + (10000 - 5000) * Math.random()));
                pauseController.checkPause();

                // 2) Trayecto a cocina (1–3 s)
                Logger.log(id + " se dirige a la cocina");
                System.out.println(id + " se dirige a la cocina");
                Thread.sleep((int)(1000 + (3000 - 1000) * Math.random()));
                pauseController.checkPause();


                // 3) Entrar a cocina (aforo 100 cocineros) y producir (2–5 cafés, 4–8 rosquillas; tiempo 5–10 s)
                int cafes = (int)(2 + Math.random() * 4);       // 2–5 cafés
                int rosquillas = (int)(4 + Math.random() * 5);  // 4–8 rosquillas
                cocina.pasarCocinaCocineros(id, cafes, rosquillas);
                // Salir de cocina
                Logger.log(id + " sale de la cocina");
                System.out.println(id + " sale de la cocina");
                pauseController.checkPause();

                // 5) Trayecto a despensa (2–5 s)
                Logger.log(id + " se dirige a la despensa");
                System.out.println(id + " se dirige a la despensa");
                Thread.sleep((int)(2000 + (5000 - 2000) * Math.random()));
                pauseController.checkPause();

                // 6) Entrar a despensa (aforo 50 cocineros) y deposita los productos
                despensa.pasarCocinerosDespensa(id, cafes, rosquillas);
                Logger.log(id + " sale de la despensa");
                System.out.println(id + " sale de la despensa");
                pauseController.checkPause();

                // 7) Volver a sala descanso (2–5 s)
                Logger.log(id + " vuelve a la sala de descanso");
                System.out.println(id + " vuelve a la sala de descanso");
                Thread.sleep((int)(2000 + (5000 - 2000) * Math.random()));
                pauseController.checkPause();
            }catch (InterruptedException e) {
            e.printStackTrace();
            Logger.log(id + " interrumpido");
            System.out.println(id + " interrumpido");}
        }
    }

}