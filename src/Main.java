import gui.MySakuGUI;
import service.ManajerMySaku;

import javax.swing.*;
import java.io.IOException;

/** Titik masuk aplikasi. Dikerjakan oleh: BAYU */
public class Main {
    public static void main(String[] args) {
        ManajerMySaku manajer = new ManajerMySaku();
        try {
            manajer.muatDariFile();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat data: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new MySakuGUI(manajer).setVisible(true));
    }
}
