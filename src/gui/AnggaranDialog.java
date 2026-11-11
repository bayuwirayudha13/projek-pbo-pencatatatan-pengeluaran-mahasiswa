package gui;

import service.ManajerMySaku;

import javax.swing.*;

/** Dialog atur batas harian. Dikerjakan oleh: ALHIYA */
public class AnggaranDialog extends JDialog {
    private ManajerMySaku manajer;

    public AnggaranDialog(JFrame parent, ManajerMySaku manajer) {
        super(parent, "Atur Batas Harian", true);
        this.manajer = manajer;
        setSize(350, 180);
        setLocationRelativeTo(parent);
        // TODO (Alhiya): input batas, validasi lewat ValidasiInput, simpan ke manajer.getAnggaran()
    }
}
