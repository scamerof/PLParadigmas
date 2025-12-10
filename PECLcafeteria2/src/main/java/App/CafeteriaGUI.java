package App;

import ObjetosCompartidos.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CafeteriaGUI extends JFrame {
    
    private final EntradaCafeteria cafeteria;
    private final Mostrador mostrador;
    private final Despensa despensa;
    private final Caja caja;
    private final AreaConsumo areaConsumo;
    private final Cocina cocina;
    private final PauseController pauseController;
    private final SalaDescanso salaDescanso;
    
    // Etiquetas de zonas
    private JLabel labelEntrada, labelMostrador, labelCaja, labelConsumo;
    private JLabel labelCocina, labelDespensa, labelSalaDescanso;
    // Etiquetas de stock
    private JLabel labelCafesStock, labelRosquillasStock, labelRecaudacion;
    
    private JButton btnPausarReanudar;
    private boolean isPaused = false;
    private Thread updateThread;
    private volatile boolean running = true;
    
    // --- ESTILO ---
    // Usamos una fuente algo más pequeña (12) para que quepan las listas de IDs
    private final Font FONT_DATOS = new Font("Segoe UI", Font.PLAIN, 12); 
    private final Font FONT_TITULO = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_STOCK = new Font("Segoe UI", Font.PLAIN, 16);
    private final Color COLOR_TEXTO = new Color(50, 50, 150);

    public CafeteriaGUI(EntradaCafeteria cafeteria, Mostrador mostrador, Despensa despensa,
                        Caja caja, AreaConsumo areaConsumo, Cocina cocina, 
                        PauseController pauseController, SalaDescanso salaDescanso) {
        this.cafeteria = cafeteria;
        this.mostrador = mostrador;
        this.despensa = despensa;
        this.caja = caja;
        this.areaConsumo = areaConsumo;
        this.cocina = cocina;
        this.pauseController = pauseController;
        this.salaDescanso = salaDescanso;
        
        setTitle("Sistema de Cafetería - Monitor PECL");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800); // Un poco más grande para que quepan las listas
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
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 15));
        panel.setBorder(BorderFactory.createTitledBorder(null, "Estado de Zonas (IDs)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_TITULO));
        
        // Fila 1
        panel.add(createZoneCard("Entrada (Cola)", "labelEntrada"));
        panel.add(createZoneCard("Mostrador", "labelMostrador"));
        panel.add(createZoneCard("Caja", "labelCaja"));
        panel.add(createZoneCard("Área Consumo", "labelConsumo"));
        
        // Fila 2
        panel.add(createZoneCard("Cocina", "labelCocina"));
        panel.add(createZoneCard("Sala Descanso", "labelSalaDescanso"));
        panel.add(createZoneCard("Despensa", "labelDespensa"));
        
        // Relleno visual
        JPanel vacio = new JPanel();
        vacio.setOpaque(false);
        panel.add(vacio); 
        
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
        
        // Permitimos scroll si la lista es muy larga (opcional, pero útil)
        // JScrollPane scroll = new JScrollPane(label); // Si quieres scroll, descomenta esto y añade 'scroll' en vez de 'label'
        // scroll.setBorder(null);
        
        card.add(label, BorderLayout.CENTER);
        
        switch (varName) {
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
                    Thread.sleep(200); // 5 actualizaciones por segundo
                    updateDisplay();
                } catch (Exception e) {}
            }
        });
        updateThread.start();
    }
    
    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Aquí llamamos a los nuevos métodos get...String() de tus objetos compartidos
                
                labelEntrada.setText(formatList("Clientes", cafeteria.getClientesString()));
                
                labelMostrador.setText("<html><center>" + formatSubList("Clientes", mostrador.getClientesString(), "black") + "<br><br>" +formatSubList("Vendedores", mostrador.getVendedoresString(), "#0000AA") + "</center></html>");
                
                labelCaja.setText(formatList("Pagando", caja.getClientesString()));
                
                labelConsumo.setText(formatList("Comiendo", areaConsumo.getClientesString()));
                
                labelCocina.setText(formatList("Cocinando", cocina.getCocinerosString()));
                
                labelSalaDescanso.setText("<html><center>" + formatSubList("Cocineros", salaDescanso.getCocinerosString(), "black") + "<br><br>" +formatSubList("Vendedores", salaDescanso.getVendedoresString(), "#0000AA") +"</center></html>");
                
                labelDespensa.setText("<html><center>" + formatSubList("Vendedores", despensa.getVendedoresString(), "#0000AA") + "<br><br>" +formatSubList("Cocineros", despensa.getCocinerosString(), "#AA0000") +"</center></html>");
                
                // Stock y Dinero
                labelCafesStock.setText("<html><center>CAFÉS<br>Most: " + mostrador.getCafes() + " | Desp: " + despensa.getCafes() + "</center></html>");
                labelRosquillasStock.setText("<html><center>ROSQUILLAS<br>Most: " + mostrador.getRosquillas() + " | Desp: " + despensa.getRosquillas() + "</center></html>");
                labelRecaudacion.setText(String.format("%.2f €", caja.getIngresosTotales()));
                
            } catch (Exception e) {
                // e.printStackTrace(); 
            }
        });
    }
    
    // Método para limpiar el String del ArrayList [id1, id2] -> id1, id2
    private String formatList(String titulo, String listaRaw) {
        String lista = listaRaw.replace("[", "").replace("]", "").trim();
        if (lista.isEmpty()) return "<html><center><font color='#ccc'>-</font></center></html>";
        
        // HTML para envolver texto si es muy largo
        return "<html><center><div style='width: 200px; text-align: center;'>" + lista + "</div></center></html>";
    }

    // Método auxiliar para cuando hay dos listas en un mismo panel (Ej: Mostrador)
    private String formatSubList(String titulo, String listaRaw, String color) {
        String lista = listaRaw.replace("[", "").replace("]", "").trim();
        if (lista.isEmpty()) return "<font color='#ccc' size='3'>" + titulo + ": -</font>";
        
        return "<font color='" + color + "'><b>" + titulo + ":</b></font> <font size='3'>" + lista + "</font>";
    }
    
    public void mostrar() { setVisible(true); }
}