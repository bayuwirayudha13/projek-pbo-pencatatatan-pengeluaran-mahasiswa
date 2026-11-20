
import gui.MySakuGUI;
import service.ManajerMySaku;

import javax.swing.*;
import java.io.IOException;

/**
 * Titik masuk aplikasi MySaku.
 * Dikerjakan oleh: BAYU
 */
public class Main {

    public static void main(String[] args) {
        // Membuat objek pengelola aplikasi
        ManajerMySaku manajer = new ManajerMySaku();

        // Memuat data transaksi dan anggaran dari file
        try {
            manajer.muatDariFile();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Gagal memuat data: " + e.getMessage(),
                    "Kesalahan Memuat Data",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        // Menjalankan tampilan utama aplikasi
        SwingUtilities.invokeLater(() -> {
            MySakuGUI aplikasi = new MySakuGUI(manajer);
            aplikasi.setVisible(true);
        });
    }
}
