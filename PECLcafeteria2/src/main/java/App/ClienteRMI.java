package App;

import RMI.ICafeteriaRemota;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.rmi.Naming;

public class ClienteRMI extends JFrame {

    private ICafeteriaRemota servicio;
    private JLabel lblParque, lblMostradorC, lblCaja, lblConsumo;
    private JLabel lblCocina, lblDespensaC, lblDespensaV, lblMostradorV, lblSala;
    private JLabel lblStockDespensa, lblStockMostrador, lblRecaudacion;
    private JButton btnPausa;
    private boolean conectado = false;

    public ClienteRMI() {
        setTitle("Monitor Remoto Cafetería (RMI)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        try { conectar(); } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error conectando: " + e.getMessage()); }

        initComponents();
        
        // Timer para actualizar cada segundo (1000 ms) como pide el enunciado
        new Timer(1000, e -> actualizarDatos()).start();
    }

    private void conectar() throws Exception {
        // Busca el objeto remoto en localhost puerto 1099 (por defecto)
        servicio = (ICafeteriaRemota) Naming.lookup("//localhost/Cafeteria");
        conectado = true;
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Botón superior
        btnPausa = new JButton("DETENER / REANUDAR");
        btnPausa.setFont(new Font("Arial", Font.BOLD, 16));
        btnPausa.addActionListener(e -> togglePausa());
        mainPanel.add(btnPausa, BorderLayout.NORTH);

        // Panel Central (Datos)
        JPanel grid = new JPanel(new GridLayout(5, 2, 10, 10)); // 5 filas
        grid.setBorder(BorderFactory.createTitledBorder("Estado del Sistema"));
        
        lblParque = addDato(grid, "Clientes en Parque");
        lblMostradorC = addDato(grid, "Clientes en Mostrador");
        lblCaja = addDato(grid, "Clientes en Caja");
        lblConsumo = addDato(grid, "Clientes en Consumición");
        lblCocina = addDato(grid, "Cocineros en Cocina");
        lblDespensaC = addDato(grid, "Cocineros en Despensa");
        lblDespensaV = addDato(grid, "Vendedores en Despensa");
        lblMostradorV = addDato(grid, "Vendedores en Mostrador");
        lblSala = addDato(grid, "Personal en Sala Descanso");
        lblRecaudacion = addDato(grid, "Recaudación Total");
        
        mainPanel.add(grid, BorderLayout.CENTER);

        // Panel Inferior (Stock)
        JPanel stockPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        lblStockDespensa = new JLabel("Despensa: -", SwingConstants.CENTER);
        lblStockMostrador = new JLabel("Mostrador: -", SwingConstants.CENTER);
        lblStockDespensa.setBorder(BorderFactory.createTitledBorder("Stock Despensa"));
        lblStockMostrador.setBorder(BorderFactory.createTitledBorder("Stock Mostrador"));
        
        stockPanel.add(lblStockDespensa);
        stockPanel.add(lblStockMostrador);
        mainPanel.add(stockPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JLabel addDato(JPanel p, String titulo) {
        JPanel sub = new JPanel(new BorderLayout());
        sub.setBorder(BorderFactory.createTitledBorder(null, titulo, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 12)));
        JLabel l = new JLabel("-");
        l.setFont(new Font("Arial", Font.PLAIN, 20));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        sub.add(l);
        p.add(sub);
        return l;
    }

    private void togglePausa() {
        if (!conectado) return;
        try {
            servicio.pausarReanudar();
            actualizarBoton();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void actualizarBoton() {
        try {
            if (servicio.estaPausado()) {
                btnPausa.setBackground(Color.RED);
                btnPausa.setText("REANUDAR SISTEMA");
                btnPausa.setForeground(Color.WHITE);
            } else {
                btnPausa.setBackground(null);
                btnPausa.setText("DETENER SISTEMA");
                btnPausa.setForeground(Color.BLACK);
            }
        } catch (Exception e) {}
    }

    private void actualizarDatos() {
        if (!conectado) return;
        try {
            lblParque.setText(String.valueOf(servicio.getClientesParque()));
            lblMostradorC.setText(String.valueOf(servicio.getClientesMostrador()));
            lblCaja.setText(String.valueOf(servicio.getClientesCaja()));
            lblConsumo.setText(String.valueOf(servicio.getClientesConsumicion()));
            lblCocina.setText(String.valueOf(servicio.getCocinerosCocina()));
            lblDespensaC.setText(String.valueOf(servicio.getCocinerosDespensa()));
            lblDespensaV.setText(String.valueOf(servicio.getVendedoresDespensa()));
            lblMostradorV.setText(String.valueOf(servicio.getVendedoresMostrador()));
            lblSala.setText(String.valueOf(servicio.getPersonalSalaDescanso()));
            lblRecaudacion.setText(String.format("%.2f €", servicio.getRecaudacion()));
            
            lblStockDespensa.setText("C: " + servicio.getCafesDespensa() + " | R: " + servicio.getRosquillasDespensa());
            lblStockMostrador.setText("C: " + servicio.getCafesMostrador() + " | R: " + servicio.getRosquillasMostrador());
            
            actualizarBoton();
        } catch (Exception e) {
            setTitle("Monitor Remoto - DESCONECTADO");
            conectado = false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClienteRMI().setVisible(true));
    }
}