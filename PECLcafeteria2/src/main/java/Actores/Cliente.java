/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Actores;

/**
 *
 * @author Sergio
 */

import ObjetosCompartidos.EntradaCafeteria;
import ObjetosCompartidos.Caja;
import ObjetosCompartidos.Mostrador;
import ObjetosCompartidos.AreaConsumo;
import ObjetosCompartidos.Logger;
import ObjetosCompartidos.PauseController;
import ObjetosCompartidos.Parque;

/**
 * Cliente (C-XXXX) – Parte 1
 * Ciclo:
 * Parque (5–10s) → Trayecto (3–9s) → Zona previa (cap 20, FIFO) →
 * Mostrador (cap 5, FIFO; espera stock) → Caja (cap 10; 2–5s) →
 * Área de consumición (cap 30; 10–15s) → Fin.
 */
public class Cliente implements Runnable {

    // --- Identidad ---
    private String id;

    // --- Recursos compartidos ---
    private EntradaCafeteria cafeteria; // control de aforos (previa y mostrador)
    private Mostrador mostrador; // stock (Lock + Condition)
    private Caja cajaPago; // control de aforo (caja)
    private AreaConsumo areaConsumo; // control de aforo (área de consumición)
    private PauseController pauseController; // control de pausa
    private Parque parque; // control de fichaje

    // Pedido
    private int cafesElegidos;
    private int rosquillasElegidas;

    public Cliente(String id, Parque parque, EntradaCafeteria cafeteria, Mostrador mostrador, Caja cajaPago, AreaConsumo areaConsumo, PauseController pauseController) {
        this.id = id;
        this.parque = parque;
        this.cafeteria = cafeteria;
        this.mostrador = mostrador;
        this.cajaPago = cajaPago;
        this.areaConsumo = areaConsumo;
        this.pauseController = pauseController;
    }

    @Override
    public void run() {
        try {
            // 1) Parque (5–10 s)
            parque.entrar(id);  // Fichar entrada
            Logger.log(id + " descansa en la sala");
            System.out.println(id + " descansa en la sala");
            Thread.sleep((int) (5000 + (10000 - 5000) * Math.random()));
            pauseController.checkPause();
            parque.salir(id);  // Fichar salida

            // 2) Trayecto a la cafetería (3–9 s)
            Logger.log(id + " se dirige a la cafeteria");
            System.out.println(id + " se dirige a la cafeteria");
            Thread.sleep((int) (3000 + (9000 - 3000) * Math.random()));
            pauseController.checkPause();

            // 3) Zona previa (máx 20, FIFO por fair=true)
            cafeteria.entrarZonaPrevia(id);

            // 4) Mostrador (máx 5, FIFO por fair=true) y selección de productos
            cafesElegidos = (int) (1 + Math.random() * 3); // 1–3 cafés
            rosquillasElegidas = (int) (Math.random() * 5); // 0–4 rosquillas
            Logger.log(id + " ha seleccionado " + cafesElegidos + " cafes y " + rosquillasElegidas + " rosquillas");
            System.out.println(id + " ha seleccionado " + cafesElegidos + " cafes y " + rosquillasElegidas + " rosquillas");

            // Esperar stock (Lock + Condition en Mostrador)
            mostrador.pasarClienteMostrador(id, cafesElegidos, rosquillasElegidas, cafeteria);
            Logger.log(id + " sale del mostrador");
            System.out.println(id + " sale del mostrador");
            pauseController.checkPause();

            // 5) Caja (aforo 10) + pagar (2–5 s)
            cajaPago.pasarPorCaja(id, cafesElegidos, rosquillasElegidas);
            Logger.log(id + " paga y sale de la caja");
            System.out.println(id + " paga y sale de la caja");
            pauseController.checkPause();

            // 6) Área de consumición (aforo 30; 10–15 s)
            areaConsumo.pasarAreaConsumo(id);
            Logger.log(id + " termina y se va");
            System.out.println(id + " termina y se va");
        } catch (InterruptedException e) {
            e.printStackTrace();
            Logger.log(id + " ha sido interrumpido");
            System.out.println(id + " ha sido interrumpido.");
        }
    }
}