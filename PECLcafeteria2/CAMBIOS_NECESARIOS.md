# Cambios Necesarios para Corregir Problemas Detectados

## 🔴 CAMBIOS CRÍTICOS

### 1. Cliente.java - Eliminar `while (true)` (Línea 58)

**Problema**: Los clientes se ejecutan infinitamente en lugar de una sola vez.

**Código ACTUAL:**
```java
@Override
public void run() {
    while (true) {  // ❌ PROBLEMA: ciclo infinito
        try {
            // ... ciclo de vida del cliente ...
        } catch (InterruptedException e) {
            e.printStackTrace();
            Logger.log(id + " ha sido interrumpido");
            System.out.println(id + " ha sido interrumpido.");
        }
    }
}
```

**Código CORREGIDO:**
```java
@Override
public void run() {
    try {
        // 1) Parque (5–10 s)
        Logger.log(id + " esta en el parque");
        System.out.println(id + " esta en el parque");
        Thread.sleep((int)(5000 + (10000 - 5000) * Math.random()));
        pauseController.checkPause();

        // 2) Trayecto a la cafetería (3–9 s)
        Logger.log(id + " se dirige a la cafeteria");
        System.out.println(id + " se dirige a la cafeteria");
        Thread.sleep((int)(3000 + (9000 - 3000) * Math.random()));
        pauseController.checkPause();

        // 3) Zona previa (máx 20, FIFO por fair=true)
        cafeteria.pasarZonaPrevia(id);
        Logger.log(id + " sale de la zona previa");
        System.out.println(id + " sale de la zona previa");

        // 4) Mostrador (máx 5, FIFO por fair=true) y selección de productos
        cafesElegidos = (int)(1 + Math.random() * 3);     // 1–3 cafés
        rosquillasElegidas = (int)(Math.random() * 5);    // 0–4 rosquillas
        // Esperar stock (Lock + Condition en Mostrador)
        if (cafesElegidos > 0 || rosquillasElegidas > 0) {  // ✅ CORREGIDO: usar ||
            mostrador.pasarClienteMostrador(id, cafesElegidos, rosquillasElegidas);
        }
        Logger.log(id + " ha seleccionado " + cafesElegidos + " cafes y " + rosquillasElegidas + " rosquillas");
        System.out.println(id + " ha seleccionado " + cafesElegidos + " cafes y " + rosquillasElegidas + " rosquillas");
        Logger.log(id + " sale del mostrador");
        System.out.println(id + " sale del mostrador");

        // 5) Caja (aforo 10) + pagar (2–5 s)
        cajaPago.entrarCaja(id);
        Logger.log(id + " accede a la caja");
        System.out.println(id + " accede a la caja");
        cajaPago.pagar(id, cafesElegidos, rosquillasElegidas);
        pauseController.checkPause();
        cajaPago.salirCaja(id);
        Logger.log(id + " paga y sale de la caja");
        System.out.println(id + " paga y sale de la caja");

        // 6) Área de consumición (aforo 30; 10–15 s)
        areaConsumo.entrarAreaConsumo(id);
        Logger.log(id + " entra al area de consumo");
        System.out.println(id + " entra al area de consumo");
        Thread.sleep((int)(10000 + (15000 - 10000) * Math.random()));
        pauseController.checkPause();
        areaConsumo.salirAreaConsumo(id);
        Logger.log(id + " termina y se va");
        System.out.println(id + " termina y se va");
    } catch (InterruptedException e) {
        e.printStackTrace();
        Logger.log(id + " ha sido interrumpido");
        System.out.println(id + " ha sido interrumpido.");
    }
    // ✅ El hilo termina naturalmente después de completar el ciclo
}
```

**Cambios específicos:**
- ❌ Eliminar `while (true) {` (línea 58)
- ❌ Eliminar la llave de cierre `}` correspondiente (línea 112)
- ✅ Cambiar `&&` por `||` en línea 82

---

### 2. Cliente.java - Corregir condición de acceso al mostrador (Línea 82)

**Problema**: La condición `&&` impide que el cliente acceda al mostrador si solo pide un tipo de producto.

**Código ACTUAL:**
```java
if (cafesElegidos > 0 && rosquillasElegidas > 0) mostrador.pasarClienteMostrador(id, cafesElegidos, rosquillasElegidas);
```

**Código CORREGIDO:**
```java
if (cafesElegidos > 0 || rosquillasElegidas > 0) {
    mostrador.pasarClienteMostrador(id, cafesElegidos, rosquillasElegidas);
}
```

**Explicación**: Un cliente puede querer solo cafés (rosquillasElegidas = 0) o solo rosquillas (cafesElegidos = 0), por lo que debe usar `||` (OR) en lugar de `&&` (AND).

---

### 3. Vendedor.java - Corregir condición de acceso a despensa (Línea 53)

**Problema**: Similar al problema del cliente, la condición `&&` impide el acceso si solo puede obtener un tipo de producto.

