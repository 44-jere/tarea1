import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Panel visual que dibuja el semáforo con tres luces (rojo, amarillo, verde).
 * La luz activa se ilumina; las inactivas aparecen apagadas.
 */
public class PanelSemaforo extends JPanel {

    private Semaforo.Estado estadoActual = Semaforo.Estado.ROJO;

    private static final Color ROJO_ON    = new Color(220, 50,  50);
    private static final Color AMARILLO_ON = new Color(230, 200, 30);
    private static final Color VERDE_ON   = new Color(50,  200, 80);
    private static final Color LUZ_OFF    = new Color(80,  80,  80);
    private static final Color CARCASA    = new Color(40,  40,  40);

    public PanelSemaforo() {
        setPreferredSize(new Dimension(90, 220));
        setOpaque(false);
    }

    public void setEstado(Semaforo.Estado estado) {
        this.estadoActual = estado;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = 70;
        int alto  = 200;
        int x     = (getWidth() - ancho) / 2;
        int y     = 5;
        int radio = 22;
        int margen = 12;

        // Carcasa
        g2.setColor(CARCASA);
        g2.fillRoundRect(x, y, ancho, alto, 15, 15);

        // Luz ROJA (arriba)
        int cx = x + ancho / 2;
        int yRojo     = y + margen + radio;
        int yAmarillo = yRojo + radio * 2 + margen;
        int yVerde    = yAmarillo + radio * 2 + margen;

        dibujarLuz(g2, cx, yRojo,     radio, estadoActual == Semaforo.Estado.ROJO    ? ROJO_ON    : LUZ_OFF);
        dibujarLuz(g2, cx, yAmarillo, radio, estadoActual == Semaforo.Estado.AMARILLO ? AMARILLO_ON : LUZ_OFF);
        dibujarLuz(g2, cx, yVerde,    radio, estadoActual == Semaforo.Estado.VERDE   ? VERDE_ON   : LUZ_OFF);
    }

    private void dibujarLuz(Graphics2D g2, int cx, int cy, int r, Color color) {
        g2.setColor(color);
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2.setColor(Color.BLACK);
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
    }
}
