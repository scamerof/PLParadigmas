package App;

import ObjetosCompartidos.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CafeteriaGUI extends JFrame {
    
    // Recursos
    private final EntradaCafeteria cafeteria;
    private final Mostrador mostrador;
    private final Despensa despensa;
    private final Caja caja;
    private final AreaConsumo areaConsumo;
    private final Cocina cocina;
    private final PauseController pauseController;
    private final SalaDescanso salaDescanso;
    private final Parque parque; // Nuevo
    
    // Etiquetas
    private JLabel labelParque, labelEntrada, labelMostrador, labelCaja, labelConsumo;
    private JLabel labelCocina, labelDespensa, labelSalaDescanso;
    private JLabel labelCafesStock, labelRosquillasStock, labelRecaudacion;
    
    private JButton btnPausarReanudar;
    private boolean isPaused = false;
    private Thread updateThread;
    private volatile boolean running = true;
    
    // Estilo
    private final Font FONT_DATOS = new Font("Segoe UI", Font.PLAIN, 12); 
    private final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_STOCK = new Font("Segoe UI", Font.PLAIN, 16);
    private final Color COLOR_TEXTO = new Color(50, 50, 150);

    public CafeteriaGUI(EntradaCafeteria cafeteria, Mostrador mostrador, Despensa despensa,
                        Caja caja, AreaConsumo areaConsumo, Cocina cocina, 
                        PauseController pauseController, SalaDescanso salaDescanso, Parque parque) {
        this.cafeteria = cafeteria;
        this.mostrador = mostrador;
        this.despensa = despensa;
        this.caja = caja;
        this.areaConsumo = areaConsumo;
        this.cocina = cocina;
        this.pauseController = pauseController;
        this.salaDescanso = salaDescanso;
        this.parque = parque;
        
        setTitle("Sistema de Cafetería - Monitor PECL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 800);
        setLocationRelativeTo(null);
        
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
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
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);
        add(mainPanel);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(null, "Control", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_TITULO));
        btnPausarReanudar = new JButton("PAUSAR");
        btnPausarReanudar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnPausarReanudar.setPreferredSize(new Dimension(200, 50));
        btnPausarReanudar.addActionListener(this::togglePause);
        panel.add(btnPausarReanudar);
        return panel;
    }
    
    private JPanel createCenterPanel() {
        // Grid 3x3 para que quepan todos (Parque incluido)
        JPanel panel = new JPanel(new GridLayout(3, 3, 15, 15)); 
        panel.setBorder(BorderFactory.createTitledBorder(null, "Estado de Zonas (IDs)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_TITULO));
        
        panel.add(createZoneCard("Parque", "labelParque")); // Nuevo
        panel.add(createZoneCard("Entrada (Cola)", "labelEntrada"));
        panel.add(createZoneCard("Mostrador", "labelMostrador"));
        
        panel.add(createZoneCard("Caja", "labelCaja"));
        panel.add(createZoneCard("Área Consumo", "labelConsumo"));
        panel.add(createZoneCard("Cocina", "labelCocina"));
        
        panel.add(createZoneCard("Despensa", "labelDespensa"));
        panel.add(createZoneCard("Sala Descanso", "labelSalaDescanso"));
        panel.add(new JPanel()); // Relleno
        
        return panel;
    }
    
    private JPanel createZoneCard(String titulo, String varName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createTitledBorder(null, titulo, TitledBorder.CENTER, TitledBorder.TOP, FONT_TITULO)
        ));
        
        JLabel label = new JLabel("-");
        label.setFont(FONT_DATOS);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(COLOR_TEXTO);
        card.add(label, BorderLayout.CENTER);
        
        switch (varName) {
            case "labelParque" -> labelParque = label;
            case "labelEntrada" -> labelEntrada = label;
            case "labelMostrador" -> labelMostrador = label;
            case "labelCaja" -> labelCaja = label;
            case "labelConsumo" -> labelConsumo = label;
            case "labelCocina" -> labelCocina = label;
            case "labelDespensa" -> labelDespensa = label;
            case "labelSalaDescanso" -> labelSalaDescanso = label;
        }
        return card;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        panel.setBorder(BorderFactory.createTitledBorder(null, "Inventario", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_TITULO));
        
        labelCafesStock = new JLabel("Cafés: 0");
        labelCafesStock.setFont(FONT_STOCK);
        labelCafesStock.setHorizontalAlignment(SwingConstants.CENTER);
        labelCafesStock.setForeground(new Color(100, 60, 0));
        
        labelRosquillasStock = new JLabel("Rosquillas: 0");
        labelRosquillasStock.setFont(FONT_STOCK);
        labelRosquillasStock.setHorizontalAlignment(SwingConstants.CENTER);
        labelRosquillasStock.setForeground(new Color(200, 100, 0));
        
        labelRecaudacion = new JLabel("Total: 0.00 €");
        labelRecaudacion.setFont(new Font("Segoe UI", Font.BOLD, 22));
        labelRecaudacion.setHorizontalAlignment(SwingConstants.CENTER);
        labelRecaudacion.setForeground(new Color(0, 120, 0));
        
        panel.add(labelCafesStock);
        panel.add(labelRosquillasStock);
        panel.add(labelRecaudacion);
        return panel;
    }
    
    private void togglePause(ActionEvent e) {
        if (!isPaused) {
            isPaused = true;
            pauseController.pause();
            btnPausarReanudar.setText("REANUDAR");
            btnPausarReanudar.setBackground(new Color(220, 80, 80));
            btnPausarReanudar.setForeground(Color.WHITE);
        } else {
            isPaused = false;
            pauseController.resume();
            btnPausarReanudar.setText("PAUSAR");
            btnPausarReanudar.setBackground(null);
            btnPausarReanudar.setForeground(Color.BLACK);
        }
    }
    
    private void startUpdateThread() {
        updateThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(200); 
                    updateDisplay();
                } catch (Exception e) {}
            }
        });
        updateThread.start();
    }
    
    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Actualizar todas las etiquetas
                labelParque.setText(formatList("Paseando", parque.getClientesString()));
                labelEntrada.setText(formatList("En Cola", cafeteria.getClientesString()));
                
                labelMostrador.setText("<html><center>" + 
                        formatSubList("Cli", mostrador.getClientesString(), "black") + "<br>" +
                        formatSubList("Vend", mostrador.getVendedoresString(), "#0000AA") + "</center></html>");
                
                labelCaja.setText(formatList("Pagando", caja.getClientesString()));
                labelConsumo.setText(formatList("Comiendo", areaConsumo.getClientesString()));
                labelCocina.setText(formatList("Cocinando", cocina.getCocinerosString()));
                
                labelDespensa.setText("<html><center>" + 
                        formatSubList("Vend", despensa.getVendedoresString(), "#0000AA") + "<br>" +
                        formatSubList("Coc", despensa.getCocinerosString(), "#AA0000") + "</center></html>");
                        
                labelSalaDescanso.setText("<html><center>" + 
                        formatSubList("Coc", salaDescanso.getCocinerosString(), "black") + "<br>" +
                        formatSubList("Vend", salaDescanso.getVendedoresString(), "#0000AA") + "</center></html>");
                
                labelCafesStock.setText("<html><center>CAFÉS<br>Most: " + mostrador.getCafes() + " | Desp: " + despensa.getCafes() + "</center></html>");
                labelRosquillasStock.setText("<html><center>ROSQUILLAS<br>Most: " + mostrador.getRosquillas() + " | Desp: " + despensa.getRosquillas() + "</center></html>");
                labelRecaudacion.setText(String.format("%.2f €", caja.getIngresosTotales()));
                
            } catch (Exception e) {}
        });
    }
    
    private String formatList(String titulo, String listaRaw) {
        String lista = listaRaw.replace("[", "").replace("]", "").trim();
        if (lista.isEmpty()) return "<html><center><font color='#ccc'>-</font></center></html>";
        return "<html><center><div style='width: 180px; text-align: center;'>" + lista + "</div></center></html>";
    }

    private String formatSubList(String titulo, String listaRaw, String color) {
        String lista = listaRaw.replace("[", "").replace("]", "").trim();
        if (lista.isEmpty()) return "<font color='#ccc' size='3'>" + titulo + ": -</font>";
        return "<font color='" + color + "'><b>" + titulo + ":</b></font> <font size='3'>" + lista + "</font>";
    }
    
    public void mostrar() { setVisible(true); }
}