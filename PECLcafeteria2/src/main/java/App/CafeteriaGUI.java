package App;

import ObjetosCompartidos.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Interfaz gráfica del sistema de cafetería.
 * Muestra el estado en tiempo real y permite pausar/reanudar.
 */
public class CafeteriaGUI extends JFrame {
    
    private final EntradaCafeteria cafeteria;
    private final Mostrador mostrador;
    private final Despensa despensa;
    private final Caja caja;
    private final AreaConsumo areaConsumo;
    private final Cocina cocina;
    private final PauseController pauseController;
    
    // Panel de estado
    private JLabel labelEntrada;
    private JLabel labelMostrador;
    private JLabel labelCaja;
    private JLabel labelConsumo;
    private JLabel labelCocina;
    private JLabel labelDespensa;
    private JLabel labelSalaDescanso;
    private JLabel labelCafesStock;
    private JLabel labelRosquillasStock;
    private JLabel labelRecaudacion;
    private JButton btnPausarReanudar;
    private boolean isPaused = false;
    
    // Thread de actualización
    private Thread updateThread;
    private volatile boolean running = true;
    
    public CafeteriaGUI(EntradaCafeteria cafeteria, Mostrador mostrador, Despensa despensa,
                        Caja caja, AreaConsumo areaConsumo, Cocina cocina, PauseController pauseController) {
        this.cafeteria = cafeteria;
        this.mostrador = mostrador;
        this.despensa = despensa;
        this.caja = caja;
        this.areaConsumo = areaConsumo;
        this.cocina = cocina;
        this.pauseController = pauseController;
        
        setTitle("Sistema de Cafetería - PECL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        
        initComponents();
        startUpdateThread();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
                Logger.close();
            }
        });
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de control (arriba)
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        
        // Panel de estado (centro)
        JPanel statePanel = createStatePanel();
        mainPanel.add(statePanel, BorderLayout.CENTER);
        
        // Panel de stock (abajo)
        JPanel stockPanel = createStockPanel();
        mainPanel.add(stockPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Control del Sistema"));
        
        btnPausarReanudar = new JButton("Pausar");
        btnPausarReanudar.setPreferredSize(new Dimension(150, 40));
        btnPausarReanudar.setFont(new Font("Arial", Font.BOLD, 14));
        btnPausarReanudar.addActionListener(this::togglePause);
        
        panel.add(btnPausarReanudar);
        return panel;
    }
    
    private JPanel createStatePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Estado de Zonas"));
        
        panel.add(createZonePanel("Entrada Cafetería", "labelEntrada"));
        panel.add(createZonePanel("Mostrador", "labelMostrador"));
        panel.add(createZonePanel("Caja", "labelCaja"));
        panel.add(createZonePanel("Área de Consumo", "labelConsumo"));
        panel.add(createZonePanel("Cocina", "labelCocina"));
        panel.add(createZonePanel("Sala Descanso", "labelSalaDescanso"));
        panel.add(createZonePanel("Despensa", "labelDespensa"));
        panel.add(new JPanel()); // Panel vacío para llenar el espacio
        
        return panel;
    }
    
    private JPanel createZonePanel(String title, String fieldName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JLabel label = new JLabel("<html><center>-</center></html>");
        label.setFont(new Font("Arial", Font.PLAIN, 9));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(new Color(150, 50, 150)); // Morado para todos los paneles
        
        panel.add(label, BorderLayout.CENTER);
        
        // Guardar referencias
        switch (fieldName) {
            case "labelEntrada" -> labelEntrada = label;
            case "labelMostrador" -> labelMostrador = label;
            case "labelCaja" -> labelCaja = label;
            case "labelConsumo" -> labelConsumo = label;
            case "labelCocina" -> labelCocina = label;
            case "labelDespensa" -> labelDespensa = label;
            case "labelSalaDescanso" -> labelSalaDescanso = label;
        }
        
        return panel;
    }
    
    private JPanel createStockPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Información General"));
        
        // Cafés
        labelCafesStock = new JLabel("Cafés: 0");
        labelCafesStock.setFont(new Font("Arial", Font.BOLD, 14));
        labelCafesStock.setForeground(new Color(139, 69, 19));
        panel.add(labelCafesStock);
        
        // Rosquillas
        labelRosquillasStock = new JLabel("Rosquillas: 0");
        labelRosquillasStock.setFont(new Font("Arial", Font.BOLD, 14));
        labelRosquillasStock.setForeground(new Color(210, 105, 30));
        panel.add(labelRosquillasStock);
        
        // Recaudación
        labelRecaudacion = new JLabel("Recaudación: 0.00 €");
        labelRecaudacion.setFont(new Font("Arial", Font.BOLD, 14));
        labelRecaudacion.setForeground(new Color(34, 139, 34));
        panel.add(labelRecaudacion);
        
        return panel;
    }
    
    private void togglePause(ActionEvent e) {
        isPaused = !isPaused;
        
        if (isPaused) {
            pauseController.pause();
            btnPausarReanudar.setText("Reanudar");
            btnPausarReanudar.setBackground(new Color(255, 100, 100));
            btnPausarReanudar.setOpaque(true);
        } else {
            pauseController.resume();
            btnPausarReanudar.setText("Pausar");
            btnPausarReanudar.setBackground(null);
        }
    }
    
    private void startUpdateThread() {
        updateThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(500); // Actualizar cada 500ms
                    updateDisplay();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Entrada Cafetería - solo clientes (sin V)
                int clientesEntrada = cafeteria.getClientesEnZonaPrevia();
                labelEntrada.setText(formatSoloClientes(clientesEntrada));
                
                // Mostrador - clientes y vendedores
                int clientesMostrador = mostrador.getClientesEnMostrador();
                int vendedoresMostrador = mostrador.getVendedoresEnMostrador();
                labelMostrador.setText("C:" + clientesMostrador + " V:" + vendedoresMostrador);
                
                // Caja - solo clientes (sin V)
                int clientesCaja = caja.getClientesEnCaja();
                labelCaja.setText(formatSoloClientes(clientesCaja));
                
                // Área de Consumo - solo clientes (sin V)
                int clientesConsumo = areaConsumo.getClientesEnArea();
                labelConsumo.setText(formatSoloClientes(clientesConsumo));
                
                // Cocina - solo cocineros (sin V)
                int cocinerosCocina = cocina.getCocineroEnCocina();
                labelCocina.setText(formatSoloCocineros(cocinerosCocina));
                
                // Despensa - vendedores y cocineros
                int vendedoresEnDespensa = despensa.getVendedoresEnDespensa();
                int cocinerosEnDespensa = despensa.getCocineroEnDespensa();
                labelDespensa.setText("V:" + vendedoresEnDespensa + " C:" + cocinerosEnDespensa);
                
                // Sala de descanso - solo clientes (sin V, según el usuario)
                labelSalaDescanso.setText("<html><center>-</center></html>");
                
                // Actualizar stock
                labelCafesStock.setText("Cafés: " + mostrador.getCafes() + " | Despensa: " + despensa.getCafes());
                labelRosquillasStock.setText("Rosquillas: " + mostrador.getRosquillas() + " | Despensa: " + despensa.getRosquillas());
                labelRecaudacion.setText("Recaudación: " + String.format("%.2f", caja.getIngresosTotales()) + " €");
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private String formatSoloClientes(int cantidad) {
        if (cantidad == 0) {
            return "<html><center>-</center></html>";
        }
        return "<html><center>C:" + cantidad + "</center></html>";
    }
    
    private String formatSoloCocineros(int cantidad) {
        if (cantidad == 0) {
            return "<html><center>-</center></html>";
        }
        
        String resultado = String.join("<br><br>");
        return "<html><center>" + resultado + "</center></html>";
    }
    
    public void mostrar() {
        setVisible(true);
    }
}
