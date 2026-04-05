import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ventana principal de la carrera de caballos.
 *
 * Layout:
 *   - Izquierda: botón Iniciar + pistas de caballos
 *   - Derecha:   semáforo visual
 *
 * Sincronización:
 *   HiloSemaforo  →  cambia estado del Semaforo  →  despierta HiloCaballo(s)
 */
public class CarreraFrame extends JFrame implements HiloSemaforo.SemaforoListener {

    // ---- Configuración ----
    private static final int NUM_CABALLOS   = 2;
    private static final int META           = 300; // valor máximo de la barra

    // ---- Componentes UI ----
    private final JButton btnIniciar;
    private final PanelSemaforo panelSemaforo;
    private final JLabel lblEstado;
    private final List<JProgressBar> barras = new ArrayList<>();
    private final List<JLabel> labelsGanador = new ArrayList<>();

    // ---- Sincronización ----
    private final Semaforo semaforo = new Semaforo();
    private HiloSemaforo hiloSemaforo;
    private final List<HiloCaballo> hilosCaballos = new ArrayList<>();

    // ---- Iconos de caballos (emoji unicode como fallback) ----
    private static final String[] NOMBRES = {"Caballo 1", "Caballo 2"};

    public CarreraFrame() {
        super("Carrera de Caballos - Sincronización de Hilos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Estilo del botón
        btnIniciar = new JButton("Iniciar Carrera");
        btnIniciar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnIniciar.setFocusPainted(false);

        panelSemaforo = new PanelSemaforo();

        lblEstado = new JLabel("Listo para iniciar", JLabel.CENTER);
        lblEstado.setFont(new Font("SansSerif", Font.ITALIC, 12));

        buildUI();
        pack();
        setLocationRelativeTo(null);

        btnIniciar.addActionListener(e -> iniciarCarrera());
    }

    // ---------------------------------------------------------------
    // Construcción de la interfaz
    // ---------------------------------------------------------------
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        root.setBackground(new Color(240, 240, 245));

        // Panel izquierdo: botón + pistas
        JPanel izquierda = new JPanel(new GridBagLayout());
        izquierda.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.gridx  = 0;
        gbc.gridy  = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Botón Iniciar Carrera
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.add(btnIniciar);
        izquierda.add(topPanel, gbc);

        // Pistas de caballos
        for (int i = 0; i < NUM_CABALLOS; i++) {
            gbc.gridy++;
            JLabel iconoCaballo = new JLabel("\uD83D\uDC0E"); // 🐎
            iconoCaballo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            izquierda.add(iconoCaballo, gbc);

            gbc.gridy++;
            JProgressBar barra = new JProgressBar(0, META);
            barra.setPreferredSize(new Dimension(380, 28));
            barra.setStringPainted(true);
            barra.setString(NOMBRES[i]);
            barra.setForeground(i == 0 ? new Color(70, 130, 180) : new Color(180, 80, 80));
            barra.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            barras.add(barra);
            izquierda.add(barra, gbc);
        }

        // Etiqueta de estado
        gbc.gridy++;
        izquierda.add(lblEstado, gbc);

        // Panel derecho: semáforo
        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setOpaque(false);
        derecha.add(panelSemaforo, BorderLayout.CENTER);
        derecha.setPreferredSize(new Dimension(100, 260));

        root.add(izquierda, BorderLayout.CENTER);
        root.add(derecha,   BorderLayout.EAST);

        add(root);
    }

    // ---------------------------------------------------------------
    // Lógica de carrera
    // ---------------------------------------------------------------
    private void iniciarCarrera() {
        // Evita doble clic
        btnIniciar.setEnabled(false);
        lblEstado.setForeground(Color.DARK_GRAY);
        lblEstado.setText("Carrera en curso...");

        // Reinicia barras
        for (JProgressBar b : barras) {
            b.setValue(0);
        }

        // Reinicia el semáforo a ROJO antes de comenzar
        semaforo.setEstado(Semaforo.Estado.ROJO);
        panelSemaforo.setEstado(Semaforo.Estado.ROJO);

        // Cancela hilos previos si existieran
        for (HiloCaballo hc : hilosCaballos) {
            hc.detener();
        }
        hilosCaballos.clear();

        if (hiloSemaforo != null && hiloSemaforo.isAlive()) {
            hiloSemaforo.interrupt();
        }

        // Crea hilos de caballos
        for (int i = 0; i < NUM_CABALLOS; i++) {
            HiloCaballo hc = new HiloCaballo(NOMBRES[i], barras.get(i), semaforo);
            hilosCaballos.add(hc);
            hc.start();
        }

        // Crea y arranca el hilo del semáforo
        hiloSemaforo = new HiloSemaforo(semaforo, this);
        hiloSemaforo.start();
    }

    // ---------------------------------------------------------------
    // Callbacks desde HiloSemaforo (llamados desde hilo secundario)
    // ---------------------------------------------------------------
    @Override
    public void onEstadoCambiado(Semaforo.Estado estado) {
        SwingUtilities.invokeLater(() -> {
            panelSemaforo.setEstado(estado);
            switch (estado) {
                case VERDE    -> lblEstado.setText("¡Luz VERDE! — Los caballos corren (30s)");
                case AMARILLO -> lblEstado.setText("Luz AMARILLA — Los caballos desaceleran...");
                case ROJO     -> lblEstado.setText("Luz ROJA — ¡Carrera terminada!");
            }
        });

        // Detener caballos cuando deja de ser VERDE
        if (estado != Semaforo.Estado.VERDE) {
            for (HiloCaballo hc : hilosCaballos) {
                hc.detener();
            }
        }
    }

    @Override
    public void onCarreraTerminada() {
        SwingUtilities.invokeLater(() -> {
            // Determina ganador
            int maxVal   = -1;
            String ganador = "Empate";
            for (int i = 0; i < barras.size(); i++) {
                int val = barras.get(i).getValue();
                if (val > maxVal) {
                    maxVal  = val;
                    ganador = NOMBRES[i];
                }
            }
            lblEstado.setText("¡Carrera terminada! — Ganador: " + ganador);
            lblEstado.setForeground(new Color(0, 120, 0));
            btnIniciar.setEnabled(true);
        });
    }
}