**Código ACTUAL:**
```java
// Esperar stock en despensa
if (cafes > 0 && rosquillas > 0) despensa.pasarVendedoresDespensa(id, cafes, rosquillas);
```

**Código CORREGIDO:**
```java
// Esperar stock en despensa
if (cafes > 0 || rosquillas > 0) {
    despensa.pasarVendedoresDespensa(id, cafes, rosquillas);
}
```

**Explicación**: Un vendedor puede querer obtener solo cafés o solo rosquillas si el stock de uno de ellos no está disponible, por lo que debe usar `||`.

---

## 🟡 CAMBIOS IMPORTANTES (Recomendados)

### 4. Despensa.java - Usar `size()` en lugar de contadores (Líneas 134-149)

**Problema**: Los métodos `getCafes()` y `getRosquillas()` usan contadores que podrían desincronizarse con las listas.

**Código ACTUAL:**
```java
/** Lectura thread-safe del número de rosquillas almacenadas. */
public int getRosquillas() {
    lockRosquilla.lock();
    try {
        return contadorRosquilla;  // ❌ Usa contador
    } finally {
        lockRosquilla.unlock();
    }
}
/** Lectura thread-safe del número de cafés almacenados. */
public int getCafes() {
    lockCafe.lock();
    try {
        return contadorCafe;  // ❌ Usa contador
    } finally {
        lockCafe.unlock();
    }
}
```

**Código CORREGIDO:**
```java
/** Lectura thread-safe del número de rosquillas almacenadas. */
public int getRosquillas() {
    lockRosquilla.lock();
    try {
        return estanteriaRosquillas.size();  // ✅ Usa tamaño real de la lista
    } finally {
        lockRosquilla.unlock();
    }
}
/** Lectura thread-safe del número de cafés almacenados. */
public int getCafes() {
    lockCafe.lock();
    try {
        return estanteriaCafes.size();  // ✅ Usa tamaño real de la lista
    } finally {
        lockCafe.unlock();
    }
}
```

**Explicación**: Usar `size()` de las listas es más robusto y evita posibles inconsistencias si hay errores no manejados que desincronicen los contadores.

---

### 5. CafeteriaGUI.java - Corregir label de Sala Descanso (Línea 209)

**Problema**: El label muestra `despensa.getVendedoresEnDespensa()`, que es incorrecto. Debería mostrar el total de vendedores y cocineros en la sala de descanso.

**Código ACTUAL:**
```java
labelSalaDescanso.setText(String.valueOf(despensa.getVendedoresEnDespensa()));
```

**Problema adicional**: No hay un método para obtener el número de personas en la sala de descanso porque no hay límite de aforo (no hay semáforo que controle esto).

**Solución temporal (mostrar información más útil):**
```java
// Mostrar vendedores y cocineros en despensa (que es lo más cercano a "no en sala")
int totalEnDespensa = despensa.getVendedoresEnDespensa() + despensa.getCocineroEnDespensa();
labelSalaDescanso.setText("V:" + despensa.getVendedoresEnDespensa() + 
                          " C:" + despensa.getCocineroEnDespensa());
```

**Solución ideal (requiere implementación adicional):**
Para mostrar correctamente el número de personas en la sala de descanso, sería necesario:
1. Crear una clase `SalaDescanso` que rastree cuántos vendedores y cocineros están allí
2. O calcular: Total de vendedores/cocineros - (en despensa + en cocina + en mostrador)

**Código CORREGIDO (solución temporal):**
```java
// Mostrar información de despensa (vendedores y cocineros)
int vendedoresEnDespensa = despensa.getVendedoresEnDespensa();
int cocinerosEnDespensa = despensa.getCocineroEnDespensa();
labelSalaDescanso.setText("V:" + vendedoresEnDespensa + " C:" + cocinerosEnDespensa);
```

---

## 📋 RESUMEN DE CAMBIOS

| Archivo | Línea | Cambio | Prioridad |
|---------|-------|--------|-----------|
| `Cliente.java` | 58 | Eliminar `while (true)` | 🔴 CRÍTICO |
| `Cliente.java` | 82 | Cambiar `&&` por `||` | 🔴 CRÍTICO |
| `Vendedor.java` | 53 | Cambiar `&&` por `||` | 🔴 CRÍTICO |
| `Despensa.java` | 137 | Usar `estanteriaRosquillas.size()` | 🟡 IMPORTANTE |
| `Despensa.java` | 146 | Usar `estanteriaCafes.size()` | 🟡 IMPORTANTE |
| `CafeteriaGUI.java` | 209 | Corregir label Sala Descanso | 🟡 IMPORTANTE |

---

## ⚠️ NOTA SOBRE PARTE 2

La **Parte 2: Programación Distribuida** no está implementada. Según las especificaciones, es obligatoria y debe incluir:

- Servidor RMI o Sockets
- Cliente remoto
- Operaciones remotas para consultar el estado del sistema
- Actualización automática cada segundo

Este es un requisito crítico que debe implementarse para que la práctica esté completa.

