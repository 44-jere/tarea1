import javax.swing.SwingUtilities;

public class CarreraMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CarreraFrame frame = new CarreraFrame();
            frame.setVisible(true);
        });
    }
}
