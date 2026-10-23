package calculadora.gui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazMathPro app = new InterfazMathPro();
            app.setVisible(true);
        });
    }
}