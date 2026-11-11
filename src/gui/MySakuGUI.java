package gui;

import service.ManajerMySaku;

import javax.swing.*;
import java.awt.*;

/** Jendela utama. Dikerjakan oleh: ALHIYA */
public class MySakuGUI extends JFrame {
    private ManajerMySaku manajer;

    public MySakuGUI(ManajerMySaku manajer) {
        this.manajer = manajer;
        setTitle("MySaku");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new FormTransaksiPanel(manajer), BorderLayout.WEST);
        add(new TabelTransaksiPanel(manajer), BorderLayout.CENTER);
        add(new RingkasanPanel(manajer), BorderLayout.SOUTH);
        // TODO (Alhiya): tombol Atur Batas Harian -> AnggaranDialog, dan refresh semua panel setelah data berubah
    }
}
